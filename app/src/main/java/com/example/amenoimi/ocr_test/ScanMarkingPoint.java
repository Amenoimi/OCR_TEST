package com.example.amenoimi.ocr_test;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by midori on 2018/8/28.
 */

public class ScanMarkingPoint {
    private Canvas ScreenTarget;
    private Bitmap ScanTarget;
    private float[] config;

    private Paint p;

    ScanMarkingPoint() {
        // 距離左 距離上 範圍寬 範圍高 小正方形長寬(依照寬度比例)
        this.config = new float[] {0.25f, 0.25f, 0.5f, 0.5f, 0.1f};

        this.p = new Paint();
    }
    ScanMarkingPoint(Canvas ScreenTarget, Bitmap ScanTarget, int[] config) {
        this.ScreenTarget = ScreenTarget;
        this.ScanTarget = ScanTarget;

        // 距離上 距離下 範圍長 範圍寬 小正方形長寬(依照寬度比例)
        this.config = new float[] {0.25f, 0.25f, 0.5f, 0.5f, 0.1f};

        this.p = new Paint();
    }

    public void drawing(Canvas ScreenTarget) {
        int w = ScreenTarget.getWidth();
        int h = ScreenTarget.getHeight();

        int mw = (int)(w * this.config[4]);
        int mh = mw;

        float left = w * this.config[0];
        float top = h * this.config[0];
        float right = left + w * this.config[2];
        float bottom = top + h * this.config[3];


        p.setColor(Color.parseColor("#006ac6"));
        p.setStyle(Paint.Style.STROKE);
        ScreenTarget.drawRect(left, top, right, bottom, p);

        p.setColor(Color.parseColor("#183292"));
        p.setStyle(Paint.Style.FILL);//設置填滿
        ScreenTarget.drawRect(left, top, left+mw, top+mh, p);
        ScreenTarget.drawRect(left, bottom - mh, left+mw, bottom, p);
        ScreenTarget.drawRect(right - mw, top, right, top+mh, p);
        ScreenTarget.drawRect(right - mw, bottom - mh, right, bottom, p);
    }

    public boolean findrect(Bitmap ScanTarget, int[] scale_error) {
        int w = ScanTarget.getWidth() * scale_error[0];
        int h = ScanTarget.getHeight() * scale_error[1];

        int mw = (int)(w * this.config[4]);
        int mh = mw;

        int left = (int)(w * this.config[0]);
        int top = (int)(h * this.config[0]);
        int right = (int)(left + w * this.config[2]);
        int bottom = (int)(top + h * this.config[3]);

        int[] pixels = new int[w * h];
        ScanTarget.getPixels(pixels, 0, w, 0, 0, w, h);

        float S = 0, V = 0;
        int n = 0;
        float[] hsv = new float[3];

        for (int y=top; y<top+mh; y++ ){
            for (int x=left; x<left+mw; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        for (int y=bottom - mh; y<bottom; y++ ){
            for (int x=left; x<left+mw; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        for (int y=top; y<top+mh; y++ ){
            for (int x=right - mw; x<right; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        for (int y=bottom - mh; y<bottom; y++ ){
            for (int x=right - mw; x<right; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        S = S/n;
        V = V/n;

        if (S > 0.25 && V > 0.75) {
            return true;
        } else {
            return false;
        }

    }
}