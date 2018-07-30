package com.python.cat.testgradle;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UseCameraActivity extends Activity {


    private Activity get() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asome);
        setTitle(getClass().getSimpleName());
        final FrameLayout frameLayout = findViewById(R.id.prev_content_layout);
        Button btn = findViewById(R.id.start_camera_preview);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndPermission.with(get())
                        .permission(Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        .onDenied(new Action() {
                            @Override
                            public void onAction(List<String> permissions) {
                                LogUtils.e("error....." + permissions);
                            }
                        })
                        .onGranted(new Action() {
                            @Override
                            public void onAction(List<String> permissions) {
                                LogUtils.w("you can do..");
                                ScanView scanView = new ScanView(get());
                                frameLayout.removeAllViews();
                                frameLayout.addView(scanView);
                            }
                        }).start();
            }
        });
    }


    static class ScanView extends SurfaceView implements SurfaceHolder.Callback,
            Camera.AutoFocusCallback {
        private Camera mCamera;
        private final File fileImg;

        private ScanView self;

        public ScanView(Context context) {
            super(context);
            fileImg = new File(context.getCacheDir(), "prev_view.jpg");
            SurfaceHolder mHolder = getHolder();
            self = this;
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            LogUtils.w("auto focus..." + success);
            if (mCamera != null) {
                mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {

                        mCamera.cancelAutoFocus();
                        mCamera.stopPreview(); // 拿到数据就停止！！！
                        LogUtils.w("========data========");
                        LogUtils.w("----------data-----------------");
//                        camera.startPreview();
                        File pictureFile = fileImg;
                        if (pictureFile == null) {
                            LogUtils.e("Error creating media file, check storage permissions: " +
                                    null);
                            return;
                        }

                        if (data == null) {
                            return;
                        }
                        try {
                            LogUtils.d(data);
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();
                            LogUtils.e("save preview complete###!!!");
                            LogUtils.e("save preview complete###!!!" + pictureFile);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), options);
                            options.inJustDecodeBounds = false;
                            int outWidth = options.outWidth;
                            int outHeight = options.outHeight;
                            if (outWidth >= getWidth() * 2) {
                                options.inSampleSize = outWidth / getWidth();
                            }
                            if (outHeight >= getHeight() * 2) {
                                options.inSampleSize = outHeight / getHeight();
                            }
                            Bitmap bmp = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), options);
                            Result result = parseInfoFromBitmap(bmp);
                            if (result != null) {
                                Toast.makeText(getContext(), "INFO:" + result.getText(), Toast.LENGTH_SHORT).show();
                                LogUtils.w("解析成功：" + result);
                            } else {
                                LogUtils.e("再次尝试中....");
                                mCamera.startPreview();
                                mCamera.autoFocus(self);
                                // todo:这里也可以做最大重试次数的限制...
                            }
                        } catch (Exception e) {
                            LogUtils.e("Error accessing file: " + e.getMessage());
                        }
                    }
                });
            }
        }

        public Result parseInfoFromBitmap(Bitmap bitmap) {
            int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            LogUtils.w("### pixels dest==" + Arrays.toString(pixels));

            RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(),
                    bitmap.getHeight(), pixels);
            GlobalHistogramBinarizer binarizer = new GlobalHistogramBinarizer(source);
            BinaryBitmap image = new BinaryBitmap(binarizer);
            Result result = null;
            try {
                result = new QRCodeReader().decode(image);
                return result;
            } catch (NotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "非二维码图片，不能解析", Toast.LENGTH_SHORT).show();
            } catch (ChecksumException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            LogUtils.e("x surfaceCreated.. #####");
            try {
                mCamera = Camera.open();
                mCamera.setPreviewDisplay(holder);
                mCamera.setDisplayOrientation(90);
                Camera.Parameters parameters = mCamera.getParameters();
//                parameters.setPictureSize(1600, 1200);
//                parameters.setPreviewSize(640, 480);
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                mCamera.autoFocus(this);
                LogUtils.e("surfaceCreated.. #####");

            } catch (IOException e) {
                LogUtils.e("Error setting camera preview: " + e.getMessage());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            LogUtils.w("--change-");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            LogUtils.w("destroy--");
            if (mCamera != null) {
                mCamera.cancelAutoFocus();
                mCamera.stopPreview();
                mCamera.release();
            }
        }
    }

    // ----------------


}
