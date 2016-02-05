package com.example.android.fibonaccithreads.fibonacci;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * This class wraps the Fibonacci calculation into a Loader that can
 * reconnect to the Activity automatically and deliver the last result.
 */
public class FibonacciLoader extends AsyncTaskLoader<FibonacciResponse> {

    private long mFibNumber;
    private FibonacciResponse mCachedResponse;

    public FibonacciLoader(Context context, long n) {
        super(context);
        mFibNumber = n;
    }

    @Override
    protected void onStartLoading() {
        if (mCachedResponse != null) {
            //Deliver immediately without computing again
            deliverResult(mCachedResponse);
        } else {
            //Start a background computation
            forceLoad();
        }
    }

    @Override
    public FibonacciResponse loadInBackground() {
        //Run the blocking work in the background
        return FibLib.getInstance().calculate(mFibNumber);
    }

    @Override
    public void deliverResult(FibonacciResponse data) {
        //Save the result to deliver again, if we need to
        mCachedResponse = data;
        //Delegate handling back to the framework
        super.deliverResult(data);
    }
}
