package com.example.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.example.adaptablecurlpage.flipping.DynamicFlipView;
import com.example.sample.utils.ViewUtils;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements DynamicFlipView.OnPageFlippedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ViewUtils.hideSystemUI(this);
        setContentView(R.layout.activity_main);
        final LinkedList<String> pages_data = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            pages_data.add("okay " + i);
        }
        DynamicAdapter dynamicAdapter = new DynamicAdapter(pages_data, new DynamicAdapter.FlipBookAdapterCallbacks() {
            @Override
            public void onPageClicked(int pos) {
                Toast.makeText(MainActivity.this, "page " + pos + " is clicked", Toast.LENGTH_SHORT).show();
            }
        });
        final DynamicFlipView dynamicFlipView = findViewById(R.id.dynamic_flip_view);
        dynamicFlipView.setAdapter(dynamicAdapter);
        dynamicFlipView.setOnPageFlippedListener(this);
    }

    @Override
    public void onPageFlipped(View page, int page_no, long id) {

    }

    @Override
    public void onPageFlippedBackward(View page, int page_no, long id) {

    }

    @Override
    public void onPageFlippedForward(View page, int page_no, long id) {

    }

    @Override
    public void onUserFinger_isDown(View page, int page_no) {
//        ViewUtils.showSystemUI(this);
    }

    @Override
    public void onUserFinger_isUp(View page, int page_no) {
//        Runnable hide_runnable = new Runnable() {
//            public void run() {
//                ViewUtils.hideSystemUI(MainActivity.this);
//            }
//        };
//        new Handler().postDelayed(hide_runnable, 1000);
    }

    @Override
    public void onFastFlippingStarted(View page, int page_no, boolean is_forward) {
    }

    @Override
    public int onChangeActiveIndex(int active_index) {
        return 0;
    }

    @Override
    public void onFastFlippingEnded(View page, int page_no, boolean is_forward) {

    }

    @Override
    public void onRefreshForwardBackwardFinished(int time_taken) {

    }


    @Override
    public void onImageLoadingStatus(View page, int page_no, boolean is_ViewFullyLoaded) {

    }

    @Override
    public void onAfterViewLoadedToFlip(int page_no) {

    }

    @Override
    public void onRestorePage(int page_no, boolean is_forward) {

    }

    @Override
    public void onResetFlipSpeed() {

    }

    @Override
    public void onHold(View v, int pos) {

    }

    @Override
    public void onRelease(View v, int pos) {

    }

    @Override
    public void onEndPageReached(View v, int pos, boolean flip_forward) {

    }
}
