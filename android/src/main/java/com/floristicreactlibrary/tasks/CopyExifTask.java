package com.floristicreactlibrary.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.floristicreactlibrary.utils.Utils;

import java.io.File;

public class CopyExifTask extends AsyncTask<Integer, Integer, Boolean> {

    private static final String MODULE_NAME = "CopyExifTask";
    private static final String E_COPY_EXIF_ERROR = "E_COPY_EXIF_ERROR";

    private File srcFile;
    private File destFile;

    private Callback errorCallback;
    private Callback successCallback;
    private Promise promise;

    private Exception exception;
    private Error error;

    public CopyExifTask(@NonNull File srcFile, @NonNull File destFile,
                        @Nullable Callback errorCallback, @Nullable Callback successCallback,
                        @Nullable Promise promise) {
        this.srcFile = srcFile;
        this.destFile = destFile;

        this.errorCallback = errorCallback;
        this.successCallback = successCallback;
        this.promise = promise;
    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
        Log.d(CopyExifTask.MODULE_NAME, "running");

        if (this.srcFile != null && this.destFile != null) {
            try {
                return Utils.copyExifData(this.srcFile, this.destFile, null, true);
            } catch (Exception e) {
                this.exception = e;
            } catch (Error e) {
                this.error = e;
            }
        } else {
            Log.d(CopyExifTask.MODULE_NAME, "failed: missing file(s)");
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean succeeded) {
        Log.d(CopyExifTask.MODULE_NAME, "ending");
        Log.d(CopyExifTask.MODULE_NAME, "succeeded: " + (succeeded != null ? succeeded : "null"));
        Log.d(CopyExifTask.MODULE_NAME, "exception: " + (this.exception != null ? this.exception.getMessage() : "null"));
        Log.d(CopyExifTask.MODULE_NAME, "error: " + (this.error != null ? this.error.getMessage() : "null"));

        if (succeeded != null && succeeded) {
            if (this.promise != null) {
                this.promise.resolve(succeeded);
            } else if (this.successCallback != null) {
                this.successCallback.invoke(succeeded);
            }
        } else {
            if (this.promise != null) {
                if (this.exception != null) {
                    this.promise.reject(CopyExifTask.E_COPY_EXIF_ERROR, this.exception);
                } else if (this.error != null) {
                    this.promise.reject(CopyExifTask.E_COPY_EXIF_ERROR, this.error);
                }
            } else if (this.errorCallback != null) {
                if (this.exception != null) {
                    this.errorCallback.invoke(
                            CopyExifTask.E_COPY_EXIF_ERROR + (
                                    this.exception.getMessage()
                            )
                    );
                } else if (this.error != null) {
                    this.errorCallback.invoke(
                            CopyExifTask.E_COPY_EXIF_ERROR + (
                                    this.error.getMessage()
                            )
                    );
                } else {
                    this.errorCallback.invoke(
                            CopyExifTask.E_COPY_EXIF_ERROR + ("")
                    );
                }
            }
        }
    }
}
