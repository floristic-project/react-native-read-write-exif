package com.floristicreactlibrary.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.floristicreactlibrary.utils.Utils;

import java.io.File;
import java.lang.ref.WeakReference;

public class ReadExifLatLonTask extends AsyncTask<Integer, Integer, WritableMap> {

    private static final String E_LAT_LON_EXIF_ERROR = "E_LAT_LON_EXIF_ERROR";
    private static final String LAT = "lat";
    private static final String LON = "lon";

    private WeakReference<Context> context;

    private File file;

    private Callback errorCallback;
    private Callback successCallback;
    private Promise promise;

    private Exception exception;

    public ReadExifLatLonTask(Context context, @NonNull File file,
                            @Nullable Callback errorCallback, @Nullable Callback successCallback,
                            @Nullable Promise promise) {
        this.context = new WeakReference<>(context);

        this.file = file;

        this.errorCallback = errorCallback;
        this.successCallback = successCallback;
        this.promise = promise;
    }

    @Override
    protected WritableMap doInBackground(Integer... integers) {
        Log.e("ReadExifLatLonTask", "running");

        if (this.file != null) {
            try {
                Context ctx = this.context.get();
                if (ctx != null) {
                    double[] latlon = Utils.getExifLatLon(ctx, this.file);

                    Log.e("ReadExifLatLonTask", "latlon: " + (latlon != null ? latlon : "null"));

                    if (latlon != null && latlon.length == 2) {
                        WritableMap map = new WritableNativeMap();
                        map.putDouble(ReadExifLatLonTask.LAT, latlon[0]);
                        map.putDouble(ReadExifLatLonTask.LON, latlon[1]);
                        return map;
                    }
                } else {
                    Log.e("ReadExifLatLonTask", "failed: missing context");
                }
            } catch (Exception e) {
                this.exception = e;
            }
        } else {
            Log.e("ReadExifLatLonTask", "failed: missing file");
        }

        return null;
    }

    @Override
    protected void onPostExecute(WritableMap latlon) {
        Log.e("ReadExifLatLonTask", "ending");
        Log.e("ReadExifLatLonTask", "latlon: " + (latlon != null ? latlon.toString() : "null"));
        Log.e("ReadExifLatLonTask", "exception: " + (this.exception != null ? this.exception.getMessage() : "null"));

        if (this.exception == null) {
            if (this.promise != null) {
                this.promise.resolve(latlon);
            } else if (this.successCallback != null) {
                this.successCallback.invoke(latlon);
            }
        } else {
            if (this.promise != null) {
                this.promise.reject(ReadExifLatLonTask.E_LAT_LON_EXIF_ERROR, this.exception);
            } else if (this.errorCallback != null) {
                this.errorCallback.invoke(
                        ReadExifLatLonTask.E_LAT_LON_EXIF_ERROR + this.exception.getMessage()
                );
            }
        }
    }
}
