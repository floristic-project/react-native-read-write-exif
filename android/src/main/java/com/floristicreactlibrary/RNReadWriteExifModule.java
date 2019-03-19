
package com.floristicreactlibrary;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.floristicreactlibrary.tasks.CopyExifTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/*
 * A native module is a Java class that usually extends the ReactContextBaseJavaModule class and
 * implements the functionality required by the JavaScript.
 *
 * To make it simpler to access your new functionality from JavaScript, it is common to wrap the
 * native module in a JavaScript module. This is not necessary but saves the consumers of your
 * library the need to pull it off of NativeModules each time. This JavaScript file also becomes
 * a good location for you to add any JavaScript side functionality.
 */
public class RNReadWriteExifModule extends ReactContextBaseJavaModule {

    private static final String E_READ_SRC_FILE_ERROR = "E_READ_SRC_FILE_ERROR";
    private static final String E_READ_DEST_FILE_ERROR = "E_READ_DEST_FILE_ERROR";

    /*
     * The following argument types are supported for methods annotated with @ReactMethod and they
     * directly map to their JavaScript equivalents.
     *
     * Boolean -> Bool
     * Integer -> Number
     * Double -> Number
     * Float -> Number
     * String -> String
     * Callback -> function
     * ReadableMap -> Object
     * ReadableArray -> Array
     */

    private final ReactApplicationContext reactContext;

    @SuppressWarnings("WeakerAccess")
    public RNReadWriteExifModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

  /*
    * The purpose of this method is to return the string name of the NativeModule which represents
    * this class in JavaScript. So here we will call this ToastExample so that we can access it
    * through React.NativeModules.ToastExample in JavaScript.
    */
    @Override
    public String getName() {
        return "RNReadWriteExif";
    }

    /*
    * An optional method called getConstants returns the constant values exposed to JavaScript.
    * Its implementation is not required but is very useful to key pre-defined values that need to
    * be communicated from JavaScript to Java in sync.
    */
    @Override
    public Map<String, Object> getConstants() {
        return new HashMap<>();
    }

    /*
     * To expose a method to JavaScript a Java method must be annotated using @ReactMethod.
     * The return type of bridge methods is always void. React Native bridge is asynchronous,
     * so the only way to pass a result to JavaScript is by using callbacks or emitting events.
     *
     * Native modules also support a special kind of argument - a callback.
     * In most cases it is used to provide the function call result to JavaScript.
     *
     * Native modules can also fulfill a promise, which can simplify your code, especially when
     * using ES2016's async/await syntax. When the last parameter of a bridged native method is a
     * Promise, its corresponding JS method will return a JS Promise object.
     */

    private void checkFileExists(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new Exception("File does not exist");
        }

        if (!file.isFile()) {
            throw new Exception("This is not a file");
        }

        if (!file.canRead()) {
            throw new Exception("File cannot be read");
        }

        if (!file.canWrite()) {
            throw new Exception("File cannot be written");
        }
    }

    @ReactMethod
    public void copyExifCallback(String srcUri, String destUri, Callback errorCallback, Callback successCallback) {
        File srcFile;
        File destFile;

        try {
            srcFile = new File(Uri.parse(srcUri).getPath());
            this.checkFileExists(srcFile);

            Log.d("copyExifCallback", "file exists (r/w): " + srcUri);
        } catch (Throwable t) {
            errorCallback.invoke(RNReadWriteExifModule.E_READ_SRC_FILE_ERROR + " " + t.getMessage());
            return;
        }

        try {
            destFile = new File(Uri.parse(destUri).getPath());
            this.checkFileExists(destFile);

            Log.d("copyExifCallback", "file exists (r/w): " + destUri);
        } catch (Throwable t) {
            errorCallback.invoke(RNReadWriteExifModule.E_READ_DEST_FILE_ERROR + " " + t.getMessage());
            return;
        }

        this.copyExif(srcFile, destFile, errorCallback, successCallback, null);
    }

    @ReactMethod
    public void copyExifPromise(String srcUri, String destUri, Promise promise) {
        File srcFile;
        File destFile;

        try {
            srcFile = new File(Uri.parse(srcUri).getPath());
            this.checkFileExists(srcFile);

            Log.d("copyExifPromise", "file exists (r/w): " + srcUri);
        } catch (Throwable t) {
            promise.reject(RNReadWriteExifModule.E_READ_SRC_FILE_ERROR, t);
            return;
        }

        try {
            destFile = new File(Uri.parse(destUri).getPath());
            this.checkFileExists(destFile);

            Log.d("copyExifPromise", "file exists (r/w): " + destUri);
        } catch (Throwable t) {
            promise.reject(RNReadWriteExifModule.E_READ_DEST_FILE_ERROR, t);
            return;
        }

        this.copyExif(srcFile, destFile, null, null, promise);
    }

    private void copyExif(@NonNull File srcFile, @NonNull File destFile,
                          @Nullable Callback errorCallback, @Nullable Callback successCallback,
                          @Nullable Promise promise) {
        CopyExifTask task = new CopyExifTask(srcFile, destFile, errorCallback, successCallback, promise);
        task.execute();
    }

    @ReactMethod
    public void scanFile(String uri, Promise promise) {
        final String MIME_JPEG = "image/jpeg";
        File file = null;

        Throwable throwable = null;

        try {
            file = new File(Uri.parse(uri).getPath());
            this.checkFileExists(file);
            Log.d("scanFile", "file exists (r/w): " + uri);
        } catch (Throwable t) {
            throwable = t;
        }

        if (file != null) {
            try {
                MediaScannerConnection.scanFile(
                        this.reactContext,
                        new String[]{file.getPath()},
                        new String[]{MIME_JPEG},
                        new MediaScannerConnection.MediaScannerConnectionClient() {
                            @Override
                            public void onMediaScannerConnected() {
                                Log.d("scanFile", "onMediaScannerConnected");
                            }

                            @Override
                            public void onScanCompleted(String s, Uri uri) {
                                Log.d("scanFile", "onMediaScannerConnected, s: " + s);
                            }
                        }
                );
            } catch (Throwable t) {
                throwable = t;
            }

            try {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                this.reactContext.sendBroadcast(mediaScanIntent);
            } catch (Throwable t) {
                throwable = t;
            }

            try {
                this.reactContext.sendBroadcast(
                        new Intent(
                                Intent.ACTION_MEDIA_MOUNTED,
                                Uri.parse("file://" + Environment.getExternalStorageDirectory())
                        )
                );
                this.reactContext.sendBroadcast(
                        new Intent(
                                Intent.ACTION_MEDIA_MOUNTED,
                                Uri.fromFile(file)
                        )
                );
            } catch (Throwable t) {
                throwable = t;
            }
        }

        if (throwable != null) {
            promise.reject(RNReadWriteExifModule.E_READ_SRC_FILE_ERROR, throwable);
            return;
        }

        promise.resolve(true);
    }
}
