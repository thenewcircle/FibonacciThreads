package com.example.android.fibonaccithreads;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.fibonaccithreads.fibonacci.FibLib;
import com.example.android.fibonaccithreads.fibonacci.FibonacciResponse;

import java.io.File;
import java.io.IOException;

public class FibonacciActivity extends AppCompatActivity implements
        View.OnClickListener {
    private static final String TAG = FibonacciActivity.class.getSimpleName();

    /* Handles to UI elements */
    private EditText mInputText;
    private TextView mOutputText;
    private RadioGroup mSelector;
    private ProgressBar mProgress;

    /* Computation class for Fibonacci numbers */
    private FibLib mFibLib;

    private ArrayAdapter<FibonacciResponse> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fibonacci);

        //Gather UI elements
        mInputText = (EditText) findViewById(R.id.input);
        mOutputText = (TextView) findViewById(R.id.output);
        mSelector = (RadioGroup) findViewById(R.id.selector);
        mProgress = (ProgressBar) findViewById(R.id.progress);

        //Wire UI actions
        findViewById(R.id.button).setOnClickListener(this);

        mFibLib = FibLib.getInstance();

        mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(mAdapter);
    }

    /* Asynchronous cases with thread and task */
    private FibLib.OnFibResultListener mFibResultListener = new FibLib.OnFibResultListener() {
        @Override
        public void onFibResult(FibonacciResponse response) {
            //Update the UI
            updateResultsUI(response);
        }

        @Override
        public void onActiveStatusChanged(boolean isActive) {
            if (isActive) {
                startProgress();
            } else {
                stopProgress();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        //Attach callback
        mFibLib.setOnFibResultListener(mFibResultListener);
        try {
            //Create a log file on external storage
            File logFile = new File(getExternalCacheDir(), "fibonacci.log");
            //Attach it to the library
            mFibLib.setLogFile(logFile);
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Detach callback to avoid leaks
        mFibLib.setOnFibResultListener(null);
        try {
            //Clear the file logger
            mFibLib.setLogFile(null);
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

    /* Handler for Button click events */
    @Override
    public void onClick(View v) {
        try {
            //Obtain input value from UI
            long n = Long.parseLong(mInputText.getText().toString());

            //Queue up operations for all numbers below the input value
            switch (mSelector.getCheckedRadioButtonId()) {
                case R.id.option_thread:
                    for (long i=n; i > 0; i--) {
                        mFibLib.calculateInThread(i);
                    }
                    break;
                case R.id.option_task:
                    for (long i=n; i > 0; i--) {
                        mFibLib.calculateAsyncTask(i);
                    }
                    break;
                default:
                    //Do nothing
                    break;
            }

        } catch (NumberFormatException e) {
            //Show an error message
            Toast.makeText(this, R.string.input_error, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /* Used to estimate overall time of each request */
    private long mStartTime;
    private long mEndTime;

    /* Show progress spinner and clear result text */
    private void startProgress() {
        //Show progress
        mProgress.setVisibility(View.VISIBLE);

        mStartTime = mEndTime = System.currentTimeMillis();
        mOutputText.setText(null);
        mAdapter.clear();
    }

    /* Hide progress spinner */
    private void stopProgress() {
        //Hide progress
        mProgress.setVisibility(View.INVISIBLE);
    }

    /* Hide progress spinner and set result text */
    private void updateResultsUI(FibonacciResponse response) {
        //Display result in UI
        mEndTime = System.currentTimeMillis();
        long delta = (mEndTime - mStartTime);
        mOutputText.setText(getString(R.string.timing_result,
                FibLib.PROCESSOR_CORES, delta / 1000f));
        mAdapter.add(response);
    }
}
