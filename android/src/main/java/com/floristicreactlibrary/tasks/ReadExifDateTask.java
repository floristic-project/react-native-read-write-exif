package com.floristicreactlibrary.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.floristicreactlibrary.utils.Utils;

import java.io.File;
import java.lang.ref.WeakReference;

public class ReadExifDateTask extends AsyncTask<Integer, Integer, String> {

    private static final String E_DATE_EXIF_ERROR = "E_DATE_EXIF_ERROR";

    private WeakReference<Context> context;

    private File file;

    private Callback errorCallback;
    private Callback successCallback;
    private Promise promise;

    private Exception exception;

    public ReadExifDateTask(Context context, @NonNull File file,
                        @Nullable Callback errorCallback, @Nullable Callback successCallback,
                        @Nullable Promise promise) {
        this.context = new WeakReference<>(context);

        this.file = file;

        this.errorCallback = errorCallback;
        this.successCallback = successCallback;
        this.promise = promise;
    }

    @Override
    protected String doInBackground(Integer... integers) {
        Log.e("ReadExifDateTask", "running");

        if (this.file != null) {
            try {
                Context ctx = this.context.get();
                if (ctx != null) {
                    return Utils.getExifDate(ctx, this.file);
                } else {
                    Log.e("ReadExifDateTask", "failed: missing context");
                }
            } catch (Exception e) {
                this.exception = e;
            }
        } else {
            Log.e("ReadExifDateTask", "failed: missing file");
        }

        return null;
    }

    @Override
    protected void onPostExecute(String date) {
        Log.e("ReadExifDateTask", "ending");
        Log.e("ReadExifDateTask", "date: " + (date != null ? date : "null"));
        Log.e("ReadExifDateTask", "exception: " + (this.exception != null ? this.exception.getMessage() : "null"));

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
