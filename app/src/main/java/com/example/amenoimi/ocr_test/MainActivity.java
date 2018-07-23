package com.example.amenoimi.ocr_test;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.googlecode.tesseract.android.TessBaseAPI;

import static android.os.Environment.getDataDirectory;
import static android.os.Environment.getDownloadCacheDirectory;
import static android.os.Environment.getRootDirectory;

public class MainActivity extends AppCompatActivity {
    static final String TESSBASE_PATH =getDataDirectory().toString();
    static final String DEFAULT_LANGUAGE = "eng";
    static final String CHINESE_LANGUAGE = "chi_tra";
    private View imgSrc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
Log.d("QQ",getRootDirectory().toString());
    }

    public String ocrWithEnglish() {
        String resString = "";

        imgSrc.setDrawingCacheEnabled(true);
        final Bitmap bitmap = imgSrc.getDrawingCache();
        final TessBaseAPI ocrApi = new TessBaseAPI();

        ocrApi.init(TESSBASE_PATH, CHINESE_LANGUAGE);
        ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);

        ocrApi.setImage(bitmap);
        resString = ocrApi.getUTF8Text();

        ocrApi.clear();
        ocrApi.end();
        return  resString;
    }

}
