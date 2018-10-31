package com.floristicreactlibrary.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.floristicreactlibrary.utils.Utils;

import java.io.File;

public class ReadExifDateTask extends AsyncTask<Integer, Integer, String> {

    private static final String E_DATE_EXIF_ERROR = "E_DATE_EXIF_ERROR";

    private File file;

    private Callback errorCallback;
    private Callback successCallback;
    private Promise promise;

    private Exception exception;

    public ReadExifDateTask(@NonNull File file,
                        @Nullable Callback errorCallback, @Nullable Callback successCallback,
                        @Nullable Promise promise) {
        this.file = file;

        this.errorCallback = errorCallback;
        this.successCallback = successCallback;
        this.promise = promise;
    }

    @Override
    protected String doInBackground(Integer... integers) {
        Log.d("ReadExifDateTask", "running");

        if (this.file != null) {
            try {
                return Utils.getExifDate(this.file);
            } catch (Exception e) {
                this.exception = e;
            }
        }

        Log.d("ReadExifDateTask", "failed: missing file");

        return null;
    }

    @Override
    protected void onPostExecute(String date) {
        Log.d("ReadExifDateTask", "ending");
        Log.d("ReadExifDateTask", "date: " + (date != null ? date : "null"));
        Log.d("ReadExifDateTask", "exception: " + (this.exception != null ? this.exception.getMessage() : "null"));

        if (this.exception == null) {
            if (this.promise != null) {
                this.promise.resolve(date);
            } else if (this.successCallback != null) {
                this.successCallback.invoke(date);
            }
        } else {
            if (this.promise != null) {
                this.promise.reject(ReadExifDateTask.E_DATE_EXIF_ERROR, this.exception);
            } else if (this.errorCallback != null) {
                this.errorCallback.invoke(
                        ReadExifDateTask.E_DATE_EXIF_ERROR + this.exception.getMessage()
                );
            }
        }
    }
}
