package com.ulyp.agent.queue.disruptor;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.WorkHandler;
import com.ulyp.agent.queue.RecordingEventDisruptorEntry;

import java.util.Arrays;

public class EventHandlerGroup {
    private final RecordingQueueDisruptor disruptor;
    private final ConsumerRepository<RecordingEventDisruptorEntry> consumerRepository;
    private final Sequence[] sequences;

    EventHandlerGroup(
            final RecordingQueueDisruptor disruptor,
            final ConsumerRepository<RecordingEventDisruptorEntry> consumerRepository,
            final Sequence[] sequences) {
        this.disruptor = disruptor;
        this.consumerRepository = consumerRepository;
        this.sequences = Arrays.copyOf(sequences, sequences.length);
    }

    public EventHandlerGroup and(final EventHandlerGroup otherHandlerGroup) {
        final Sequence[] combinedSequences = new Sequence[this.sequences.length + otherHandlerGroup.sequences.length];
        System.arraycopy(this.sequences, 0, combinedSequences, 0, this.sequences.length);
        System.arraycopy(
                otherHandlerGroup.sequences, 0,
                combinedSequences, this.sequences.length, otherHandlerGroup.sequences.length);
        return new EventHandlerGroup(disruptor, consumerRepository, combinedSequences);
    }

    public EventHandlerGroup and(final EventProcessor... processors) {
        Sequence[] combinedSequences = new Sequence[sequences.length + processors.length];

        for (int i = 0; i < processors.length; i++) {
            consumerRepository.add(processors[i]);
            combinedSequences[i] = processors[i].getSequence();
        }
        System.arraycopy(sequences, 0, combinedSequences, processors.length, sequences.length);

        return new EventHandlerGroup(disruptor, consumerRepository, combinedSequences);
    }

    @SafeVarargs
    public final EventHandlerGroup handleEventsWithWorkerPool(final WorkHandler<RecordingEventDisruptorEntry>... handlers) {
        return disruptor.createWorkerPool(sequences, handlers);
    }
}
