package com.example.amenoimi.ocr_test;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.util.Calendar;

import static android.os.Environment.getDataDirectory;
import static android.os.Environment.getDownloadCacheDirectory;
import static android.os.Environment.getRootDirectory;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{
    static final String TESSBASE_PATH ="/sdcard/download/tesseract/";
    static final String DEFAULT_LANGUAGE = "eng";
    static final String CHINESE_LANGUAGE = "chi_tra";
    private ImageView imgSrc;
    public TextView t1;
    public Button b1,b2;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgSrc=(ImageView)findViewById(R.id.imageView);
        t1=(TextView)findViewById(R.id.t1);
        b1=(Button)findViewById(R.id.b1);
        b2=(Button)findViewById(R.id.b2);
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
    }

    public void get_img(View v){
        final String mimeType = "image/*";

           //讀取圖片
        Intent intent = new Intent();
        //開啟Pictures畫面Type設定為image
        intent.setType(mimeType);
        //使用Intent.ACTION_GET_CONTENT這個Action
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //取得照片後返回此畫面
        startActivityForResult(intent, 0);




    }
    public void get_img_now(View v){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        /**
         * 1、注意这里，如果指定了Uri，则在onActivityResult中的 intent data  将返回null
         * 2、如果不指定Uri的话，将可以在onActivityResult中 通过data.getParcelableExtra("data")获取bitmap对象，
         * 而这个bitmap对象是被压缩的,非常模糊，我想这不是我们想要的
         * 3、也有的地方说，没有指定Uri 则会默认保存，然后通过Uri.getData()就可以获得这个图片的Uri。但是我测试了一下模拟器和真机都
         * 不是这样的 ，是上述1,2两种情况。可能与手机有关，所以用上述两种方法比较靠谱。
         */
        Calendar cal = Calendar.getInstance();
        Uri saveUri = Uri.fromFile(new File(getExternalFilesDir(Environment.DIRECTORY_DCIM), cal.getTime().toString()+".jpg"));

        intent.putExtra(MediaStore.EXTRA_OUTPUT,saveUri);

        startActivityForResult(intent,12);


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressDialog = new ProgressDialog(this);
//                    progressDialog.setTitle("");
        progressDialog.setMessage("識別中.....");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        // 取得檔案的 Uri
        Log.d("QUQ",data.toString());
        Uri uri = data.getData();
        ContentResolver cr = this.getContentResolver();

            if(requestCode==0){
                if ( resultCode == RESULT_OK )
                {
                    // 有選擇檔案
                    try {
                        if (uri != null) {

                            imgSrc.setImageURI(uri);
                            t1.setText(ocrWithEnglish());
                            progressDialog.dismiss();
                        }
                    }catch (IOError e){

                    }
                }
            }else  if(requestCode==12){
                if ( resultCode == RESULT_OK )
                {
                    try {
                        if (uri != null) {

                            imgSrc.setImageURI(uri);
                            t1.setText(ocrWithEnglish());
                            progressDialog.dismiss();
                        }
                    }catch (IOError e){

                    }
                }
            }



    }
    @Override
    public void onClick(View v) {
        if( v.getId()==R.id.b1)get_img(v);
        if( v.getId()==R.id.b2)get_img_now(v);
    }
    public String ocrWithEnglish() {
        String resString = "";

        imgSrc.setDrawingCacheEnabled(true);
        final Bitmap bitmap = imgSrc.getDrawingCache();
        final TessBaseAPI ocrApi = new TessBaseAPI();

        ocrApi.init(TESSBASE_PATH, CHINESE_LANGUAGE);
        ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD    );

        ocrApi.setImage(bitmap);
        resString = ocrApi.getUTF8Text();

        ocrApi.clear();
        ocrApi.end();
        return  resString;
    }

}
