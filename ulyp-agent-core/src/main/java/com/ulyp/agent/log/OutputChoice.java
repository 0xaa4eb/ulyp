package com.ulyp.agent.log;

import java.io.PrintStream;

/**
 * Forked from slf4j-simple
 */
class OutputChoice {

    enum OutputChoiceType {
        SYS_OUT, CACHED_SYS_OUT, SYS_ERR, CACHED_SYS_ERR, FILE;
    }

    final OutputChoiceType outputChoiceType;
    final PrintStream targetPrintStream;

    OutputChoice(OutputChoiceType outputChoiceType) {
        if (outputChoiceType == OutputChoiceType.FILE) {
            throw new IllegalArgumentException();
        }
        this.outputChoiceType = outputChoiceType;
        if (outputChoiceType == OutputChoiceType.CACHED_SYS_OUT) {
            this.targetPrintStream = System.out;
        } else if (outputChoiceType == OutputChoiceType.CACHED_SYS_ERR) {
            this.targetPrintStream = System.err;
        } else {
            this.targetPrintStream = null;
        }
    }

    OutputChoice(PrintStream printStream) {
        this.outputChoiceType = OutputChoiceType.FILE;
        this.targetPrintStream = printStream;
    }

    PrintStream getTargetPrintStream() {
        switch (outputChoiceType) {
        case SYS_OUT:
            return System.out;
        case SYS_ERR:
            return System.err;
        case CACHED_SYS_ERR:
        case CACHED_SYS_OUT:
        case FILE:
            return targetPrintStream;
        default:
            throw new IllegalArgumentException();
        }

    }

}
