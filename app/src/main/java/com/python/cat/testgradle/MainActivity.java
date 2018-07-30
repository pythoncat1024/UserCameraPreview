package com.python.cat.testgradle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.python.cat.testgradle.utils.QRCodeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class MainActivity extends Activity {


    public static android.os.Handler mHandler;

    public static final int REQUEST_CAMERA = 12;

    private Activity get() {
        return this;
    }

    private ImageView img;
    private View btn;
    private ViewGroup rootLayout;
    private FrameLayout prevLayout;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        String s = Flowable.class.toString();
        setContentView(R.layout.activity_main);
        rootLayout = findViewById(R.id.main_root_view);
        img = findViewById(R.id.zx_img);
        btn = findViewById(R.id.btn_create_zx);
        prevLayout = findViewById(R.id.prev_view);

        LogUtils.w("");

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        assert wifiManager != null;
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        LogUtils.w(dhcpInfo);
        // ipaddr 172.20.161.205
        // gateway 172.20.160.1
        // netmask 255.255.254.0
        // dns1 172.16.2.15
        // dns2 172.16.2.16
        // DHCP server 172.20.160.1
        // lease 14400 seconds
        int ip = dhcpInfo.serverAddress;

        //此处获取ip为整数类型，需要进行转换
        final String strIp = intToIp(ip); // 172.20.160.1 ip --->< 27268268
        LogUtils.w(strIp + " ip --->< " + ip);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = new File(getCacheDir(), "zx.jpg").getAbsolutePath();
                String content = "ip==" + strIp;
                LogUtils.i("src info==" + content);
                boolean qr = QRCodeUtil.createQRImage(content, img.getWidth(), img.getHeight(), null,
                        path);
                LogUtils.w("生成二维码：" + qr);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                img.setImageBitmap(bitmap);

            }
        });

        findViewById(R.id.btn_scan_zx)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtils.w("扫描二维码");
//                        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
//                        startActivityForResult(intent, REQ_QR_CODE);

//                        ################### todo: 使用开源库的效果：
//                        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
//                        ZxingConfig config = new ZxingConfig();
//                        config.setPlayBeep(false);//是否播放扫描声音 默认为true
//                        config.setShake(false);//是否震动  默认为true
////                        config.setDecodeBarCode(false);//是否扫描条形码 默认为true
//                        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
//                        startActivityForResult(intent, REQUEST_CAMERA);

//                        ################### // end -- 开源库-实现扫码

//                        parseOriginZX();

                        // ok ... ok ... UseCameraActivity --> 自定义 surfaceView 实现扫码
//                        startActivity(new Intent(get(), UseCameraActivity.class));

                        // 试试 camera2 的 api
                        startActivity(new Intent(get(), AndroidCameraApi.class));

                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LogUtils.e(requestCode + " ... " + resultCode);
        LogUtils.w(data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.w(requestCode + " ， " + Arrays.toString(permissions) + " xx " + Arrays.toString(grantResults));
        switch (requestCode) {
            case REQUEST_CAMERA:
                LogUtils.i("request  camera...");
                take();
//                cameraPreview.takePicture(runnable, mPicture);
                break;
        }
    }

    private void take() {
        rootLayout.removeAllViews();
        ScanView scanView = new ScanView(this);
        rootLayout.addView(scanView);
    }

    /**
     * https://developer.android.google.cn/guide/topics/media/camera#custom-camera
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            LogUtils.w("----------data-----------------");
            camera.startPreview();
//            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                LogUtils.e("Error creating media file, check storage permissions: " +
                        null);
                return;
            }

            try {
                LogUtils.d(data);
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                LogUtils.w("complete###!!!");
            } catch (FileNotFoundException e) {
                LogUtils.e("File not found: " + e.getMessage());
            } catch (IOException e) {
                LogUtils.e("Error accessing file: " + e.getMessage());
            }
        }
    };

    private static File getOutputMediaFile() {


        return null;
    }


    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            LogUtils.w("has camera...");
            return true;
        } else {
            // no camera on this device
            LogUtils.w("has no camera...");
            return false;
        }
    }


    /**
     * 解析自己生成的二维码图片
     */
    private void parseOriginZX() {
        String path = new File(getCacheDir(), "zx.jpg").getAbsolutePath();
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Result result = parseInfoFromBitmap(bitmap);
        if (result != null) {
            LogUtils.w(result);
            LogUtils.i("result info==" + result.getText());
        }
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
                + ((i >> 24) & 0xFF);
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
        } catch (ChecksumException e) {
            LogUtils.v(e);
            e.printStackTrace();
        } catch (FormatException e) {
            LogUtils.d(e);
            e.printStackTrace();
        }

        return null;

    }


    static class ScanView extends SurfaceView implements SurfaceHolder.Callback,
            Camera.AutoFocusCallback {
        private Camera mCamera;
        private final File fileImg;

        public ScanView(Context context) {
            super(context);
            fileImg = new File(context.getCacheDir(), "prev_view.jpg");
            SurfaceHolder mHolder = getHolder();
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
                        camera.startPreview();
//            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
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
                            Toast.makeText(getContext(), "INFO:" + result.getText(), Toast.LENGTH_SHORT).show();
                            LogUtils.w("解析成功：" + result);
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
                parameters.setPictureSize(1600, 1200);
                parameters.setPreviewSize(640, 480);
                mCamera.startPreview();
                mCamera.autoFocus(this);
                LogUtils.e("surfaceCreated.. #####");

            } catch (IOException e) {
                LogUtils.e("Error setting camera preview: " + e.getMessage());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }


}
/*
1. 获取自己的 ip , serverIp 等信息。[已完成]
2. 然后生成一个二维码（包含这些信息）[已完成]
3. 另一个设备扫描二维码，然后得到我的ip 等信息
 */
