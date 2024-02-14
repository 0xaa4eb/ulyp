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
    private final Map<Integer, RecordingEventHandler> recordingQueueProcessors = new HashMap<>();
    private final AtomicInteger running = new AtomicInteger(IDLE);
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
        running.set(HALTED);
        sequenceBarrier.alert();
    }

    @Override
    public boolean isRunning() {
        return running.get() != IDLE;
    }

    @Override
    public void run() {
        if (running.compareAndSet(IDLE, RUNNING)) {
            sequenceBarrier.clearAlert();

            try {
                if (running.get() == RUNNING) {
                    processEvents();
                }
            } finally {
                running.set(IDLE);
            }
        } else {
            // This is a little bit of guess work.  The running state could of changed to HALTED by
            // this point.  However, Java does not have compareAndExchange which is the only way
            // to get it exactly correct.
            if (running.get() == RUNNING) {
                throw new IllegalStateException("Thread is already running");
            }
        }
    }

    private void processEvents() {
        EventHolder eventHolder = null;
        long nextSequence = sequence.get() + 1L;

        while (true) {
            try {
                final long availableSequence = sequenceBarrier.waitFor(nextSequence);
                Set<Integer> recordingIds = new HashSet<>();

                while (nextSequence <= availableSequence)
                {
                    eventHolder = dataProvider.get(nextSequence);
                    Object event = eventHolder.event;
                    if (event instanceof EnterRecordQueueEvent) {
                        // TODO possible inline recording id and event type in item holder
                        EnterRecordQueueEvent enterRecord = (EnterRecordQueueEvent) event;
                        RecordingEventHandler processor = recordingQueueProcessors.get(enterRecord.getRecordingId());
                        if (recordingIds.add(enterRecord.getRecordingId())) {
                            processor.onEventBatchStart();
                        }
                        processor.onEnterCallRecord(enterRecord);
                    } else if (event instanceof ExitRecordQueueEvent) {
                        ExitRecordQueueEvent exitRecord = (ExitRecordQueueEvent) event;
                        RecordingEventHandler processor = recordingQueueProcessors.get(exitRecord.getRecordingId());
                        if (recordingIds.add(exitRecord.getRecordingId())) {
                            processor.onEventBatchStart();
                        }
                        processor.onExitCallRecord(exitRecord);
                    } else {
                        RecordingMetadataQueueEvent updateRecordingMetadataItem = (RecordingMetadataQueueEvent) event;
                        RecordingEventHandler processor = recordingQueueProcessors.get(updateRecordingMetadataItem.getRecordingMetadata().getId());
                        if (processor == null) {
                            recordingQueueProcessors.put(
                                updateRecordingMetadataItem.getRecordingMetadata().getId(),
                                processor = new RecordingEventHandler(typeResolver, agentDataWriter)
                            );
                        }
                        processor.onRecordingMetadataUpdate(updateRecordingMetadataItem);
                    }
                    if (LoggingSettings.TRACE_ENABLED) {
                        log.trace("Event processed {} at seq {}", event, nextSequence);
                    }

                    if (nextSequence == availableSequence) {
                        // handle batch end
                        for (Integer recordingId : recordingIds) {
                            RecordingEventHandler handler = recordingQueueProcessors.get(recordingId);
                            handler.onEventBatchEnd();
                            if (handler.isComplete()) {
                                recordingQueueProcessors.remove(recordingId);
                            }
                        }
                    }

                    nextSequence++;
                }

                sequence.set(availableSequence);
            } catch (final AlertException ex) {
                if (running.get() != RUNNING) {
                    break;
                }
            } catch (final Throwable ex) {
                handleEventException(ex, nextSequence, eventHolder);
                sequence.set(nextSequence);
                nextSequence++;
            }
        }
    }

    private void handleEventException(final Throwable ex, final long sequence, final EventHolder event) {
        // TODO handle somehow
    }
}