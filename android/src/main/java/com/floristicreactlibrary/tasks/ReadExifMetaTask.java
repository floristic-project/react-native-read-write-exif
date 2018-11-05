package com.floristicreactlibrary.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.floristicreactlibrary.utils.Utils;

import java.io.File;

public class ReadExifMetaTask extends AsyncTask<Integer, Integer, String> {

    private static final String MODULE_NAME = "ReadExifMetaTask";
    private static final String E_META_EXIF_ERROR = "E_META_EXIF_ERROR";

    private File file;

    private Callback errorCallback;
    private Callback successCallback;
    private Promise promise;

    private Exception exception;

    public ReadExifMetaTask(@NonNull File file,
                            @Nullable Callback errorCallback, @Nullable Callback successCallback,
                            @Nullable Promise promise) {
        this.file = file;

        this.errorCallback = errorCallback;
        this.successCallback = successCallback;
        this.promise = promise;
    }

    @Override
    protected String doInBackground(Integer... integers) {
        Log.e(ReadExifMetaTask.MODULE_NAME, "running");

        if (this.file != null) {
            try {
                return Utils.readMetadata(this.file);
            } catch (Exception e) {
                this.exception = e;
            }
        } else {
            Log.e(ReadExifMetaTask.MODULE_NAME, "failed: missing file");
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.e(ReadExifMetaTask.MODULE_NAME, "ending");
        Log.e(ReadExifMetaTask.MODULE_NAME, "result: " + (result != null ? result : "null"));
        Log.e(ReadExifMetaTask.MODULE_NAME, "exception: " + (this.exception != null ? this.exception.getMessage() : "null"));

        if (this.exception == null) {
            if (this.promise != null) {
                this.promise.resolve(result);
            } else if (this.successCallback != null) {
                this.successCallback.invoke(result);
            }
        } else {
            if (this.promise != null) {
                this.promise.reject(ReadExifMetaTask.E_META_EXIF_ERROR, this.exception);
            } else if (this.errorCallback != null) {
                this.errorCallback.invoke(
                        ReadExifMetaTask.E_META_EXIF_ERROR + this.exception.getMessage()
                );
            }
        }
    }
}
