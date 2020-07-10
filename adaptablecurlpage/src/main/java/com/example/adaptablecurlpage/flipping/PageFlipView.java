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
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;

import com.eschao.android.widget.pageflip.PageFlip;
import com.eschao.android.widget.pageflip.PageFlipException;
import com.eschao.android.widget.pageflip.PageFlipState;
import com.example.adaptablecurlpage.flipping.utils.Constants;

import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Page flip view
 *
 * @author eschao
 */

public class PageFlipView extends GLSurfaceView implements Renderer {
    public DynamicFlipView mDynamicFlipView;
    private final static String TAG1 = "PageFlipView";
    public static final int NOTIFY_POSITION = 1;

    private final static String TAG = "active";
    int mPageNo;
    int mDuration;
    Handler mHandler;
    PageFlip mPageFlip;
    PageRender mPageRender;

    ReentrantLock mDrawLock;
    private int temp_index;

    //Can't use this constructor
    public PageFlipView(Context context) {
        super(context);
        init(context);
    }

    public PageFlipView(Context context, DynamicFlipView dynamicFlipView) {
        super(context);
        init(context);
        this.mDynamicFlipView = dynamicFlipView;
    }

    public PageFlipView(Context context, DynamicFlipView dynamicFlipView, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        this.mDynamicFlipView = dynamicFlipView;

    }

    public void setFlipDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    public int getmDuration() {
        return mDuration;
    }

    public void setMaxBackAlpha(float alpha) {
        mPageFlip.setMaskAlphaOfFold(alpha);
    }

    private void init(Context context) {
        // create handler to tackle message

        newHandler();

        mDuration = 1500/*600 before*/;
        int pixelsOfMesh = 10/*10 before*/;
        boolean isAuto = false;
        // create PageFlip
        mPageFlip = new PageFlip(context);
        mPageFlip.setWidthRatioOfClickToFlip(0.08f);
        mPageFlip.setSemiPerimeterRatio(0.79f)//0.99 b4//0.69f b4
                .setShadowWidthOfFoldEdges(5, 60, 0.3f)
                .setShadowWidthOfFoldBase(5, 80, 0.4f)
//                .setShadowWidthOfFoldEdges(7, 70, 0.4f)
//                .setShadowWidthOfFoldBase(7, 90, 0.5f)
                .setPixelsOfMesh(pixelsOfMesh)
                .enableAutoPage(isAuto);
        mPageNo = 0; // getting initial page_no
        mDrawLock = new ReentrantLock();
        mPageRender = new SinglePageRender(context, mPageFlip,
                mHandler, mPageNo, this);
        //config surfaceView
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //        setZOrderOnTop(true);
        // configure render
//        if(Build.VERSION.SDK_INT>=11){
//        }
//        setPreserveEGLContextOnPause(true);

    }

    public void setmPageNo(int mPageNo) {
        this.mPageNo = mPageNo;
    }

    public void resetPageNo(int position) {
        mPageNo = position;
        mPageRender.setmPageNo(mPageNo);
//        mPageRender.mPageNo = position;
//        requestRender();
        Log.i(TAG1, "New Page no " + mPageRender.mPageNo);
    }

    public PageRender getmPageRender() {
        return mPageRender;
    }


    public int getmPageNo() {
        return mPageNo;
    }

    /**
     * Is auto page mode enabled?
     *
     * @return true if auto page mode enabled
     */
    public boolean isAutoPageEnabled() {
        return mPageFlip.isAutoPageEnabled();
    }

    /**
     * Enable/Disable auto page mode
     *
     * @param enable true is enable
     */
    public void enableAutoPage(boolean enable) {
        if (mPageFlip.enableAutoPage(enable)) {
            try {
                mDrawLock.lock();
                if (mPageFlip.getSecondPage() != null &&
                        mPageRender instanceof SinglePageRender) {
                    mPageRender = new DoublePagesRender(getContext(),
                            mPageFlip,
                            mHandler,
                            mPageNo);
                    mPageRender.onSurfaceChanged(mPageFlip.getSurfaceWidth(),
                            mPageFlip.getSurfaceHeight());
                } else if (mPageFlip.getSecondPage() == null &&
                        mPageRender instanceof DoublePagesRender) {
                    mPageRender = new SinglePageRender(getContext(),
                            mPageFlip,
                            mHandler,
                            mPageNo, this);
                    mPageRender.onSurfaceChanged(mPageFlip.getSurfaceWidth(),
                            mPageFlip.getSurfaceHeight());
                }
                requestRender();
            } finally {
                mDrawLock.unlock();
            }
        }
    }

    /**
     * Get duration of animating
     *
     * @return duration of animating
     */
    public int getAnimateDuration() {
        return mDuration;
    }

    /**
     * Set animate duration
     *
     * @param duration duration of animating
     */
    public void setAnimateDuration(int duration) {
        mDuration = duration;
    }

    /**
     * Get pixels of mesh
     *
     * @return pixels of mesh
     */
    public int getPixelsOfMesh() {
        return mPageFlip.getPixelsOfMesh();
    }

    public PageFlip getmPageFlip() {
        return mPageFlip;
    }
//boolean

