package com.ulyp.agent.queue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.DataProvider;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.queue.events.EnterRecordQueueEvent;
import com.ulyp.agent.queue.events.ExitRecordQueueEvent;
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
    private final DataProvider<EventHolder> dataProvider;
    private final SequenceBarrier sequenceBarrier;
    private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);

    public QueueBatchEventProcessor(
        DataProvider<EventHolder> dataProvider,
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
                Set<Integer> recordingIds = new HashSet<>();

                while (nextSequence <= availableSequence) {
                    processAtSeq(nextSequence, recordingIds);

                    if (nextSequence == availableSequence) {
                        handleBatchEnd(recordingIds);
                    }
                    nextSequence++;
                }

                sequence.set(availableSequence);
            } catch (final AlertException ex) {
                if (status.get() != RUNNING) {
                    break;
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (final Throwable ex) {
                sequence.set(nextSequence);
                nextSequence++;
            }
        }
    }

    private void processAtSeq(long sequence, Set<Integer> recordingIds) {
        EventHolder eventHolder = dataProvider.get(sequence);
        Object event = eventHolder.event;
        eventHolder.event = null;
        if (event instanceof EnterRecordQueueEvent) {
            // TODO possible inline recording id and event type in item holder
            EnterRecordQueueEvent enterRecord = (EnterRecordQueueEvent) event;
            RecordingEventProcessor processor = recordingQueueProcessors.get(enterRecord.getRecordingId());
            if (recordingIds.add(enterRecord.getRecordingId())) {
                processor.onEventBatchStart();
            }
            processor.onEnterCallRecord(enterRecord);
        } else if (event instanceof ExitRecordQueueEvent) {
            ExitRecordQueueEvent exitRecord = (ExitRecordQueueEvent) event;
            RecordingEventProcessor processor = recordingQueueProcessors.get(exitRecord.getRecordingId());
            if (recordingIds.add(exitRecord.getRecordingId())) {
                processor.onEventBatchStart();
            }
            processor.onExitCallRecord(exitRecord);
        } else {
            RecordingMetadataQueueEvent updateRecordingMetadataItem = (RecordingMetadataQueueEvent) event;
            RecordingEventProcessor processor = recordingQueueProcessors.get(updateRecordingMetadataItem.getRecordingMetadata().getId());
            if (processor == null) {
                processor = new RecordingEventProcessor(typeResolver, agentDataWriter);
                recordingQueueProcessors.put(
                        updateRecordingMetadataItem.getRecordingMetadata().getId(),
                        processor
                );
            }
            processor.onRecordingMetadataUpdate(updateRecordingMetadataItem);
        }
        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Event processed {} at seq {}", event, sequence);
        }
    }

    private void handleBatchEnd(Set<Integer> recordingIds) {
        for (Integer recordingId : recordingIds) {
            RecordingEventProcessor handler = recordingQueueProcessors.get(recordingId);
            handler.onEventBatchEnd();
            if (handler.isComplete()) {
                recordingQueueProcessors.remove(recordingId);
            }
        }
    }
}