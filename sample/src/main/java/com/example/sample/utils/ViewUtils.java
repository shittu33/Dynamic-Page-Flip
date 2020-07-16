package com.example.sample.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.sample.R;

public class ViewUtils {

    public static void loadImageWithGlide(Object imgData, ImageView imgView){
        GlideApp.with(imgView.getContext())
                .load(imgData)
                .placeholder(R.drawable.ic_launcher_background)
                .thumbnail(0.5f)
                .into(imgView);

    }
    public static void makeStatusTransparent(Activity activity) {
        final Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR/*| View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR*/);
            } else {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    public static void setMarginTop(View v, int margin) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        marginLayoutParams.setMargins(0,margin,0,0);
        v.setLayoutParams(marginLayoutParams);
    }

    public static void setMarginBottom(View v, int margin) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        marginLayoutParams.setMargins(0,0,0,margin);
        v.setLayoutParams(marginLayoutParams);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    public static void showSystemUI(Activity c) {
        View decorView = c.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    public static void hideSystemUI(Activity c) {
//        if (Build.VERSION.SDK_INT < 16) {
//            c.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        }
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = c.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            decorView.setSystemUiVisibility(
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.

                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);

        }
    }

}