    /**
     * Handle finger down event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public void onFingerDown(float x, float y) {
//        adapterPageFlipView.setIs_click_trigger(true);
        // if the animation is going, we should ignore this event to avoid
        // mess drawing on screen
//        Log.i(TAG,"onDown Up X is " + x + " and Y is " + y);
//        setAlpha(1);
        if (!mPageFlip.isAnimating() && mPageFlip.getFirstPage() != null) {
            mPageFlip.onFingerDown(x, y);
        }
    }

    /**
     * Handle finger moving event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public void onFingerMove(float x, float y) {
//        adapterPageFlipView.setIs_click_trigger(false);
        if (mPageFlip.isAnimating()) {
            // nothing to do during animating
        } else if (mPageFlip.canAnimate(x, y)) {
            // if the point is out of current page, try to start animating
            onFingerUp(x, y);
            Log.i("MissingNumber", "Render Requested onFingerMove");

        }
        // move page by finger
        else if (mPageFlip.onFingerMove(x, y)) {
            try {
                mDrawLock.lock();
                if (mPageRender != null && mPageRender.onFingerMove(x, y)) {
                    requestRender();
                }
            } finally {
                mDrawLock.unlock();
            }
        }
    }

    /**
     * Handle finger up event and start animating if need
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public void onFingerUp(float x, float y) {
        if (!mPageFlip.isAnimating()) {
            mPageFlip.onFingerUp(x, y, mDuration);
            try {
                mDrawLock.lock();
                if (mPageRender != null && mPageRender.onFingerUp(x, y)) {
                    UpdateFromAdapter();
                    requestRender();
                    mDynamicFlipView.checkViewStatus();
                    setAlpha(0);
                }
            } finally {
                mDrawLock.unlock();
                setAlpha(0);
            }
        }
    }

    public boolean is_click_trigger() {
        Log.i(TAG1, mDynamicFlipView.is_click_trigger ? "is click" : "is move");
        return mDynamicFlipView.is_click_trigger;
    }

    public void UpdateFromAdapter() {
        if (mPageFlip.getFlipState() == PageFlipState.FORWARD_FLIP) {
            mPageNo++;
            mPageRender.setmPageNo(mPageNo);
            mDynamicFlipView.PostflippedToView(mPageNo);
            Log.i(TAG1, "TMP index ++ " + temp_index);
            Log.i(TAG1, "after flippedToView with mPageNo " + mPageNo + ", " + mPageRender.getPageNo());
        } else if (mPageFlip.getFlipState() == PageFlipState.BACKWARD_FLIP) {
            if (mPageFlip.is_back_restore) {
                if (mDynamicFlipView.onPageFlippedListener != null)
                    mDynamicFlipView.onPageFlippedListener.onRestorePage(mPageNo, false);
            }
            mPageNo--;
            mPageRender.setmPageNo(mPageNo);
            Log.i(TAG1, "TMP index -- " + temp_index);
            mDynamicFlipView.PostflippedToView(mPageNo);
            Log.i(TAG1, "after flippedToView back with mPageNo " + mPageNo + ", " + mPageRender.getPageNo());
        } else if (mPageFlip.getFlipState() == PageFlipState.RESTORE_FLIP) {
            if (mDynamicFlipView.onPageFlippedListener != null)
                mDynamicFlipView.onPageFlippedListener.onRestorePage(mPageNo, true);
        }
    }

    /**
     * Draw frame
     *
     * @param gl OpenGL handle
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        try {
            mDrawLock.lock();
            if (mPageRender != null) {
                mPageRender.onDrawFrame();
            }
        } finally {
            mDrawLock.unlock();
        }
    }


    /**
     * Handle surface is changed
     *
     * @param gl     OpenGL handle
     * @param width  new width of surface
     * @param height new height of surface
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        try {
            mPageFlip.onSurfaceChanged(width, height);
            // if there is the second page, create double page render when need
            int pageNo = mPageRender.getPageNo();
            if (mPageFlip.getSecondPage() != null && width > height) {
                if (!(mPageRender instanceof DoublePagesRender)) {
                    mPageRender.release();
                    mPageRender = new DoublePagesRender(getContext(),
                            mPageFlip,
                            mHandler,
                            pageNo);
                }
            }
            // if there is only one page, create single page render when need
            else if (!(mPageRender instanceof SinglePageRender)) {
                mPageRender.release();
                mPageRender = new SinglePageRender(getContext(),
                        mPageFlip,
                        mHandler,
                        pageNo, this);
            }

            // let page render handle surface change
            mPageRender.onSurfaceChanged(width, height);
        } catch (PageFlipException e) {
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceChanged");
        }
    }

    /**
     * Handle surface is created
     *
     * @param gl     OpenGL handle
     * @param config EGLConfig object
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        try {
            mPageFlip.onSurfaceCreated();
        } catch (PageFlipException e) {
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceCreated");
        }

    }

    /**
     * Create message handler to cope with messages from page render,
     * Page render will send message in GL thread, but we want to handle those
     * messages in main thread that why we need handler here
     */
    private void newHandler() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case PageRender.MSG_ENDED_DRAWING_FRAME:
                        try {
                            mDrawLock.lock();
                            // notify page render to handle ended drawing
                            // message
                            if (mPageRender != null &&
                                    mPageRender.onEndedDrawing(msg.arg1)) {
                                requestRender();
                            }
                        } finally {
                            mDrawLock.unlock();
                        }
                        break;

                    default:
                        break;
                }
            }
        };
    }
}
