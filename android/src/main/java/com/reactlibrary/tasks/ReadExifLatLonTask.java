package com.reactlibrary.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.reactlibrary.utils.Utils;

import java.io.File;

public class ReadExifLatLonTask extends AsyncTask<Integer, Integer, WritableMap> {

    private static final String E_LAT_LON_EXIF_ERROR = "E_LAT_LON_EXIF_ERROR";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private Exception exception;

    private File file;

    private Callback errorCallback;
    private Callback successCallback;
    private Promise promise;

    public ReadExifLatLonTask(@NonNull File file,
                            @Nullable Callback errorCallback, @Nullable Callback successCallback,
                            @Nullable Promise promise) {
        this.file = file;

        this.errorCallback = errorCallback;
        this.successCallback = successCallback;
        this.promise = promise;
    }

    @Override
    protected WritableMap doInBackground(Integer... integers) {
        if (this.file != null) {
            try {
                float[] latlon = Utils.getExifLatLon(this.file);
                if (latlon != null && latlon.length == 2) {
                    WritableMap map = new WritableNativeMap();
                    map.putDouble(ReadExifLatLonTask.LAT, Float.valueOf(latlon[0]).doubleValue());
                    map.putDouble(ReadExifLatLonTask.LON, Float.valueOf(latlon[1]).doubleValue());
                    return map;
                }
            } catch (Exception e) {
                this.exception = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(WritableMap latlon) {
        if (latlon != null) {
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
                        ReadExifLatLonTask.E_LAT_LON_EXIF_ERROR + (
                                this.exception != null ? " " + this.exception.getMessage() : ""
                        )
                );
            }
        }
    }
}
