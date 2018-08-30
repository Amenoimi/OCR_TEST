package com.example.amenoimi.ocr_test;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by EienYuki on 2018/8/28.
 * 說明：這是一個簡單的掃描四邊正方形定位點的程式
 */

public class ScanMarkingPoint {
    private float[] config;

    private Paint p;

    ScanMarkingPoint() {
        // 以下參數都是百分比
        // 距離左, 距離上, 範圍寬, 範圍高, 小正方形長寬(依照寬度比例)
        this.config = new float[] {0.25f, 0.25f, 0.54f, 0.3f, 0.05f};

        this.p = new Paint();
    }
    ScanMarkingPoint(float[] config) {
        // 以下參數都是百分比
        // 距離左, 距離上, 範圍寬, 範圍高, 小正方形長寬(依照寬度比例)
        this.config = config;

        this.p = new Paint();
    }

    // 繪製邊框
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

    // 識別邊框
    // for 一般情況
    public int[] findrect(Bitmap ScanTarget) {
        int ok = 0;
        int w = ScanTarget.getWidth();
        int h = ScanTarget.getHeight();

        int mw = (int)(w * this.config[4]);
        int mh = mw;

        int left = (int)(w * this.config[0]);
        int top = (int)(h * this.config[0]);
        int right = (int)(left + w * this.config[2]);
        int bottom = (int)(top + h * this.config[3]);


        float S = 0, V = 0;
        int n = 0;
        float[] hsv = new float[3];

        // 找定位點
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

        if (S > 0.20 && V < 0.70) ok+=1;
        // 找定位點 END

        if (ok == 1) {
            return new int[] {
                    left + mw,
                    top + mh,
                    right - mw,
                    bottom - mh
            };
        }
        else {
            return new int[] {};
        }

    }

    // for 一般情況
    public int[] findrect2(Bitmap ScanTarget) {
        int ok = 0;
        int w = ScanTarget.getWidth();
        int h = ScanTarget.getHeight();

        int mw = (int)(w * this.config[4]);
        int mh = mw;

        int left = (int)(w * this.config[0]);
        int top = (int)(h * this.config[0]);
        int right = (int)(left + w * this.config[2]);
        int bottom = (int)(top + h * this.config[3]);

        float S = 0, V = 0;
        float pointS = 0, pointV = 0;
        float bkS = 0, bkV = 0;
        int n = 0;
        float[] hsv = new float[3];

        // 找定位點
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
        pointS = S;
        pointV = V;

        if (S > 0.3) ok+=1;
        // 找定位點 END

        // 確認在白紙
        S = 0;
        V = 0;
        n = 0;

        for (int y=top+mh; y<bottom - mh; y++ ){
            for (int x=left; x<left+mw; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        for (int y=top; y<top+mh; y++ ){
            for (int x=left+mw; x<right - mw; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        for (int y=bottom - mh; y<bottom; y++ ){
            for (int x=left+mw; x<right - mw; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        for (int y=top+mh; y<bottom - mh; y++ ){
            for (int x=right - mw; x<right; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        S = S/n;
        V = V/n;
        bkS = S;
        bkV = V;

        if (S < 0.1) ok+=1;
        // 確認在白紙 END

        Log.d("FR", "OK: " + ok);
        Log.d("FR", "pointSV: " + pointS + " ," + pointV);
        Log.d("FR", "bkSV: " + bkS + " ," + bkV);
        if ( (pointS - bkS > 0.3) && (ok == 2) ) {
            return new int[] {
                    left + mw,
                    top + mh,
                    right - mw,
                    bottom - mh
            };
        }
        else {
            return new int[] {};
        }

    }

    // for 二值化
    public int[] findrect3(Bitmap ScanTarget) {
        int ok = 0;
        int w = ScanTarget.getWidth();
        int h = ScanTarget.getHeight();

        int mw = (int)(w * this.config[4]);
        int mh = mw;

        int left = (int)(w * this.config[0]);
        int top = (int)(h * this.config[0]);
        int right = (int)(left + w * this.config[2]);
        int bottom = (int)(top + h * this.config[3]);

        float S = 0, V = 0;
        float pointS = 0, pointV = 0;
        float bkS = 0, bkV = 0;
        int n = 0;
        float[] hsv = new float[3];

        // 找定位點
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
        pointS = S;
        pointV = V;

        if (V < 0.8) ok+=1;
        // 找定位點 END

        // 確認在白紙
        S = 0;
        V = 0;
        n = 0;

        for (int y=top+mh; y<bottom - mh; y++ ){
            for (int x=left; x<left+mw; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        for (int y=top; y<top+mh; y++ ){
            for (int x=left+mw; x<right - mw; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        for (int y=bottom - mh; y<bottom; y++ ){
            for (int x=left+mw; x<right - mw; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        for (int y=top+mh; y<bottom - mh; y++ ){
            for (int x=right - mw; x<right; x++ ){
                Color.colorToHSV(ScanTarget.getPixel(x, y), hsv);
                S += hsv[1];
                V += hsv[2];
                n++;
            }
        }

        S = S/n;
        V = V/n;
        bkS = S;
        bkV = V;

        if (V > 0.9) ok+=1;
        // 確認在白紙 END

        Log.d("FR", "OK: " + ok);
        Log.d("FR", "pointSV: " + pointS + " ," + pointV);
        Log.d("FR", "bkSV: " + bkS + " ," + bkV);
        if ( ok == 2 ) {
            return new int[] {
                    left + mw,
                    top + mh,
                    right - mw,
                    bottom - mh
            };
        }
        else {
            return new int[] {};
        }

    }
}
