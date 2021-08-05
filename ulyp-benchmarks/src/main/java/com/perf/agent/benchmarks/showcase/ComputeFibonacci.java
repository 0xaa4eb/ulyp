package com.perf.agent.benchmarks.showcase;

public class ComputeFibonacci {

    public static int compute(int n) {
        if (n <= 1)
            return n;
        return compute(n - 1) + compute(n - 2);
    }

    public static void main(String[] args) {
        System.out.println(compute(7));
    }
}
