package com.example.amenoimi.ocr_test;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cv4j.core.binary.ConnectedAreaLabel;
import com.cv4j.core.binary.MorphOpen;
import com.cv4j.core.binary.Threshold;
import com.cv4j.core.datamodel.CV4JImage;
import com.cv4j.core.datamodel.ImageProcessor;
import com.cv4j.core.datamodel.Size;
import com.cv4j.image.util.Preconditions;
import com.cv4j.image.util.QRCodeScanner;
import com.cv4j.image.util.Tools;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static android.os.Environment.getDataDirectory;
import static android.os.Environment.getDownloadCacheDirectory;
import static android.os.Environment.getRootDirectory;

import com.google.zxing.qrcode.detector.AlignmentPattern;
import com.cv4j.core.datamodel.ByteProcessor;
import com.cv4j.image.util.QRCodeScanner.*;
import com.example.amenoimi.ocr_test.QR.*;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener,CompoundButton.OnCheckedChangeListener, SurfaceHolder.Callback{
    static String TESSBASE_PATH;
    static final String DEFAULT_LANGUAGE = "eng";
    static final String CHINESE_LANGUAGE = "chi_tra";
    static final String CHINESE_LANGUAGE_SIM = "chi_sim";
    static final String img_LANG = "img";
    private ImageView imgSrc;
    public TextView t1;
    public Button b1,b2,b3,b4;
    public Switch sw;
    public static final int progressType = 0;
    public static final int CAMERA_PIC_REQUEST = 12;
    private ProgressDialog progressDialog;
    public int ImgToTextMode=0;

    public int now_ocr=0;

    public int TextMode=0;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    ///为了使照片竖直显示
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceView2 ;
    private SurfaceHolder mSurfaceHolder2 ;


    private CameraManager mCameraManager;//摄像头管理器
    private Handler childHandler, mainHandler;
    private String mCameraID;//摄像头Id 0 为后  1 为前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    int tmp=0;
    public Bitmap new_bitmap;
    public Thread mThread;
    public boolean f=true;
    public int img_or_video_mode=0;
    public boolean areWeFocused = false;
    public Float Focus_distance;
    public Boolean QR_code_bool=false;

    public static String[] resizeArray(String[] arrayToResize, int size) {
        // create a new array twice the size
        String[] newArray = new String[size];

        System.arraycopy(arrayToResize, 0,
                newArray, 0, arrayToResize.length);
        return newArray;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String [] permission_array = new String[0];

        // Camera permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            permission_array = resizeArray(permission_array, permission_array.length + 1);
            permission_array[permission_array.length -1] = Manifest.permission.CAMERA;
        }

        // STORAGE permission
        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            permission_array = resizeArray(permission_array, permission_array.length + 1);
            permission_array[permission_array.length -1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }

        if (permission_array.length != 0)
            ActivityCompat.requestPermissions(MainActivity.this, permission_array, 1);



        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        imgSrc=(ImageView)findViewById(R.id.imageView);
        t1=(TextView)findViewById(R.id.t1);
        b1=(Button)findViewById(R.id.b1);
        b2=(Button)findViewById(R.id.b2);
        b3=(Button)findViewById(R.id.b3);
        b4=(Button)findViewById(R.id.b4);
        sw = (Switch) findViewById(R.id.sw);
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);
        b4.setOnClickListener(this);
        sw.setOnCheckedChangeListener(this);
        // mSurfaceView
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.setOnClickListener(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);


        // surfaceView2
         mSurfaceView2 = (SurfaceView) findViewById(R.id.surfaceView2);
         mSurfaceHolder2 = mSurfaceView2.getHolder();
        mSurfaceView2.setZOrderOnTop(true);//处于顶层
        mSurfaceHolder2.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder2.addCallback(this);

        Spinner spinner = (Spinner)findViewById(R.id.sp);
        ArrayAdapter<CharSequence> lunchList = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.lunch,
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(lunchList);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ImgToTextMode=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Focus_distance=0.0f;

