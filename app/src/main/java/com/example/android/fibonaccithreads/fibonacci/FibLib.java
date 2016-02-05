package com.example.android.fibonaccithreads.fibonacci;

import android.util.Log;

/**
 * This class calculates Fibonacci sequence values and returns their results.
 */
public class FibLib {
    private static final String TAG = FibLib.class.getSimpleName();

    /* FibLib is a singleton */
    private static final FibLib INSTANCE = new FibLib();
    private FibLib() { }
    public static FibLib getInstance() {
        return INSTANCE;
    }

    //Internal helper method for recursive computation
    private long fib(long n) {
        return n <= 0 ? 0 : n == 1 ? 1 : fib(n - 1) + fib(n - 2);
    }

    /**
     * Calculate the Fibonacci number to the given sequence number.
     *
     * @param n Sequence index of Fibonacci number
     *
     * @return the calculated Fibonacci number
     */
    public FibonacciResponse calculate(long n) {
        Log.d(TAG, "fibonacci(" + n + ")");
        long now = System.currentTimeMillis();
        long result = fib(n);

        return new FibonacciResponse(n, result, System.currentTimeMillis() - now);
    }

}
