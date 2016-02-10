package com.example.android.fibonaccithreads.fibonacci;

/**
 * This class contains the results of a Fibonacci calculation
 */
public class FibonacciResponse {

    /**
     * Requested sequence number for this computation
     */
    public final long n;
    /**
     * Result of the computation
     */
    public final long result;
    /**
     * Duration, in milliseconds, of the computation
     */
    public final long computeTime;

    public FibonacciResponse(long n, long result, long computeTime) {
        this.n = n;
        this.result = result;
        this.computeTime = computeTime;
    }

    @Override
    public String toString() {
        return String.format("N: %d\tResult: %d\nTime: %.3f", this.n,
                this.result, this.computeTime / 1000f);
    }
}
