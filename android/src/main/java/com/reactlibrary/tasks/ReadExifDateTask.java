package com.reactlibrary.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.reactlibrary.utils.Utils;

import java.io.File;

public class ReadExifDateTask extends AsyncTask<Integer, Integer, String> {

    private static final String E_DATE_EXIF_ERROR = "E_DATE_EXIF_ERROR";
    private Exception exception;

    private File file;

    private Callback errorCallback;
    private Callback successCallback;
    private Promise promise;

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
        if (this.file != null) {
            try {
                return Utils.getExifDate(this.file);
            } catch (Exception e) {
                this.exception = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String date) {
        if (date != null) {
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
                        ReadExifDateTask.E_DATE_EXIF_ERROR + (
                                this.exception != null ? " " + this.exception.getMessage() : ""
                        )
                );
            }
        }
    }
}
