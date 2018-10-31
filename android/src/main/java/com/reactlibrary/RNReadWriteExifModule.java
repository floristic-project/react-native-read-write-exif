
package com.reactlibrary;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.reactlibrary.tasks.CopyExifTask;
import com.reactlibrary.tasks.ReadExifDateTask;
import com.reactlibrary.tasks.ReadExifLatLonTask;

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
    }

    @ReactMethod
    public void copyExif(String srcUri, String destUri, Callback errorCallback, Callback successCallback) {
        File srcFile;
        File destFile;

        try {
            srcFile = new File(Uri.parse(srcUri).getPath());
            this.checkFileExists(srcFile);
        } catch (Exception e) {
            errorCallback.invoke(RNReadWriteExifModule.E_READ_SRC_FILE_ERROR + " " + e.getMessage());
            return;
        }

        try {
            destFile = new File(Uri.parse(destUri).getPath());
            this.checkFileExists(destFile);
        } catch (Exception e) {
            errorCallback.invoke(RNReadWriteExifModule.E_READ_DEST_FILE_ERROR + " " + e.getMessage());
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
        } catch (Exception e) {
            promise.reject(RNReadWriteExifModule.E_READ_SRC_FILE_ERROR, e);
            return;
        }

        try {
            destFile = new File(Uri.parse(destUri).getPath());
            this.checkFileExists(destFile);
        } catch (Exception e) {
            promise.reject(RNReadWriteExifModule.E_READ_DEST_FILE_ERROR, e);
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
    public void readExifDate(String uri, Callback errorCallback, Callback successCallback) {
        File file;

        try {
            file = new File(Uri.parse(uri).getPath());
            this.checkFileExists(file);
        } catch (Exception e) {
            errorCallback.invoke(RNReadWriteExifModule.E_READ_SRC_FILE_ERROR + " " + e.getMessage());
            return;
        }

        this.readExifDate(file, errorCallback, successCallback, null);
    }

    @ReactMethod
    public void readExifDatePromise(String uri, Promise promise) {
        File file;

        try {
            file = new File(Uri.parse(uri).getPath());
        } catch (Exception e) {
            promise.reject(RNReadWriteExifModule.E_READ_SRC_FILE_ERROR, e);
            return;
        }

        this.readExifDate(file, null, null, promise);
    }

    private void readExifDate(@NonNull File file,
                              @Nullable Callback errorCallback, @Nullable Callback successCallback,
                              @Nullable Promise promise) {
        ReadExifDateTask task = new ReadExifDateTask(file, errorCallback, successCallback, promise);
        task.execute();
    }

    @ReactMethod
    public void readExifLatLon(String uri, Callback errorCallback, Callback successCallback) {
        File file;

        try {
            file = new File(Uri.parse(uri).getPath());
            this.checkFileExists(file);
        } catch (Exception e) {
            errorCallback.invoke(RNReadWriteExifModule.E_READ_SRC_FILE_ERROR + " " + e.getMessage());
            return;
        }

        this.readExifLatLon(file, errorCallback, successCallback, null);
    }

    @ReactMethod
    public void readExifLatLonPromise(String uri, Promise promise) {
        File file;

        try {
            file = new File(Uri.parse(uri).getPath());
        } catch (Exception e) {
            promise.reject(RNReadWriteExifModule.E_READ_SRC_FILE_ERROR, e);
            return;
        }

        this.readExifLatLon(file, null, null, promise);
    }

    private void readExifLatLon(@NonNull File file,
                              @Nullable Callback errorCallback, @Nullable Callback successCallback,
                              @Nullable Promise promise) {
        ReadExifLatLonTask task = new ReadExifLatLonTask(file, errorCallback, successCallback, promise);
        task.execute();
    }
}