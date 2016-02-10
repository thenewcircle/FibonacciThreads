package com.example.android.fibonaccithreads.fibonacci;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class accepts logging messages, and writes them to a file in a
 * queued background thread.
 */
public class FileLoggingHandler implements Handler.Callback {
    private static final String TAG = FileLoggingHandler.class.getSimpleName();

    /* Local message handler and background Looper thread */
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    /* Wrapper to write file data */
    private FileWriter mFileWriter;

    public FileLoggingHandler(File destinationFile) throws IOException {
        mFileWriter = new FileWriter(destinationFile);

        //Create a background Looper thread to process log requests
        mHandlerThread = new HandlerThread(TAG,
                Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        //Attach a handler to the new thread, with this object as the callback
        mHandler = new Handler(mHandlerThread.getLooper(), this);
    }

    /*
     * Implementation of Handler.Callback interface.
     * Receives every Message passed to our internal Handler, and is
     * therefore executed on the attached background HandlerThread.
     */
    @Override
    public boolean handleMessage(Message msg) {
        String logMessage = (String) msg.obj;
        try {
            mFileWriter.write(logMessage);
            mFileWriter.flush();
        } catch (IOException e) {
            Log.w(TAG, "Unable to write log message", e);
        }
        //Indicate that we've handled the message
        return true;
    }

    /**
     * Post a log message to this handler's destination file.
     *
     * @param tag Identifier for the source of the message.
     * @param message The message you would like logged.
     */
    public void logMessage(String tag, String message) {
        //Format the incoming strings as a log message
        String logMessage = String.format("%s: %s\n",
                tag.replaceAll("\n", " "),
                message.replaceAll("\n", " "));
        //Post a message to the background thread
        Message msg = mHandler.obtainMessage(0, logMessage);
        msg.sendToTarget();
    }

    /**
     * Call this method to tear down the logging handler and it's
     * associated thread.
     *
     * @return true if successful in asking the underlying Looper to quit
     */
    public boolean shutdown() {
        Log.i(TAG, "Shutting down file handler");

        try {
            mFileWriter.close();
        } catch (IOException e) {
            Log.w(TAG, "Unable to close file", e);
        }
        return mHandlerThread.quit();
    }
}
