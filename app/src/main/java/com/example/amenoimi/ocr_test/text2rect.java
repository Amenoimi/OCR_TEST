package com.example.amenoimi.ocr_test;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.cv4j.core.binary.ConnectedAreaLabel;
import com.cv4j.core.binary.MorphOpen;
import com.cv4j.core.binary.Threshold;
import com.cv4j.core.datamodel.ByteProcessor;
import com.cv4j.core.datamodel.CV4JImage;
import com.cv4j.core.datamodel.ImageProcessor;
import com.cv4j.core.datamodel.Rect;
import com.cv4j.core.datamodel.Size;
import com.cv4j.image.util.Preconditions;
import com.cv4j.image.util.Tools;

import java.util.ArrayList;
import java.util.List;



public class text2rect {
    Bitmap new_bitmap;
    text2rect(Bitmap bitmap){
        new_bitmap=bitmap;
    }
   public Bitmap img(){
        CV4JImage cv4JImage = new CV4JImage(new_bitmap);
        ImageProcessor img= cv4JImage.getProcessor();
        Threshold threshold = new Threshold();
        threshold.process((ByteProcessor)(cv4JImage.convert2Gray().getProcessor()),Threshold.THRESH_TRIANGLE,Threshold.METHOD_THRESH_BINARY_INV,255);
        MorphOpen morphOpen = new MorphOpen();
        cv4JImage.resetBitmap();
        morphOpen.process((ByteProcessor)cv4JImage.getProcessor(),new Size(1));//Size是受侵蝕程度

        ConnectedAreaLabel connectedAreaLabel = new ConnectedAreaLabel();
        int[] mask = new int[cv4JImage.getProcessor().getWidth() * cv4JImage.getProcessor().getHeight()];
        List<Rect> rectangles = new ArrayList<>();
        connectedAreaLabel.process((ByteProcessor)cv4JImage.getProcessor(), mask,rectangles,true);
        cv4JImage.resetBitmap();
        new_bitmap = cv4JImage.getProcessor().getImage().toBitmap();

        if (Preconditions.isNotBlank(rectangles)) {
            Tools.drawRects(new_bitmap,rectangles);
            new_bitmap=ARGBBitmap(new_bitmap);

            return new_bitmap;
        }
       return null;
    }

    public  List<Rect>  rect(){
        CV4JImage cv4JImage = new CV4JImage(new_bitmap);
        ImageProcessor img= cv4JImage.getProcessor();
        Threshold threshold = new Threshold();
        threshold.process((ByteProcessor)(cv4JImage.convert2Gray().getProcessor()),Threshold.THRESH_TRIANGLE,Threshold.METHOD_THRESH_BINARY_INV,255);
        MorphOpen morphOpen = new MorphOpen();
        cv4JImage.resetBitmap();
        morphOpen.process((ByteProcessor)cv4JImage.getProcessor(),new Size(1));//Size是受侵蝕程度

        ConnectedAreaLabel connectedAreaLabel = new ConnectedAreaLabel();
        int[] mask = new int[cv4JImage.getProcessor().getWidth() * cv4JImage.getProcessor().getHeight()];
        List<Rect> rectangles = new ArrayList<>();
        connectedAreaLabel.process((ByteProcessor)cv4JImage.getProcessor(), mask,rectangles,true);
        cv4JImage.resetBitmap();
        new_bitmap = cv4JImage.getProcessor().getImage().toBitmap();

        if (Preconditions.isNotBlank(rectangles)) {
            Tools.drawRects(new_bitmap,rectangles);
            new_bitmap=ARGBBitmap(new_bitmap);

            return rectangles;
        }
        return null;
    }

    private Bitmap ARGBBitmap(Bitmap img) {
        return img.copy(Bitmap.Config.ARGB_8888,true);
    }

}
