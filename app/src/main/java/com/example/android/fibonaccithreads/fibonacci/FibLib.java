package com.example.android.fibonaccithreads.fibonacci;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

/**
 * This class calculates Fibonacci sequence values and returns their results.
 */
public class FibLib {
    private static final String TAG = FibLib.class.getSimpleName();

    /* Callback interface for Fibonacci results */
    public interface OnFibResultListener {
        /**
         * Called when a Fibonacci computation is complete.
         *
         * @param response Response containing results
         */
        void onFibResult(FibonacciResponse response);
    }

    /* Local callback for asynchronous results */
    private OnFibResultListener mOnFibResultListener;

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

    /**
     * Set the current Fibonacci result callback.
     *
     * @param listener Callback to invoke, or null to remove a callback
     */
    public void setOnFibResultListener(OnFibResultListener listener) {
        mOnFibResultListener = listener;
    }

    /* Wrapper to pass a result to the attached callback */
    private void deliverCallbackResult(FibonacciResponse response) {
        if (mOnFibResultListener != null) {
            mOnFibResultListener.onFibResult(response);
        } else {
            Log.w(TAG, "Unable to deliver Fibonacci callback. Listener detached.");
        }
    }

    /* Handler instance bound to the main thread via getMainLooper() */
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /* Runnable to encapsulate main thread callback */
    private class CallbackWork implements Runnable {
        private FibonacciResponse mResponse;

        public CallbackWork(FibonacciResponse response) {
            mResponse = response;
        }

        @Override
        public void run() {
            //Deliver the callback on the main thread
            deliverCallbackResult(mResponse);
        }
    }

    /* Runnable to encapsulate the blocking work */
    private class ComputeWork implements Runnable {
        private final long mFibNumber;
        private final Handler mHandler;

        public ComputeWork(long n, Handler handler) {
            mFibNumber = n;
            mHandler = handler;
        }

        /* Code in this method will be on a background thread */
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            //Execute computation in the background thread
            FibonacciResponse response = calculate(mFibNumber);

            //Post the response back to the main thread
            mHandler.post(new CallbackWork(response));
        }
    }

    /**
     * Calculate the Fibonacci number in a background thread
     *
     * @param n Sequence index of Fibonacci number
     */
    public void calculateInThread(long n) {
        Runnable work = new ComputeWork(n, mHandler);
        //Start a new thread to handle our background work
        new Thread(work).start();
    }
}
