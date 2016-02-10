package com.example.android.fibonaccithreads.fibonacci;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class calculates Fibonacci sequence values and returns their results.
 */
public class FibLib {
    private static final String TAG = FibLib.class.getSimpleName();

    /* Constant providing number of cores on the system */
    public static final int PROCESSOR_CORES =
            Runtime.getRuntime().availableProcessors();

    /* Callback interface for Fibonacci results */
    public interface OnFibResultListener {
        /**
         * Called when a Fibonacci computation is complete.
         *
         * @param response Response containing results
         */
        void onFibResult(FibonacciResponse response);

        /**
         * Called when a change in background activity is detected.
         *
         * @param isActive true when work is currently in process, false
         *                 when idle.
         */
        void onActiveStatusChanged(boolean isActive);
    }

    /* Local callback for asynchronous results */
    private OnFibResultListener mOnFibResultListener;

    /* Local executor for handling incoming work */
    private ThreadPoolExecutor mExecutor;

    /* Local handler for processing file log requests */
    private FileLoggingHandler mLoggingHandler;

    /* FibLib is a singleton */
    private static final FibLib INSTANCE = new FibLib();
    private FibLib() {
        //Construct a thread pool with a fixed size (2x available cores)
        mExecutor = new ThreadPoolExecutor(
                PROCESSOR_CORES * 2, PROCESSOR_CORES * 2, //min, max
                1, TimeUnit.SECONDS,                      //timeout
                new LinkedBlockingQueue<Runnable>()       //queue
        );
    }
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

    /* Wrapper to pass a status change to the attached callback */
    private void deliverRunStatus(boolean isActive) {
        if (mOnFibResultListener != null) {
            mOnFibResultListener.onActiveStatusChanged(isActive);
        } else {
            Log.w(TAG, "Unable to deliver status callback. Listener detached.");
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
            //Log the result to a file
            logEvent(TAG, mResponse.toString());
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
        //Start a progress tracker
        trackProgress();

        Runnable work = new ComputeWork(n, mHandler);
        //Start a new thread to handle our background work
        mExecutor.execute(work);
    }

    /* AsyncTask to perform block work in the background */
    private class ComputeTask extends AsyncTask<Long, Void, FibonacciResponse> {

        @Override
        protected FibonacciResponse doInBackground(Long... params) {
            final long n = params[0];

            //Execute computation in the background thread
            //Let AsyncTask hand the result back to the main thread
            return calculate(n);
        }

        @Override
        protected void onPostExecute(FibonacciResponse response) {
            //Deliver response to an attached callback
            deliverCallbackResult(response);
        }
    }

    /**
     * Calculate the Fibonacci number using AsyncTask
     *
     * @param n Sequence index of Fibonacci number
     */
    public void calculateAsyncTask(long n) {
        //Start a progress tracker
        trackProgress();

        new ComputeTask().executeOnExecutor(mExecutor, n);
    }

    /**
     * Return the number of remaining tasks, which includes tasks in the
     * queue and actively in process.
     */
    public int getRemainingTasks() {
        return mExecutor.getQueue().size() + mExecutor.getActiveCount();
    }

    /**
     * Return an estimation of whether the Executor is currently processing
     * any incoming work.
     */
    public boolean isActive() {
        return getRemainingTasks() > 0;
    }

    /* Start tracking the Executor state, if we aren't already */
    private void trackProgress() {
        if (!isActive()) {
            deliverRunStatus(true);
            mHandler.post(new ProgressWork());
        }
    }

    /* Runnable to encapsulate polling of running status */
    private class ProgressWork implements Runnable {

        @Override
        public void run() {
            //Poll the active status of the Executor
            if (isActive()) {
                //Check again later
                mHandler.postDelayed(this, 500);
            } else {
                //Notify the listener of the change
                deliverRunStatus(false);
            }
        }
    }

    /**
     * Create a new log handler to route logging events to the provided
     * file destination.
     *
     * @param logFile Destination file for logged events.
     *
     * @throws IOException if unable to open the requested file.
     */
    public void setLogFile(File logFile) throws IOException {
        if (mLoggingHandler != null) {
            //Shut down the current handler
            mLoggingHandler.shutdown();
            mLoggingHandler = null;
        }

        if (logFile != null) {
            mLoggingHandler = new FileLoggingHandler(logFile);
        }
    }

    /* Pass log message requests down to the handler */
    private void logEvent(String tag, String message) {
        if (mLoggingHandler != null) {
            mLoggingHandler.logMessage(tag, message);
        }
    }
}
