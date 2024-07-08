package com.ulyp.agent.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.DataProvider;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.queue.events.*;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.LoggingSettings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class QueueBatchEventProcessor implements EventProcessor {
    private static final int IDLE = 0;
    private static final int HALTED = IDLE + 1;
    private static final int RUNNING = HALTED + 1;

    private final TypeResolver typeResolver;
    private final AgentDataWriter agentDataWriter;
    private final Map<Integer, RecordingEventProcessor> recordingQueueProcessors = new HashMap<>();
    private final AtomicInteger status = new AtomicInteger(IDLE);
    private final DataProvider<RecordingEventDisruptorEntry> dataProvider;
    private final SequenceBarrier sequenceBarrier;
    private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);

    public QueueBatchEventProcessor(
            DataProvider<RecordingEventDisruptorEntry> dataProvider,
            SequenceBarrier sequenceBarrier,
            TypeResolver typeResolver,
            AgentDataWriter agentDataWriter) {
        this.dataProvider = dataProvider;
        this.sequenceBarrier = sequenceBarrier;
        this.typeResolver = typeResolver;
        this.agentDataWriter = agentDataWriter;
    }

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void halt() {
        status.set(HALTED);
        sequenceBarrier.alert();
    }

    @Override
    public boolean isRunning() {
        return status.get() != IDLE;
    }

    @Override
    public void run() {
        if (status.compareAndSet(IDLE, RUNNING)) {
            sequenceBarrier.clearAlert();

            try {
                if (status.get() == RUNNING) {
                    processEvents();
                }
            } finally {
                status.set(IDLE);
            }
        } else {
            if (status.get() == RUNNING) {
                throw new IllegalStateException("Thread is already running");
            }
        }
    }

    private void processEvents() {
        long nextSequence = sequence.get() + 1L;

        while (true) {
            try {
                final long availableSequence = sequenceBarrier.waitFor(nextSequence);

                while (nextSequence <= availableSequence) {
                    processAtSeq(nextSequence);
                    nextSequence++;
                }

                sequence.set(availableSequence);
            } catch (final AlertException ex) {
                if (status.get() != RUNNING) {
                    return;
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                sequence.set(nextSequence);
                nextSequence++;
            }
        }
    }

    private void processAtSeq(long sequence) {
        RecordingEventDisruptorEntry batch = dataProvider.get(sequence);
        try {
            int recordingId = batch.getRecordingId();
            RecordingEventProcessor processor = recordingQueueProcessors.get(batch.getRecordingId());
            if (processor == null) {
                processor = new RecordingEventProcessor(typeResolver, agentDataWriter);
                recordingQueueProcessors.put(recordingId, processor);
            }
            for (RecordingEvent event : batch.getEvents()) {
                if (event instanceof EnterMethodRecordingEvent) {
                    processor.onEnterCallRecord(recordingId, (EnterMethodRecordingEvent) event);
                } else if (event instanceof ExitMethodRecordingEvent) {
                    processor.onExitCallRecord(recordingId, (ExitMethodRecordingEvent) event);
                } else if (event instanceof RecordingStartedEvent) {
                    processor.onRecordingStarted((RecordingStartedEvent) event);
                } else if (event instanceof RecordingFinishedEvent) {
                    processor.onRecordingFinished((RecordingFinishedEvent) event);
                }
                if (LoggingSettings.TRACE_ENABLED) {
                    log.trace("Event processed {} at seq {}", event, sequence);
                }
            }
        } finally {
            batch.reset();
        }
    }
}