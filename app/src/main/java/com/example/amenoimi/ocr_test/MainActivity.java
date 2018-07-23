package com.example.amenoimi.ocr_test;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import static android.os.Environment.getDataDirectory;
import static android.os.Environment.getDownloadCacheDirectory;
import static android.os.Environment.getRootDirectory;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{
    static String TESSBASE_PATH;
    static final String DEFAULT_LANGUAGE = "eng";
    static final String CHINESE_LANGUAGE = "chi_tra";
    static final String CHINESE_LANGUAGE_SIM = "chi_sim";
    private ImageView imgSrc;
    public TextView t1;
    public Button b1,b2,b3;
    public static final int progressType = 0;
    private ProgressDialog progressDialog;
    public int ImgToTextMode=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgSrc=(ImageView)findViewById(R.id.imageView);
        t1=(TextView)findViewById(R.id.t1);
        b1=(Button)findViewById(R.id.b1);
        b2=(Button)findViewById(R.id.b2);
        b3=(Button)findViewById(R.id.b3);
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);
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

        try {
            TESSBASE_PATH =getDataDir(getApplicationContext());
            isExist(getDataDir(getApplicationContext())+"/tessdata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/chi_tra.traineddata"))myDownload("chi_tra.traineddata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/chi_sim.traineddata")) myDownload("chi_sim.traineddata");
            if(!fileIsExists(getDataDir(getApplicationContext())+"/tessdata/eng.traineddata")) myDownload("eng.traineddata");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void up_mode(View v){
        try {
            myDownload("chi_tra.traineddata");
            myDownload("chi_sim.traineddata");
            myDownload("eng.traineddata");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void get_img(View v){
        final String mimeType = "image/*";
        Intent intent = new Intent();
        intent.setType(mimeType);
        intent.setAction(Intent.ACTION_GET_CONTENT);
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
        if( v.getId()==R.id.b3)up_mode(v);
    }
    public String ocrWithEnglish() {
        String resString = "";

        imgSrc.setDrawingCacheEnabled(true);
        final Bitmap bitmap = imgSrc.getDrawingCache();
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
        }

        ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD    );

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


    public void myDownload(String mod) throws Exception {
        try {
            String path = getDataDir(getApplicationContext());
            path += "/tessdata/"+mod;
            new DownloadFromURL().execute("https://github.com/tesseract-ocr/tessdata/raw/master/"+mod, path);
        } catch (Exception e) {
            e.printStackTrace();
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
                progressDialog.show();
                return progressDialog;
            default:
                return null;
        }
    }
    public static Bitmap convertToBMW(Bitmap bmp, int w, int h,int tmp) {
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        // 设定二值化的域值，默认值为100
        //tmp = 180;
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                // 分离三原色
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
        // 新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, w, h);
        return resizeBmp;
    }}
