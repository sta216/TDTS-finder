/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.tdtsfinder;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;


import com.google.android.gms.samples.vision.tdtsfinder.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;


import java.util.ArrayList;
import java.util.List;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class OcrGraphic extends GraphicOverlay.Graphic {

    private int mId;

    private static final int TEXT_COLOR = Color.WHITE;

    private static Paint sRectPaint;
    private static Paint sTextPaint;
    private final TextBlock mText;


    private String sText ="";

    private int cSum = 0;
    private int dSum = 0;

    public ArrayList sWagons = new ArrayList() ;

    OcrGraphic(GraphicOverlay overlay, TextBlock text) {
        super(overlay);

        mText = text;

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(TEXT_COLOR);
            sRectPaint.setStyle(Paint.Style.STROKE);
            sRectPaint.setStrokeWidth(4.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(54.0f);
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public TextBlock getTextBlock() {
        return mText;
    }

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */

    public boolean contains(float x, float y) {
        // TODO: Check if this graphic's text contains this point.
        if (mText == null) {
            return false;
        }
        RectF rect = new RectF(mText.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);
    }
    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        if (mText == null) {
            return;
        }

        // Draws the bounding box around the TextBlock.
        RectF rect = new RectF(mText.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);


        sRectPaint.setColor(Color.RED);
        sTextPaint.setColor(Color.RED);
        sTextPaint.setTextSize(20);
        // Checks for control digit in wagon number (for filtering other records (i.e. owner code, manufactured date and others )
        if(mText.getValue().trim().replaceAll("\\D","").length()==8 && mText.getValue().trim().replaceAll("\\D","").matches("\\d+")) {
            sText = mText.getValue().trim().replaceAll("\\D","");
            cSum = 0;
            dSum = 0;
            for (int i =0; i<=6; i++) {
                dSum = Integer.valueOf(sText.substring(i, i + 1)) * (2 - (i % 2));
                cSum += dSum % 10;
                if (dSum > 9) {
                    cSum += 1;
                }
            }
            cSum = 10-(cSum%10);
            cSum -= Integer.valueOf(sText.substring(7, 8));
            if(cSum==0) {
                // text blocks that have wagon numbers - highlight`s with green frame (other highlight`s with red frame)
                sRectPaint.setColor(Color.GREEN);
                sTextPaint.setColor(Color.GREEN);
                sTextPaint.setTextSize(46);
                // TODO: save wagon number

                if(! sWagons.contains(sText))
                {

                    sWagons.add(sWagons.size(),sText);

                }



            }
        }



        canvas.drawRect(rect, sRectPaint);

        // Break the text into multiple lines and draw each one according to its own bounding box.
        List<? extends Text> textComponents = mText.getComponents();
        for(Text currentText : textComponents) {
            float left = translateX(currentText.getBoundingBox().left+10);
            float bottom = translateY(currentText.getBoundingBox().bottom-10);
            canvas.drawText(currentText.getValue(), left, bottom, sTextPaint);
        }
    }
}
