package com.python.cat.testgradle.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.apkfuns.logutils.LogUtils;
import com.python.cat.testgradle.MainActivity;

import java.io.IOException;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView
        implements SurfaceHolder.Callback,
        Camera.AutoFocusCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Parameters parameters;

    public Camera getmCamera() {
        return mCamera;
    }

    public CameraPreview(Context context) {
        super(context);
//        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        LogUtils.e("x surfaceCreated.. #####");

        try {
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            parameters = mCamera.getParameters();
            parameters.setPictureSize(1600, 1200);
            parameters.setPreviewSize(640, 480);
            mCamera.startPreview();
            mCamera.autoFocus(this);
            LogUtils.e("surfaceCreated.. #####");
            if (MainActivity.mHandler != null) {
                MainActivity.mHandler.sendEmptyMessage(123);
            }
        } catch (IOException e) {
            LogUtils.e("Error setting camera preview: " + e.getMessage());
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        LogUtils.e("xx pp");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        LogUtils.e("x surfaceChanged.. #####");
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            LogUtils.e("Error starting camera preview: " + e.getMessage());
        }
    }

    public void takePicture(Runnable runnable, Camera.PictureCallback callback) {

        runnable.run();
//        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                LogUtils.e("call back....");
//                LogUtils.i(data);
//                LogUtils.w(camera);
//            }
//        });
////        mCamera.startPreview();
//        LogUtils.w("start preview");
//        mCamera.takePicture(null, null, null); // 会崩溃

        if (mCamera != null) {
            mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    LogUtils.w("========data========");
                }
            });
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance

            LogUtils.w("c=====" + c);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        LogUtils.w("auto focus..." + success);
        if (success) {
            takePicture(runnable, callback);
        }
    }

    public Runnable runnable;

    public Camera.PictureCallback callback;
}