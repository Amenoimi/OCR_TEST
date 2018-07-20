package com.example.amenoimi.ocr_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    static final String TESSBASE_PATH = "/storage/emulated/0/download/tesseract/";
    static final String DEFAULT_LANGUAGE = "eng";
    static final String CHINESE_LANGUAGE = "chi_tra";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
