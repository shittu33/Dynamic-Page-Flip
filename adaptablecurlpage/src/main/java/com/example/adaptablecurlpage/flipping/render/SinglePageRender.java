/*
 * Copyright (C) 2016 eschao <esc.chao@gmail.com>
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
package com.example.adaptablecurlpage.flipping.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.eschao.android.widget.pageflip.Page;
import com.eschao.android.widget.pageflip.PageFlip;
import com.eschao.android.widget.pageflip.PageFlipState;
import com.example.adaptablecurlpage.flipping.utils.BitmapLoader;
import com.example.adaptablecurlpage.flipping.utils.DrawingState;
import com.example.adaptablecurlpage.flipping.views.PageFlipView;

/**
 * Single page render
 * <p>
 * Every page need 2 texture in single page mode:
 * <ul>
 * <li>First texture: current page content</li>
 * <li>Back texture: back of front content, it is same with first texture
 * </li>
 * <li>Second texture: next page content</li>
 * </ul>
 * </p>
 *
 * @author eschao
 */

public class SinglePageRender extends PageRender {
    private static final String TAG1 = "SinglePageRender";
    private PageFlipView mpageFlipView;

    /**
     * Constructor
     *
     * @see {@link PageRender (Context, PageFlip , Handler, int)} (Context, PageFlip, Handler, int)}
     */
    public SinglePageRender(Context context, PageFlip pageFlip,
                            Handler handler, int pageNo, PageFlipView pageFlipView) {
        super(context, pageFlip, handler, pageNo);
        this.mpageFlipView = pageFlipView;
    }

    /**
     * Draw frame
     */
    @Override
    public void onDrawFrame() {
        // 1. delete unused textures
        mPageFlip.deleteUnusedTextures();
        Page page = mPageFlip.getFirstPage();
        // 2. handle drawing command triggered from finger moving and animating
        if (mDrawCommand == DRAW_MOVING_FRAME ||
                mDrawCommand == DRAW_ANIMATING_FRAME) {
            // is forward flip
            if (mPageFlip.getFlipState() == PageFlipState.FORWARD_FLIP) {
                // check if second texture of first page is valid, if not,
                // create new one
                if (!page.isSecondTextureSet()) {
                    drawPage(mPageNo + 1);
                    Log.i(TAG1, "DRAW_MOVING_FRAME-> page Number is " + mPageNo);
                    page.setSecondTexture(mBitmap);
                }
            }
            // in backward flip, check first texture of first page is valid
            else if (!page.isFirstTextureSet()) {
                drawPage(--mPageNo);
                Log.i(TAG1, "DRAW_MOVING_FRAME-> page Number is " + mPageNo);
                page.setFirstTexture(mBitmap);
            }
            // draw frame for page flip
            mPageFlip.drawFlipFrame();
        }
        // draw stationary page without flipping
        else if (mDrawCommand == DRAW_FULL_PAGE) {
            DrawingState drawingState = mpageFlipView.mDynamicFlipView.getmDrawingState();
            if (!page.isFirstTextureSet()) {
                drawPage(mPageNo);
                Log.i(TAG1, "page Number is " + mPageNo);
                page.setFirstTexture(mBitmap);
            } else if (drawingState == DrawingState.REFRESH_DRAW) {
                background = null;
//                page.deleteAllTextures();
                drawPage(mPageNo);
                Log.i(TAG1, "page Number is " + mPageNo);
                page.setFirstTexture(mBitmap);
                mpageFlipView.mDynamicFlipView.setmDrawingState(DrawingState.NORMAL_DRAW);
                Log.i(TAG1, "texture is already set");
            }
            mPageFlip.drawPageFrame();
        }

        // 3. send message to main thread to notify drawing is ended so that
        // we can continue to calculate next animation frame if need.
        // Remember: the drawing operation is always in GL thread instead of
        // main thread
        Message msg = Message.obtain();
        msg.what = MSG_ENDED_DRAWING_FRAME;
        msg.arg1 = mDrawCommand;
        mHandler.sendMessage(msg);
    }

