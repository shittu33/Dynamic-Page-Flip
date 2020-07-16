package com.example.adaptablecurlpage.flipping.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.example.adaptablecurlpage.flipping.model.FlipItem;
import com.example.adaptablecurlpage.flipping.views.DynamicFlipView;

/**
 * Created by Abu Muhsin on 28/11/2018.
 */

public final class BitmapLoader {
    private final static String TAG = "LoadBitmapTask";
    private static BitmapLoader __object;
    private final boolean mIsLandscape;
    private Context context;
    private SparseArray<Bitmap> sparseArray = new SparseArray<>();


    public static BitmapLoader get(Context context) {
        if (__object == null) {
            __object = new BitmapLoader(context);
        }
        return __object;
    }

    private BitmapLoader(Context context) {
        mIsLandscape = false;
        this.context = context;
    }

    public Bitmap getBitmap(int which_page) {
        Bitmap bitmap = sparseArray.get(which_page);
        if (bitmap != null) {
            Log.e("active", " getBitmap-> This bitmap is named " + bitmap.toString());
        } else {
            if (mDynamicFlipView != null) {
                try {
                    final View view;
                    if (mDynamicFlipView.is_moving_fwd)
                        view = mDynamicFlipView.getNextView();
                    else
                        view = mDynamicFlipView.getPreviousView();
                    bitmap = getBitmapFromView(view, false, Color.WHITE);
                    Log.e("active", "Bitmap is loaded directly for current page " + which_page);
                } catch (Exception e) {
                    if (mDynamicFlipView.is_moving_fwd)
                        bitmap = sparseArray.get(which_page + 1);
                    else
                        bitmap = sparseArray.get(which_page - 1);
                    Log.e("active", "Bitmap is loaded directly for previous page " + which_page);
                    e.printStackTrace();
                }
            } else {
                Log.e("active", "Bitmap is not loaded for index " + which_page);
            }
        }
        return bitmap;
    }

    private DynamicFlipView mDynamicFlipView;

    public void loadViewsForFlipping(DynamicFlipView dynamicView, FlipItem previousItem, FlipItem currentItem, FlipItem nextItem) {
        int previous_index = previousItem.index;
        View previousView = previousItem.view;
        int selected_index = currentItem.index;
        View selectedView = currentItem.view;
        int next_index = nextItem.index;
        View next_view = nextItem.view;
        this.mDynamicFlipView = dynamicView;
        sparseArray = new SparseArray<>();
        if (previousView != null) {
            Bitmap previous_page = getBitmapFromView(previousView, false, dynamicView.getBackgroundColor());
            sparseArray.put(previous_index, previous_page);
            Log.i(TAG, "previous index is" + previous_index);
            Log.e("active", "Previous Bitmap of index " + previous_index + "is added");
        }
        if (selectedView != null) {
            Bitmap selected_page = getBitmapFromView(selectedView, false, dynamicView.getBackgroundColor());
            sparseArray.put(selected_index, selected_page);
            Log.i(TAG, "selected index is " + selected_index);
            Log.e("active", "Selected Bitmap of index " + selected_index + "is added");
        }
        if (next_view != null) {
            Bitmap next_page = getBitmapFromView(next_view, false, dynamicView.getBackgroundColor());
            sparseArray.put(next_index, next_page);
            Log.i(TAG, "next index is " + next_index);
            Log.e("active", "Next Bitmap of index " + next_index + "is added");
        }
        Log.i(TAG, "bitmapLoadedd");

    }

    public static Bitmap getBitmapFromView(View view, boolean is_landscape, int BackgroundColor) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width > 0 && height > 0) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            if (is_landscape) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap lb = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true);
                bitmap.recycle();
                return lb;
            }

            return bitmap;
        } else {
            return null;
        }
    }
}
