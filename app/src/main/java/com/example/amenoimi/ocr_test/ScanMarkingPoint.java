package com.example.amenoimi.ocr_test;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by EienYuki on 2018/8/28.
 * 說明：這是一個簡單的掃描四邊正方形定位點的程式
 *
 * Step1. 設定 Config 關係到定位點的呈現
 * Step2. 呼叫 drawing 繪製定位點
 * Step3. 呼叫 findrect 判斷有沒有找到定位點 ps:目前測試第三個可以使用
 */

public class ScanMarkingPoint {
    private float[] Config;
    private float[] Bias = new float[] {1, 1};

    private Paint p;

    ScanMarkingPoint() {
        this.Config = new float[] {0.25f, 0.25f, 0.54f, 0.3f, 0.05f};

        this.p = new Paint();
    }

    ScanMarkingPoint(float[] Config) {
        this.Config = Config;

        this.p = new Paint();
    }

    // 關係到 定位點的呈現 設定值都是百分比
    // 距離左(x), 距離上(y), 範圍寬(width), 範圍高(height), 小正方形長寬[依照寬度比例](square)
    public void setConfig(float left, float top, float width, float height, float square) {
        this.Config = new float[]{left, top, width, height, square};
    }

    // 用於偏移 findrect 輸出的座標
    public void setBias(float left, float top) {
        this.Bias = new float[]{left, top};
    }

    // 繪製邊框、正方形定位點 於畫布上
    public void drawing(Canvas ScreenTarget) {
        int w = ScreenTarget.getWidth();
        int h = ScreenTarget.getHeight();

        int mw = (int)(w * this.Config[4]);
        int mh = mw;

        float left = w * this.Config[0];
        float top = h * this.Config[1];
        float right = left + w * this.Config[2];
        float bottom = top + h * this.Config[3];


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

    // 識別 正方形定位點 - 只找尋定位點的版本
    public int[] findrect(Bitmap ScanTarget) {
        int ok = 0;
        int w = ScanTarget.getWidth();
        int h = ScanTarget.getHeight();

        int mw = (int)(w * this.Config[4]);
        int mh = mw;

        int left = (int)(w * this.Config[0]);
        int top = (int)(h * this.Config[1]);
        int right = (int)(left + w * this.Config[2]);
        int bottom = (int)(top + h * this.Config[3]);


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
                    left + (int)(mw * this.Bias[0]),
                    top + (int)(mh * this.Bias[1]),
                    right - mw,
                    bottom - mh
            };
        }
        else {
            return new int[] {};
        }

    }

    // 識別 正方形定位點 - 找尋定位點、確認再白紙上 的版本
    public int[] findrect2(Bitmap ScanTarget) {
        int ok = 0;
        int w = ScanTarget.getWidth();
        int h = ScanTarget.getHeight();

        int mw = (int)(w * this.Config[4]);
        int mh = mw;

        int left = (int)(w * this.Config[0]);
        int top = (int)(h * this.Config[1]);
        int right = (int)(left + w * this.Config[2]);
        int bottom = (int)(top + h * this.Config[3]);

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
        if (ok == 2) {
            return new int[] {
                    left + (int)(mw * this.Bias[0]),
                    top + (int)(mh * this.Bias[1]),
                    right - mw,
                    bottom - mh
            };
        }
        else {
            return new int[] {};
        }

    }

    // 識別 正方形定位點 - 找尋定位點、確認再白紙上 的版本 (來源爲 二值化 的 Bitmap)
    public int[] findrect3(Bitmap ScanTarget) {
        int ok = 0;
        int w = ScanTarget.getWidth();
        int h = ScanTarget.getHeight();

        int mw = (int)(w * this.Config[4]);
        int mh = mw;

        int left = (int)(w * this.Config[0]);
        int top = (int)(h * this.Config[1]);
        int right = (int)(left + w * this.Config[2]);
        int bottom = (int)(top + h * this.Config[3]);

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
        if (ok == 2) {
            return new int[] {
                    left + (int)(mw * this.Bias[0]),
                    top + (int)(mh * this.Bias[1]),
                    right - mw,
                    bottom - mh
            };
        }
        else {
            return new int[] {};
        }

    }
}
