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
package com.example.adaptablecurlpage.flipping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;

import com.eschao.android.widget.pageflip.OnPageFlipListener;
import com.eschao.android.widget.pageflip.PageFlip;

/**
 * Abstract Page Render
 *
 * @author eschao
 */

public abstract class PageRender implements OnPageFlipListener {

    public final static int MSG_ENDED_DRAWING_FRAME = 1;
    public final static String TAG = "PageRender";

    final static int DRAW_MOVING_FRAME = 0;
    final static int DRAW_ANIMATING_FRAME = 1;
    final static int DRAW_FULL_PAGE = 2;
    final static int RESTORE_PAGE = 3;
    static int MAX_PAGES = 1;

    int mPageNo;
    Matrix bitmap_matrix;
    float scale;
    int mDrawCommand;
    Bitmap mBitmap;
    Canvas mCanvas;
    Bitmap mBackgroundBitmap;
    Context mContext;
    Handler mHandler;
    PageFlip mPageFlip;

    public PageRender(Context context, PageFlip pageFlip,
                      Handler handler, int pageNo) {
        mContext = context;
        mPageFlip = pageFlip;
        mPageNo = pageNo;
        mDrawCommand = DRAW_FULL_PAGE;
        mCanvas = new Canvas();
        mPageFlip.setListener(this);
        mHandler = handler;
    }

    public static void setMaxPages(int maxPages) {
        MAX_PAGES = maxPages;
    }

    public static int getMaxPages() {
        return MAX_PAGES;
    }

    /**
     * Get page number
     *
     * @return page number
     */
    public int getPageNo() {
        return mPageNo;
    }

    public int setmPageNo(int mPageNo) {
        this.mPageNo = mPageNo;
        return this.mPageNo;
    }

    /**
     * Release resources
     */
    public void release() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        mPageFlip.setListener(null);
        mCanvas = null;
        mBackgroundBitmap = null;
    }

    /**
     * Handle finger moving event
     *
     * @param x x coordinate of finger moving
     * @param y y coordinate of finger moving
     * @return true if event is handled
     */
    public boolean onFingerMove(float x, float y) {
        mDrawCommand = DRAW_MOVING_FRAME;
        return true;
    }

    public boolean onRestore(float x, float y) {
        if (mPageFlip.animating()) {
            mDrawCommand = DRAW_FULL_PAGE;
            return true;
        }
        return false;
    }

    public boolean onRefreshPage() {
//        if (mPageFlip.animating()) {
            mDrawCommand = DRAW_FULL_PAGE;
            return true;
//        }
//        return false;
    }

    /**
     * Handle finger up event
     *
     * @param x x coordinate of finger up
     * @param y y coordinate of inger up
     * @return true if event is handled
     */
    public boolean onFingerUp(float x, float y) {
        if (mPageFlip.animating()) {
            mDrawCommand = DRAW_ANIMATING_FRAME;
            return true;
        }

        return false;
    }

    /**
     * Calculate font size by given SP unit
     */
    protected int calcFontSize(int size) {
        return (int) (size * mContext.getResources().getDisplayMetrics()
                .scaledDensity);
    }

    public void setBitmap_matrix(Matrix matrix) {
        bitmap_matrix = matrix;
    }

    public void setBitmap_scale(float scale) {
        this.scale = scale;
    }

    /**
     * Render page frame
     */
    abstract void onDrawFrame();

    /**
     * Handle surface changing event
     *
     * @param width  surface width
     * @param height surface height
     */
    abstract void onSurfaceChanged(int width, int height);

    /**
     * Handle drawing ended event
     *
     * @param what draw command
     * @return true if render is needed
     */
    public abstract boolean onEndedDrawing(int what);
}
