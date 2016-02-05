package com.example.android.fibonaccithreads;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.fibonaccithreads.fibonacci.FibLib;
import com.example.android.fibonaccithreads.fibonacci.FibonacciResponse;

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
    }

    /* Asynchronous cases with thread and task */
    private FibLib.OnFibResultListener mFibResultListener = new FibLib.OnFibResultListener() {
        @Override
        public void onFibResult(FibonacciResponse response) {
            //Update the UI
            updateResultsUI(response);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        //Attach callback
        mFibLib.setOnFibResultListener(mFibResultListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Detach callback to avoid leaks
        mFibLib.setOnFibResultListener(null);
    }

    /* Handler for Button click events */
    @Override
    public void onClick(View v) {
        try {
            //Obtain input value from UI
            long n = Long.parseLong(mInputText.getText().toString());

            //Show progress UI
            startProgress();
            //Calculate result
            switch (mSelector.getCheckedRadioButtonId()) {
                case R.id.option_thread:
                    mFibLib.calculateInThread(n);
                    break;
                case R.id.option_task:
                    mFibLib.calculateAsyncTask(n);
                    break;
                default:
                    //Do nothing
                    stopProgress();
                    break;
            }

        } catch (NumberFormatException e) {
            //Show an error message
            Toast.makeText(this, R.string.input_error, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /* Show progress spinner and clear result text */
    private void startProgress() {
        //Show progress
        mProgress.setVisibility(View.VISIBLE);
        mOutputText.setText(null);
    }

    /* Hide progress spinner */
    private void stopProgress() {
        //Hide progress
        mProgress.setVisibility(View.INVISIBLE);
    }

    /* Hide progress spinner and set result text */
    private void updateResultsUI(FibonacciResponse response) {
        //Display result in UI
        String result = getString(R.string.output_result,
                response.result, response.computeTime / 1000f);
        mOutputText.setText(result);

        //Hide progress
        stopProgress();
    }
}