//判定有無需要檔案
        try {
            TESSBASE_PATH =getDataDir(getApplicationContext());
            isExist(getDataDir(getApplicationContext())+"/tessdata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/chi_tra.traineddata"))mymodeDownload("chi_tra.traineddata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/chi_sim.traineddata")) mymodeDownload("chi_sim.traineddata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/eng.traineddata")) mymodeDownload("eng.traineddata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/img.traineddata")) mymodeDownload("img.traineddata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/QR.traineddata")) mymodeDownload("QR.traineddata");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //下載自己訓練的模型檔
    public void mymodeDownload(String mod) throws Exception {
        try {
            String path = getDataDir(getApplicationContext());
            path += "/tessdata/"+mod;
            if(fileIsExists(path + "/" + mod)) {
                deleteFile(path + "/" + mod);
            }
            new DownloadFromURL().execute("https://github.com/Amenoimi/tessdata_chi_tra.traineddata_rest/raw/master/"+mod, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  /**
     * 初始化
     */
    private void initVIew() {

        // 初始化Camera2
        initCamera2();
    }

    private void delView() {
        // 释放Camera资源
        if (null != mCameraDevice) {
            mCameraDevice.close();
            MainActivity.this.mCameraDevice = null;
        }
    }

    private String get_View( Bitmap b) {
        String resString = "";

        imgSrc.setDrawingCacheEnabled(true);
        final Bitmap bitmap =b;//convertToBMW( b,b.getWidth(),b.getHeight(),180);
        final TessBaseAPI ocrApi = new TessBaseAPI();
        
        switch (ImgToTextMode){
            case 0:
                ocrApi.init(TESSBASE_PATH, CHINESE_LANGUAGE);
                break;
            case 1:
                ocrApi.init(TESSBASE_PATH, CHINESE_LANGUAGE_SIM);
                break;
            case 2:
                ocrApi.init(TESSBASE_PATH,DEFAULT_LANGUAGE );
                break;
            case 3:
                ocrApi.init(TESSBASE_PATH,img_LANG );
                break;
        }


        switch (TextMode){
            case 0:
                ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD    );
                break;
            case 1:
                ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR      );
                break;
            case 2:
                ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_WORD      );
                break;
        }

        ocrApi.setImage(bitmap);
        resString = ocrApi.getUTF8Text();

        ocrApi.clear();
        ocrApi.end();
        return  resString;
    }
    public Bitmap getBitmap() {
       mSurfaceView.setDrawingCacheEnabled(true);
        mSurfaceView.buildDrawingCache(true);
        final Bitmap bitmap = Bitmap.createBitmap( mSurfaceView.getDrawingCache() );
        mSurfaceView.setDrawingCacheEnabled(false);
        mSurfaceView.destroyDrawingCache();
        return bitmap;
    }

    // 剪裁 Bitmap 會依照新的大小 自動至中剪裁
    public Bitmap Crop_Bitmap (Bitmap input,int re_width, int re_height) {
        int w = input.getWidth(), h = input.getHeight();
        Log.d("ABC", "w" + w);
        Log.d("ABC", "h" + h);
        Log.d("ABC", "rw" + re_width);
        Log.d("ABC", "rh" + re_height);
        return Bitmap.createBitmap(input, (w - re_width) / 2, (h - re_height) / 2, re_width, re_height);
    }
    // 剪裁 Bitmap 會依照新的大小 自動至中剪裁
    public Bitmap Crop_Bitmap_rect (Bitmap input,int x,int y,int re_width, int re_height) {
            Log.d(" Crop_Bitmap_rect", "x" + x);
            Log.d(" Crop_Bitmap_rect", "y" + y);
            Log.d(" Crop_Bitmap_rect", "rw" + re_width);
            Log.d(" Crop_Bitmap_rect", "rh" + re_height);
            int tmp;
            return Bitmap.createBitmap(input, x , y, re_width, re_height);

    }

    /**
     * 初始化Camera2
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());
        mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;//后摄像头
        mImageReader = ImageReader.newInstance(1080, 1020, ImageFormat.JPEG,1);

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {

                // 拿到拍照照片数据
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);//由缓冲区存入字节数组
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                QR_code_bool=false;
                if (bitmap != null) {

                    Matrix matrix  = new Matrix();
                    matrix.setRotate(90);
                    new_bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                    Log.d("GAN", String.valueOf(new_bitmap.getWidth()));
                    Log.d("GAN", String.valueOf(new_bitmap.getHeight()));


//                    int[] tmp=find_box(new_bitmap,20, 20);
//                    Log.d("rrrl", String.valueOf(tmp[0]));
//                    Log.d("rrrt", String.valueOf(tmp[1]));
//                    Log.d("rrrr", String.valueOf(tmp[2]));
//                    Log.d("rrrb", String.valueOf(tmp[3]));
//                    Rect rect = null;
//                    surfaceDrawing(mSurfaceView2.getHolder(), tmp[0]*1.5, tmp[1]*1.36770833333, tmp[0]+tmp[2]*1.5, tmp[1]+tmp[3]*1.36770833333);

//                    mSurfaceView.setVisibility(View.GONE);
//                    imgSrc.setVisibility(View.VISIBLE);
//                    imgSrc.setImageBitmap(convertToBMW(new_bitmap,new_bitmap.getWidth(),new_bitmap.getHeight(),100));

//
//                    if(tmp[0]>0&&tmp[1]>0&& tmp[2]>0&&tmp[3]>0) {
//
//                        rect.left=tmp[0];
//                        rect.top=tmp[1];
//                        rect.right=tmp[0]+tmp[2];
//                        rect.bottom=tmp[1]+tmp[3];
//                        new_bitmap = Crop_Bitmap_rect(new_bitmap, rect.left,rect.top, rect.right,rect.bottom);
//                        mSurfaceView.setVisibility(View.GONE);
//                        imgSrc.setVisibility(View.VISIBLE);
//                        imgSrc.setImageBitmap(new_bitmap);
//                        Log.d("QQ", "C");
//                        QR_code_bool=true;
//                        f=false;
//                        b4.setBackgroundResource(R.drawable.unsee);
//                    }else{
//                        QR_code_bool=false;
//                    }




//
//                    CV4JImage cv4JImage = new CV4JImage(new_bitmap);
//                    ImageProcessor img= cv4JImage.getProcessor();
//                    Threshold threshold = new Threshold();
//                    threshold.process((ByteProcessor)(cv4JImage.convert2Gray().getProcessor()),Threshold.THRESH_TRIANGLE,Threshold.METHOD_THRESH_BINARY_INV,255);
//                    MorphOpen morphOpen = new MorphOpen();
//                    cv4JImage.resetBitmap();
//                    morphOpen.process((ByteProcessor)cv4JImage.getProcessor(),new Size(5));
//
//                    ConnectedAreaLabel connectedAreaLabel = new ConnectedAreaLabel();
//                    int[] mask = new int[cv4JImage.getProcessor().getWidth() * cv4JImage.getProcessor().getHeight()];
//                    List<com.cv4j.core.datamodel.Rect> rectangles = new ArrayList<>();
//                    connectedAreaLabel.process((ByteProcessor)cv4JImage.getProcessor(), mask,rectangles,true);
//                    cv4JImage.resetBitmap();
//                    new_bitmap = cv4JImage.getProcessor().getImage().toBitmap();
//
//                    if (Preconditions.isNotBlank(rectangles)) {
//                        Tools.drawRects(new_bitmap,rectangles);
//                        new_bitmap=ARGBBitmap(new_bitmap);
//                        mSurfaceView.setVisibility(View.GONE);
//                        imgSrc.setVisibility(View.VISIBLE);
//                        imgSrc.setImageBitmap(new_bitmap);
//                        Log.d("QQ", "C");
//                        QR_code_bool = true;
//                        f = false;
//                        b4.setBackgroundResource(R.drawable.unsee);
//                    }else{
//                        QR_code_bool = false;
//                    }







                    CV4JImage cv4JImage = new CV4JImage(new_bitmap);
                    ImageProcessor img= cv4JImage.getProcessor();

                    Log.d("GAN", String.valueOf(mSurfaceView2.getWidth()));
                    Log.d("GAN", String.valueOf(mSurfaceView2.getHeight()));

                    com.cv4j.core.datamodel.Rect rect =findQRCodeBounding(img, 1, 6);
                    Log.d("OUO(GAN((tx", String.valueOf(rect.tl().x));
                    Log.d("OUO(GAN((ty", String.valueOf(rect.tl().y));
                    Log.d("OUO(GAN((bx", String.valueOf(rect.br().x));
                    Log.d("OUO(GAN((by", String.valueOf(rect.br().y));
                    surfaceDrawing(mSurfaceView2.getHolder(), rect.tl().x*1.5, rect.tl().y*1.36770833333, rect.br().x*1.5, rect.br().y*1.36770833333);

                    if(rect.tl().x>0&&rect.tl().y>0&&rect.br().x>0&&rect.br().y>0&&rect.br().x<new_bitmap.getWidth()&&rect.br().y<new_bitmap.getHeight()&&Math.abs(rect.br().x-rect.tl().x)>100&&Math.abs(rect.br().y-rect.tl().y)>100) {
                        Log.d("OAO(GAN((w", String.valueOf( Math.abs(rect.br().x-rect.tl().x )));
                        Log.d("OAO(GAN((h", String.valueOf(Math.abs(rect.br().y-rect.tl().y)));

                        new_bitmap = Crop_Bitmap_rect(new_bitmap, rect.x, rect.y, Math.abs(rect.br().x-rect.x ),Math.abs(rect.br().y-rect.y));//這個地方怪怪的
                        new_bitmap=convertToBMW(new_bitmap,new_bitmap.getWidth(),new_bitmap.getHeight(),100);
                        mSurfaceView.setVisibility(View.GONE);
                        imgSrc.setVisibility(View.VISIBLE);
                        imgSrc.setImageBitmap(new_bitmap);
                        Log.d("QQ", "C");
                        QR_code_bool=true;
                        f=false;
                        b4.setBackgroundResource(R.drawable.unsee);
                    }else{
                        QR_code_bool=false;
                    }
                    image.close();
                }
            }
        }, mainHandler);
        //获取摄像头管理
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        //获取摄像头管理

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开摄像头
            mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Bitmap ARGBBitmap(Bitmap img) {
        return img.copy(Bitmap.Config.ARGB_8888,true);
    }

    /**
     * 摄像头创建监听
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {//打开摄像头
            mCameraDevice = camera;
            //开启预览

            takePreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {//关闭摄像头
            if (null != mCameraDevice) {
                Log.d("QQ","bye");
                mCameraDevice.close();
                MainActivity.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {//发生错误
            Toast.makeText(MainActivity.this, "摄像头开启失败", Toast.LENGTH_SHORT).show();
        }
    };



    /**
     * 开始预览
     */
    private void takePreview() {
        try {
            // 创建预览需要的CaptureRequest.Builder
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSession.StateCallback() // ③
            {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    // 当摄像头已经准备好时，开始显示预览
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        // 自动对焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打开闪光灯
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        // 显示预览
                        CaptureRequest previewRequest = previewRequestBuilder.build();

                        // add CaptureCallback
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, mCaptureCallback, childHandler);


                            f = true;
                            mThread = new Thread(r1);
                            mThread.start();




                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "配置失败", Toast.LENGTH_SHORT).show();
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {

            int afState = result.get(CaptureResult.CONTROL_AF_STATE);
            if (CaptureResult.CONTROL_AF_TRIGGER_START == afState) {
                if (areWeFocused) {
                    //Run specific task here
                    Log.d("AAA", "YYYYYY" );
                }
            }
            if (CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState) {
                areWeFocused = true;
            } else {
                areWeFocused = false;
            }
            Focus_distance= result.get(CaptureResult.LENS_FOCUS_DISTANCE);
            Log.d("OAAAO", Focus_distance.toString());
            Log.d("AAA", String.valueOf(afState) );
            Log.d("t", String.valueOf(areWeFocused) );

        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                        CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            process(result);
        }
    };

    private Runnable r1=new Runnable () {

        public void run() {

            // TODO Auto-generated method stub
            while (f) {
                if(img_or_video_mode<1){

                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (now_ocr < 1 && areWeFocused && Focus_distance>3) {
                        now_ocr = 1;
                        takePicture();
                        while (new_bitmap == null && QR_code_bool==false) {

                        }
                        Message msg = mHandler.obtainMessage();
                        msg.obj = null;
                        //                            msg.what = 1;
//                        BitMatrix QR_bitmap=new BitMatrix(new_bitmap.getWidth(),new_bitmap.getHeight(),new_bitmap.getDensity(),new_bitmap.getNinePatchChunk());
//                        new AlignmentPatternFinder(new_bitmap,0,0,new_bitmap.getWidth(),new_bitmap.getHeight());

//                        Bitmap.createScaledBitmap(new_bitmap, 960, 480, false);


                            msg.obj = get_View(new_bitmap); // Put the string into Message, into "obj" field.
                            QR_code_bool=false;
                            while (msg.obj == null) {

                            }
                            Log.d("QQ", msg.obj.toString());
                            msg.setTarget(mHandler); // Set the Handler
                            msg.sendToTarget();

                            Log.d("QQ", "B");

                    }
                }else if(img_or_video_mode==2){
                    takePicture();
                    while (new_bitmap == null) {

                    }
                    Message msg = mHandler.obtainMessage();
                    msg.obj = null;
                    //                            msg.what = 1;
                    msg.obj = get_View(new_bitmap); // Put the string into Message, into "obj" field.
                    while (msg.obj == null) {

                    }
                    Log.d("QQ", msg.obj.toString());
                    msg.setTarget(mHandler); // Set the Handler
                    msg.sendToTarget();
                    img_or_video_mode++;
                    f=false;
                }

            }
        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            if(msg.what == 1) {
//                if(now_ocr==0){
//                    t1.setText(get_View());
//                    now_ocr=0;
//                }
//                now_ocr++;
//            }

                Log.d("QQ","A");
                String message = (String) msg.obj;
                t1.setText(message);
                if(img_or_video_mode>0){
                    imgSrc.setImageBitmap(new_bitmap);
                }
                now_ocr=0;
                if(img_or_video_mode==3){
                    img_or_video_mode=0;
                    b4.setBackgroundResource(R.drawable.unsee);
                    QR_code_bool=false;
                    delView();
                }

            super.handleMessage(msg);
        }
    };


    /**
     * 拍照
     */
    private void takePicture() {
        if (mCameraDevice == null) return;
        // 创建拍照需要的CaptureRequest.Builder
        final CaptureRequest.Builder captureRequestBuilder;
        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取手机方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            //拍照
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.capture(mCaptureRequest, null, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }





    public void up_mode(View v){
        try {
            mymodeDownload("chi_tra.traineddata");
            mymodeDownload("chi_sim.traineddata");
            mymodeDownload("eng.traineddata");
            mymodeDownload("img.traineddata");
            mymodeDownload("QR.traineddata");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //開相簿
    public void get_img(View v){
        final String mimeType = "image/*";
        Intent intent = new Intent();
        intent.setType(mimeType);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 0);
    }
    //開相機
    public void get_img_now(View v){
        Calendar cal = Calendar.getInstance();
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;

        progressDialog = new ProgressDialog(this);
        // progressDialog.setTitle("");
        progressDialog.setMessage("識別中.....");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        // 取得檔案的 Uri
        Log.d("QUQ", data.toString());

        Uri uri = data.getData();
        ContentResolver cr = this.getContentResolver();

            if (requestCode==0) {
                // 有選擇檔案
                try {
                    if (uri != null) {
                        imgSrc.setImageURI(uri);
                        // imgSrc.setImageBitmap(Crop_Bitmap(((BitmapDrawable)imgSrc.getDrawable()).getBitmap(), 100, 50));
                        t1.setText(ocrWithEnglish());
                        progressDialog.dismiss();
                    }
                } catch (IOError e){

                }
            } else if (requestCode==12) {
                try {
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    uri = getImageUri(getApplicationContext(), imageBitmap);
                    if (uri != null) {
                        imgSrc.setImageURI(uri);
                        t1.setText(ocrWithEnglish());
                        progressDialog.dismiss();
                    }
                } catch (IOError e){

                }
            }



    }

    @Override
    public void onClick(View v) {
       SurfaceView mUI=findViewById(R.id.surfaceView2);
        if( v.getId()==R.id.b1){
            b4.setBackgroundResource(R.drawable.unsee);
            get_img(v);
            if(mSurfaceView != null){
                mSurfaceView.setVisibility(View.GONE);
                mUI.setVisibility(View.GONE);
            }
            imgSrc.setVisibility(View.VISIBLE);
            QR_code_bool=false;
        }
        if( v.getId()==R.id.b2){
//            b4.setBackgroundResource(R.drawable.unsee);
//            get_img_now(v);
//            if(mSurfaceView != null)
//                mSurfaceView.setVisibility(View.GONE);
//            imgSrc.setVisibility(View.VISIBLE);
            if(img_or_video_mode==0||img_or_video_mode==1) img_or_video_mode++;
            if(img_or_video_mode==1){
                b4.setBackgroundResource(R.drawable.see);
                if(mSurfaceView != null)
                    mSurfaceView.setVisibility(View.VISIBLE);
                mUI.setVisibility(View.VISIBLE);
                imgSrc.setVisibility(View.GONE);
                initVIew();
            }else if(img_or_video_mode==2){

                if(mSurfaceView != null){
                    mSurfaceView.setVisibility(View.GONE);
                    mUI.setVisibility(View.GONE);
                }
                imgSrc.setVisibility(View.VISIBLE);
                imgSrc.getLayoutParams().height = 800;

//                while (new_bitmap==null){
//
//                }

            }else if(img_or_video_mode==3){
                img_or_video_mode=0;
                b4.setBackgroundResource(R.drawable.unsee);
                QR_code_bool=false;
                delView();
            }

        }
        if(v.getId()==R.id.b3) up_mode(v);
        if(v.getId()==R.id.b4){
            if(tmp==0){
                tmp=1;
                b4.setBackgroundResource(R.drawable.see);
                if(mSurfaceView != null)
                    mSurfaceView.setVisibility(View.VISIBLE);
                mUI.setVisibility(View.VISIBLE);
                imgSrc.setVisibility(View.GONE);
                initVIew();
            }else if(tmp==1){
                tmp=0;
                b4.setBackgroundResource(R.drawable.unsee);
                if(mSurfaceView != null){
                    mSurfaceView.setVisibility(View.GONE);
                    mUI.setVisibility(View.GONE);
                }
                imgSrc.setVisibility(View.VISIBLE);
                imgSrc.getLayoutParams().height = 800;
                f=false;
                QR_code_bool=false;
//                while (new_bitmap == null) {
//
//                }
                delView();
            }
        }
    }
    //執行OCR
    public String ocrWithEnglish() {
        String resString = "";

        imgSrc.setDrawingCacheEnabled(true);
        final Bitmap bitmap =imgSrc.getDrawingCache();//convertToBMW(imgSrc.getDrawingCache(),imgSrc.getWidth()*3,imgSrc.getHeight()*3,180);
        final TessBaseAPI ocrApi = new TessBaseAPI();

        switch (ImgToTextMode){
            case 0:
                ocrApi.init(TESSBASE_PATH, CHINESE_LANGUAGE);
                break;
            case 1:
                ocrApi.init(TESSBASE_PATH, CHINESE_LANGUAGE_SIM);
                break;
            case 2:
                ocrApi.init(TESSBASE_PATH,DEFAULT_LANGUAGE );
                break;
            case 3:
                ocrApi.init(TESSBASE_PATH,img_LANG );
                break;
        }

//        switch (TextMode){
//            case 0:
//                ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD    );
//                break;
//            case 1:
//                ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR      );
//                break;
//            case 2:
//                ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_COLUMN        );
//                break;
//        }

        ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE    );


        ocrApi.setImage(bitmap);
        resString = ocrApi.getUTF8Text();

        ocrApi.clear();
        ocrApi.end();
        return  resString;
    }

    public boolean fileIsExists(String strFile)
    {
        //判斷文件夾是否存在
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            {
                return false;
            }

        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public static void isExist(String path) {
        File file = new File(path);
        //判斷文件夾是否存在,如果不存在則建立文件夾
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static String getDataDir(Context context) throws Exception {
        return context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0)
                .applicationInfo.dataDir;
    }

    private void writeToFile(File fout, String data) {
        FileOutputStream osw = null;
        try {
            osw = new FileOutputStream(fout);
            osw.write(data.getBytes());
            osw.flush();
        } catch (Exception e) {
            ;
        } finally {
            try {
                osw.close();
            } catch (Exception e) {
                ;
            }
        }
    }
    private String readFromFile(File fin) {
        StringBuilder data = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(fin), "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        } catch (Exception e) {
            ;
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                ;
            }
        }
        return data.toString();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (sw.getId()){
            case R.id.sw:
                if(sw.isChecked())TextMode=2;
                else TextMode=0;
                break;


        }


    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //定义画笔
        Paint mpaint = new Paint();
        mpaint.setColor(Color.BLUE);
        // mpaint.setAntiAlias(true);//去锯齿
        mpaint.setStyle(Paint.Style.STROKE);//空心
        // 设置paint的外框宽度
        mpaint.setStrokeWidth(20f);

        Canvas canvas=new Canvas();

        canvas =  surfaceHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
        Rect r = new Rect(0,0,0,0);
        canvas.drawRect(r, mpaint);
        surfaceHolder.unlockCanvasAndPost(canvas);

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


    class DownloadFromURL extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progressType);
        }

        @Override
        protected String doInBackground(String... fileUrl) {
            int count;
            try {
                URL url = new URL(fileUrl[0]);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();
                // show progress bar 0-100%
                int fileLength = urlConnection.getContentLength();
                InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
                OutputStream outputStream = new FileOutputStream(fileUrl[1]);

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / fileLength));
                    outputStream.write(data, 0, count);
                }
                // flushing output
                outputStream.flush();
                // closing streams
                outputStream.close();
                inputStream.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        // progress bar Updating

        protected void onProgressUpdate(String... progress) {
            // progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            dismissDialog(progressType);
        }
    }

    //progress dialog
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progressType: // we set this to 0
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("File is Downloading. Please wait...");
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                return progressDialog;
            default:
                return null;
        }
    }

    //二值化
    public static Bitmap convertToBMW(Bitmap bmp, int w, int h,int tmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                // 分離三原色
                alpha = ((grey & 0xFF000000) >> 24);
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                if (red > tmp) {
                    red = 255;
                } else {
                    red = 0;
                }
                if (blue > tmp) {
                    blue = 255;
                } else {
                    blue = 0;
                }
                if (green > tmp) {
                    green = 255;
                } else {
                    green = 0;
                }
                pixels[width * i + j] = alpha << 24 | red << 16 | green << 8
                        | blue;
                if (pixels[width * i + j] == -1) {
                    pixels[width * i + j] = -1;
                } else {
                    pixels[width * i + j] = -16777216;
                }
            }
        }
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, w, h);
        return resizeBmp;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }




    public com.cv4j.core.datamodel.Rect findQRCodeBounding(ImageProcessor image, int n1, int n2) {

        com.cv4j.core.datamodel.Rect rect = new com.cv4j.core.datamodel.Rect();
        image = image.getImage().convert2Gray().getProcessor();
        ByteProcessor src = ((ByteProcessor)image);
        int width = src.getWidth();
        int height = src.getHeight();
        Threshold t = new Threshold();
        t.process(src, Threshold.THRESH_OTSU, Threshold.METHOD_THRESH_BINARY_INV, 20);
        MorphOpen mOpen = new MorphOpen();
        byte[] data = new byte[width*height];
        System.arraycopy(src.getGray(), 0, data, 0, data.length);
        ByteProcessor copy = new ByteProcessor(data, width, height);
        mOpen.process(src, new Size(n1, n2)); // Y方向开操作
        src.getImage().resetBitmap();

        mOpen.process(copy, new Size(n2, n1)); // X方向开操作
        CV4JImage cv4JImage = new CV4JImage(width,height);
        ((ByteProcessor)cv4JImage.getProcessor()).putGray(copy.getGray());

        for(int i=0; i<data.length; i++) {
            int pv = src.getGray()[i]&0xff;
            if(pv == 255) {
                copy.getGray()[i] = (byte)255;
            }
        }
        src.putGray(copy.getGray());

        // 联通组件查找连接区域
        ConnectedAreaLabel ccal = new ConnectedAreaLabel();
        ccal.setFilterNoise(true);
        List<com.cv4j.core.datamodel.Rect> rectList = new ArrayList<>();
        int[] labelMask = new int[width*height];
        ccal.process(src, labelMask, rectList, true);
        float w = 0;
        float h = 0;
        float rate = 0;
        List<com.cv4j.core.datamodel.Rect> qrRects = new ArrayList<>();
        for(com.cv4j.core.datamodel.Rect roi : rectList) {

            if (roi == null) continue;

            if((roi.width > width/4 || roi .width < 10) || (roi.height < 10 || roi.height > height/4))
                continue;

            if((roi.x < 10 || roi.x > width -10)|| (roi.y < 10 || roi.y > height-10))
                continue;

            w = roi.width;
            h = roi.height;
            rate = (float)Math.abs(w / h  - 1.0);
            if(rate < 0.05 && isRect(roi, labelMask, width, height,true)) {
                qrRects.add(roi);
            }
        }

        // find RQ code bounding
        com.cv4j.core.datamodel.Rect[] blocks = qrRects.toArray(new com.cv4j.core.datamodel.Rect[0]);
        Log.i("QRCode Finder", "blocks.length : " + blocks.length);

        if (Preconditions.isBlank(blocks)) {

            for(com.cv4j.core.datamodel.Rect roi : rectList) {

                if (roi == null) continue;

                if((roi.width > width/4 || roi .width < 10) || (roi.height < 10 || roi.height > height/4))
                    continue;

                if((roi.x < 10 || roi.x > width -10)|| (roi.y < 10 || roi.y > height-10))
                    continue;

                w = roi.width;
                h = roi.height;
                rate = (float)Math.abs(w / h  - 1.0);
                if(rate < 0.05 && isRect(roi, labelMask, width, height,false)) {
                    qrRects.add(roi);
                }
            }

            // find RQ code bounding
            blocks = qrRects.toArray(new com.cv4j.core.datamodel.Rect[0]);
            Log.i("QRCode Finder", "blocks.length : " + blocks.length);
        }

        // 二维码很小的情况
        if (blocks.length == 1) {
            rect.x = blocks[0].x-5;
            rect.y = blocks[0].y- 5;
            rect.width= blocks[0].width + 10;
            rect.height = blocks[0].height + 10;
        } else if (blocks.length == 6 || blocks.length == 3) {
            for (int i = 0; i < blocks.length-1; i++) {
                for (int j = i + 1; j < blocks.length; j++) {
                    int idx1 = blocks[i].tl().y*width + blocks[i].tl().x;
                    int idx2 = blocks[j].tl().y*width + blocks[j].tl().x;
                    if (idx2 < idx1){
                        com.cv4j.core.datamodel.Rect temp = blocks[i];
                        blocks[i] = blocks[j];
                        blocks[j] = temp;
                    }
                }
            }
            rect.x = blocks[0].x - 5;
            rect.y = blocks[0].y - 5;
            rect.width = blocks[1].width + (blocks[1].x - blocks[0].x) + 10;
            if(blocks.length == 3) {
                rect.height = (blocks[2].height + blocks[2].y - blocks[0].y) + 10;
            } else {
                rect.height = (blocks[4].height + blocks[4].y - blocks[0].y) + 10;
            }
        } else {
            rect.width = 0;
            rect.height = 0;
        }
        return rect;
    }

    private boolean isRect(com.cv4j.core.datamodel.Rect roi, int[] labelMask, int w, int h, boolean useRate) {
        int ox = roi.x;
        int oy = roi.y;
        int width = roi.width;
        int height = roi.height;

        byte[] image = new byte[width*height];
        int label = roi.labelIdx;
        float bcount = 0, wcount = 0;
        for(int row=oy; row<(oy + height); row++) {
            for(int col=ox; col<(ox + width); col++) {
                int v = labelMask[row*w + col];
                if(v == label) {
                    image[(row - oy) * width + col - ox] = (byte)255;
                    wcount++;
                } else {
                    bcount++;
                }
            }
        }


        int cx = width / 2;
        int offset = 0;
        if (width % 2 > 0) {
            offset = 1;
        }

        int v1=0, v2=0;
        float[] data = new float[cx *height];
        for(int row=0; row<height; row++) {
            for(int col=0; col<cx; col++) {
                v1 = image[row*width+ col]&0xff;
                v2 = image[row*width+(width-1-col)]&0xff;
                data[row*cx+col] = Math.abs(v1-v2);
            }
        }

        float[] mdev = Tools.calcMeansAndDev(data);
        Log.i("QRCodeScanner","mdev[0]="+mdev[0]);
        Log.i("QRCodeScanner","mdev[1]="+mdev[1]);

        if (useRate) {
            // 黑色跟白色的像素数目比
            float rate = Math.min(bcount, wcount)/Math.max(bcount, wcount);

            return mdev[0] <= 20 && rate > 0.50f;
        } else {

            return mdev[0] <= 20;
        }
    }

    public void surfaceDrawing(SurfaceHolder surfaceHolder, Double L, Double T, Double R, Double B) {
        //定义画笔
        Paint mpaint = new Paint();
        mpaint.setColor(Color.BLUE);
        // mpaint.setAntiAlias(true);//去锯齿
        mpaint.setStyle(Paint.Style.STROKE);//空心
        // 设置paint的外框宽度
        mpaint.setStrokeWidth(20f);

        Canvas canvas=new Canvas();

        canvas =  surfaceHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
        Rect r = new Rect(  L.intValue(),T.intValue(),R.intValue(),B.intValue());
        canvas.drawRect(r, mpaint);
        surfaceHolder.unlockCanvasAndPost(canvas);

    }



    // 找框線  原圖, 框線大小相對於原圖的百分比 width, height
    public int [] find_box(Bitmap img, int box_width_proportion, int box_height_proportion) {

        int fx=-1, fy=-1, fw=-1, fh=-1;
        int width = img.getWidth();
        int height = img.getHeight();
        img= convertToBMW(img,width,height,100);
        // 由上往下 找 x,y width
        for (int y=0; y<height; y++) {
            int tpx = -1;
            int count = 0;
            int px = 0;
            int red, green, blue;
            for (int x=0; x<width; x++) {
                px = img.getPixel(x, y);
                red = Color.red(px);
                green = Color.green(px);
                blue = Color.blue(px);
                if ( (red == 0)&&(green == 0)&&(blue == 0) ) {
                    count++;
                    tpx = x;
                }
            }
            if ( (count / width) >= box_width_proportion ) {
                fx = tpx;
                fy = y;
                fw = count;
                break;
            }
        }

        // 有找到 x,y 的話開始找 height 由左到右
        if (fx != -1) {

            for (int x= ((fx - 10) > 0)? fx-10:0; x<width; x++) {
                int count = 0;
                int px = 0;
                int red, green, blue;
                for (int y=0; y<height; y++) {
                    px = img.getPixel(x, y);
                    red = Color.red(px);
                    green = Color.green(px);
                    blue = Color.blue(px);
                    if ( (red == 0)&&(green == 0)&&(blue == 0) ) {
                        count++;
                    }
                }
                if ( (count / height) >= box_height_proportion ) {
                    fh = count;
                    break;
                }
            }

        }

        return new int[] {fx, fy, fw, fh};
    }




}
