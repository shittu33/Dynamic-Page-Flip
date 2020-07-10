package com.example.adaptablecurlpage.flipping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

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

    Bitmap getBitmap(int which_page) {
        Bitmap bitmap = sparseArray.get(which_page);
        if (bitmap != null) {
            Log.e("active", "This bitmap is named " + bitmap.toString());
        } else {
            if (mDynamicFlipView != null) {
                bitmap = takeScreenshot(mDynamicFlipView.getSelectedView(), false, Color.WHITE);
                Log.e("active", "Bitmap is loaded directly for " + which_page);
            } else {
                Log.e("active", "Bitmap is not loaded for index " + which_page);
            }
        }
        return bitmap;
    }

    private DynamicFlipView mDynamicFlipView;

    void loadViewsForFlipping(DynamicFlipView dynamicView, FlipItem previousItem, FlipItem currentItem, FlipItem nextItem) {
        int previous_index = previousItem.index;
        View previousView = previousItem.view;
        int selected_index = currentItem.index;
        View selectedView = currentItem.view;
        int next_index = nextItem.index;
        View next_view = nextItem.view;
//    void loadViewsForFlipping(DynamicFlipView dynamicView, int previous_index, View previousView, int selected_index, View selectedView,
//                              int next_index, View next_view) {
        this.mDynamicFlipView = dynamicView;
        sparseArray = new SparseArray<>();
        if (previousView != null) {
            Bitmap previous_page = takeScreenshot(previousView, false, dynamicView.getBackgroundColor());
            sparseArray.put(previous_index, previous_page);
            Log.i(TAG, "previous index is" + previous_index);
            Log.e("active", "Previous Bitmap of index " + previous_index + "is added");
        }
        if (selectedView != null) {
            Bitmap selected_page = takeScreenshot(selectedView, false, dynamicView.getBackgroundColor());
            sparseArray.put(selected_index, selected_page);
            Log.i(TAG, "selected index is " + selected_index);
            Log.e("active", "Selected Bitmap of index " + selected_index + "is added");
        }
        if (next_view != null) {
            Bitmap next_page = takeScreenshot(next_view, false, dynamicView.getBackgroundColor());
            sparseArray.put(next_index, next_page);
            Log.i(TAG, "next index is " + next_index);
            Log.e("active", "Next Bitmap of index " + next_index + "is added");
        }
//        adapterPageFlipView.requestRender();
        Log.i(TAG, "bitmapLoadedd");

    }

    public static Bitmap takeScreenshot(View view, boolean is_landscape, int BackgroundColor) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width > 0 && height > 0) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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
