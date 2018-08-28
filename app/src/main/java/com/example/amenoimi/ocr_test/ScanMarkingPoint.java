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
        // 距離上 距離下 範圍長 範圍寬 小正方形長 小正方形寬
        this.config = new float[] {0.25f, 0.25f, 0.5f, 0.5f, 0.2f, 0.2f};

        this.p = new Paint();
    }
    ScanMarkingPoint(Canvas ScreenTarget, Bitmap ScanTarget, int[] config) {
        this.ScreenTarget = ScreenTarget;
        this.ScanTarget = ScanTarget;

        // 距離上 距離下 範圍長 範圍寬 小正方形長 小正方形寬
        this.config = new float[] {0.25f, 0.25f, 0.5f, 0.5f, 0.2f, 0.2f};

        this.p = new Paint();
    }

    public void drawing(Canvas ScreenTarget) {
        int w = ScreenTarget.getWidth();
        int h = ScreenTarget.getHeight();

        int mw = (int) (w * this.config[4]) ;
        int mh = (int) (h * this.config[5]) ;

        float left = w * this.config[0];
        float top = h * this.config[0];
        float right = left + w * this.config[2];
        float bottom = top + h * this.config[3];


        p.setColor(Color.parseColor("#006ac6"));
        p.setStyle(Paint.Style.FILL);
        ScreenTarget.drawRect(left, top, right, bottom, p);

        p.setColor(Color.parseColor("#183292"));
        ScreenTarget.drawRect(left, top, left+mw, top+mh, p);
        ScreenTarget.drawRect(left, bottom - mh, left+mw, bottom, p);
        ScreenTarget.drawRect(right - mw, top, right, top+mh, p);
        ScreenTarget.drawRect(right - mw, bottom - mh, right, bottom, p);
    }
}
