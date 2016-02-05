package com.example.android.fibonaccithreads;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.fibonaccithreads.fibonacci.FibonacciLoader;
import com.example.android.fibonaccithreads.fibonacci.FibonacciResponse;

public class FibonacciActivity extends AppCompatActivity implements
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<FibonacciResponse> {
    private static final String TAG = FibonacciActivity.class.getSimpleName();

    /* Unique ID for our loader */
    private static final int LOADER_ID = 0;

    /* Handles to UI elements */
    private EditText mInputText;
    private TextView mOutputText;
    private RadioGroup mSelector;
    private ProgressBar mProgress;

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

        //Check for an existing load in progress
        Loader<FibonacciResponse> fibLoader =
                getSupportLoaderManager().getLoader(LOADER_ID);
        if (fibLoader != null && fibLoader.isStarted()) {
            //Reconnect with the existing load in progress
            startProgress();
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    /* Handler for Button click events */
    @Override
    public void onClick(View v) {
        try {
            //Obtain input value from UI
            long n = Long.parseLong(mInputText.getText().toString());

            //Show progress UI
            startProgress();
            //Start the loader with new arguments to calculate result
            Bundle args = new Bundle();
            args.putLong("fibNumber", n);
            getSupportLoaderManager().restartLoader(LOADER_ID, args, this);

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

    /* Loader callbacks for Fibonacci computation */

    @Override
    public Loader<FibonacciResponse> onCreateLoader(int id, Bundle args) {
        return new FibonacciLoader(this, args.getLong("fibNumber"));
    }

    @Override
    public void onLoadFinished(Loader<FibonacciResponse> loader, FibonacciResponse data) {
        //Display result in UI
        updateResultsUI(data);
    }

    @Override
    public void onLoaderReset(Loader<FibonacciResponse> loader) {
        mOutputText.setText(null);
    }
}
