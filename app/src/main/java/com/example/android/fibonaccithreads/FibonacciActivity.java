package com.example.android.fibonaccithreads;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.fibonaccithreads.fibonacci.FibLib;
import com.example.android.fibonaccithreads.fibonacci.FibonacciResponse;

public class FibonacciActivity extends AppCompatActivity implements
        View.OnClickListener {

    /* Handles to UI elements */
    private EditText mInputText;
    private TextView mOutputText;
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
        mProgress = (ProgressBar) findViewById(R.id.progress);

        //Wire UI actions
        findViewById(R.id.button).setOnClickListener(this);

        mFibLib = FibLib.getInstance();
    }

    /* Handler for Button click events */
    @Override
    public void onClick(View v) {
        try {
            //Show progress UI
            startProgress();

            //Obtain input value from UI
            long n = Long.parseLong(mInputText.getText().toString());
            //Calculate result
            FibonacciResponse response = mFibLib.calculate(n);

            //Display result in UI
            setResultText(response);
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

    /* Hide progress spinner and set result text */
    private void setResultText(FibonacciResponse response) {
        //Display result in UI
        String result = getString(R.string.output_result,
                response.result, response.computeTime / 1000f);
        mOutputText.setText(result);

        //Hide progress
        mProgress.setVisibility(View.INVISIBLE);
    }
}