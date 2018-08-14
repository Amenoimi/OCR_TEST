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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static android.os.Environment.getDataDirectory;
import static android.os.Environment.getDownloadCacheDirectory;
import static android.os.Environment.getRootDirectory;

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
    private ImageView iv_show;
    private CameraManager mCameraManager;//摄像头管理器
    private Handler childHandler, mainHandler;
    private String mCameraID;//摄像头Id 0 为后  1 为前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    int tmp=0;
    public   Bitmap new_bitmap;
    public Thread mThread;
    public boolean f=true;
    public int img_or_video_mode=0;
    public int bitmap_rew,bitmap_reh;
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
        SurfaceView mSurfaceView2 = (SurfaceView) findViewById(R.id.surfaceView2);
        SurfaceHolder mSurfaceHolder2 = mSurfaceView2.getHolder();
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
//判定有無需要檔案
        try {
            TESSBASE_PATH =getDataDir(getApplicationContext());
            isExist(getDataDir(getApplicationContext())+"/tessdata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/chi_tra.traineddata"))mymodeDownload("chi_tra.traineddata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/chi_sim.traineddata")) mymodeDownload("chi_sim.traineddata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/eng.traineddata")) mymodeDownload("eng.traineddata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/img.traineddata")) mymodeDownload("img.traineddata");
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
        iv_show = (ImageView) findViewById(R.id.imageView);
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
    public Bitmap Crop_Bitmap (Bitmap input, int re_width, int re_height) {
        int w = input.getWidth(), h = input.getHeight();
        Log.d("ABC", "w" + w);
        Log.d("ABC", "h" + h);
        Log.d("ABC", "rw" + re_width);
        Log.d("ABC", "rh" + re_height);
        return Bitmap.createBitmap(input, (w - re_width) / 2, (h - re_height) / 2, re_width, re_height);
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
//                mCameraDevice.close();
//                mSurfaceView.setVisibility(View.GONE);
//                iv_show.setVisibility(View.VISIBLE);
                // 拿到拍照照片数据
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);//由缓冲区存入字节数组
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null && bitmap_rew!=0 && bitmap_reh!=0) {

                    Matrix matrix  = new Matrix();
                    matrix.setRotate(90);
                    new_bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                    new_bitmap=Crop_Bitmap (new_bitmap, new_bitmap.getWidth(), new_bitmap.getHeight()/3);
//                    t1.setText(get_View( new_bitmap));
                    Log.d("QQ","C");
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

    public boolean areWeFocused = false;
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {

            /*
            if (result.get(CaptureResult.CONTROL_AF_STATE) == CaptureResult.CONTROL_AF_TRIGGER_START) {
                Log.d("AAA", "YYYYYY" );
            }*/
            
            // Log.d("YYY", "process: " +  result.get(CaptureResult.CONTROL_AF_STATE).toString() );


            /*
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
            Log.d("AAA", String.valueOf(afState) );
            */
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
                    if (now_ocr < 1) {
                        now_ocr = 1;
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
        bitmap_rew=canvas.getWidth();
        bitmap_reh=canvas.getHeight()/3;
        Rect r = new Rect(0,canvas.getHeight()/3,canvas.getWidth(),canvas.getHeight()/3*2);
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



}