    /**
     * Handle ended drawing event
     * In here, we only tackle the animation drawing event, If we need to
     * continue requesting render, please return true. Remember this function
     * will be called in main thread
     *
     * @param what event type
     * @return ture if need render again
     */
    @Override
    public boolean onEndedDrawing(int what) {
        if (what == DRAW_ANIMATING_FRAME) {
            boolean isAnimating = mPageFlip.animating();
            // continue animating
            if (isAnimating) {
                mDrawCommand = DRAW_ANIMATING_FRAME;
                return true;
            }
            // animation is finished
            else {
                final PageFlipState state = mPageFlip.getFlipState();
                if (state == PageFlipState.END_WITH_BACKWARD) {
                    // update page number for backward flip
                    // don't do anything on page number since mPageNo is always
                    // represents the FIRST_TEXTURE no;
                    // update page number and switch textures for forward flip
//                    mPageNo--;
                } else if (state == PageFlipState.END_WITH_FORWARD) {
                    mPageFlip.getFirstPage().setFirstTextureWithSecond();
                    mPageNo++;
                    Log.i(TAG1, "Ended Page number index ++ " + mPageNo);
                }
                mDrawCommand = DRAW_FULL_PAGE;
                mpageFlipView.mDynamicFlipView.postFlippedToView(mPageNo);
                mpageFlipView.hideAnimation();
                Log.i(TAG1, "after flippedToView with mPageNo " + mPageNo + ", " + mPageNo);
                return true;
            }
        }
        return false;
    }

    /**
     * Handle GL surface is changed
     *
     * @param width  surface width
     * @param height surface height
     */
    @Override
    public void onSurfaceChanged(int width, int height) {
        // recycle bitmap resources if need
        if (mBackgroundBitmap != null) {
            mBackgroundBitmap.recycle();
        }

        if (mBitmap != null) {
            mBitmap.recycle();
        }

        // create bitmap and canvas for page
//        mBackgroundBitmap = background;
        Page page = mPageFlip.getFirstPage();
        mBitmap = Bitmap.createBitmap((int) page.width(), (int) page.height(),
                Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        //something
//        LoadBitmapTask.get(mContext).set(width, height, 1);
    }


    /**
     * Draw page content
     *
     * @param which page number
     */
    Bitmap background;

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    private void drawPage(int which) {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();
        Paint p = new Paint();
//        p.setColor(Color.YELLOW);
        p.setFilterBitmap(true);

        // 1. draw background bitmap
        Log.i(TAG, "before calling getBitmap");
        background = BitmapLoader.get(mContext).getBitmap(which);
        Rect rect = new Rect(0, 0, width, height);
        Log.i("active", "Calling onDraw()");
        try {
            mCanvas.drawBitmap(background, null, rect, p);
        } catch (Exception e) {
            e.printStackTrace();
            if (background == null) {
                mCanvas.drawColor(Color.YELLOW);
                Log.e("active", background == null ? "bitmap is null" : "bitmap okay");
            }
//            Log.e("active", "can't draw");
            Log.e("active", e.getMessage() == null ? "dont have any error message" : e.getMessage());
        }
        if (background != null) {
            background.recycle();
        }
        background = null;
        // 2. draw page number
//        int fontSize = calcFontSize(80);
//        p.setColor(Color.WHITE);
//        p.setStrokeWidth(1);
//        p.setAntiAlias(true);
//        p.setShadowLayer(5.0f, 8.0f, 8.0f, Color.BLACK);
//        p.setTextSize(fontSize);
//        String text = String.valueOf(which);
//        float textWidth = p.measureText(text);
//        float y = height - p.getTextSize() - 20;
//        mCanvas.drawText(text, (width - textWidth) / 2, y, p);
//
//        if (which <= 1) {
//            String firstPage = "The First Page";
//            p.setTextSize(calcFontSize(16));
//            float w = p.measureText(firstPage);
//            float h = p.getTextSize();
//            mCanvas.drawText(firstPage, (width - w) / 2, y + 5 + h, p);
//        }
//        else if (which >= MAX_PAGES) {
//            String lastPage = "The Last Page";
//            p.setTextSize(calcFontSize(16));
//            float w = p.measureText(lastPage);
//            float h = p.getTextSize();
//            mCanvas.drawText(lastPage, (width - w) / 2, y + 5 + h, p);
//        }
    }

    /**
     * If page can flip forward
     *
     * @return true if it can flip forward
     */
    @Override
    public boolean canFlipForward() {
        return (mPageNo < MAX_PAGES - 1);
    }


    /**
     * If page can flip backward
     *
     * @return true if it can flip backward
     */
    @Override
    public boolean canFlipBackward() {
        if (mPageNo > 0) {
            mPageFlip.getFirstPage().setSecondTextureWithFirst();
            return true;
        } else {
            return false;
        }
    }
}
