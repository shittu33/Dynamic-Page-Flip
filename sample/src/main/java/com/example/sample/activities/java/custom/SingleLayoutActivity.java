package com.example.sample.activities.java.custom;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adaptablecurlpage.flipping.enums.FlipSpeed;
import com.example.adaptablecurlpage.flipping.enums.PageShadowType;
import com.example.adaptablecurlpage.flipping.enums.PageType;
import com.example.adaptablecurlpage.flipping.views.DynamicFlipView;
import com.example.sample.R;
import com.example.sample.adapter.SimpleAdapter;

import java.util.LinkedList;

public class SingleLayoutActivity extends AppCompatActivity
        implements DynamicFlipView.OnPageFlippedListener
        , DynamicFlipView.SystemUIVisibilityLister
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flip_activity);
        DynamicFlipView dynamicFlipView = findViewById(R.id.dynamic_flip_view);
        final LinkedList<String> pages_data = new LinkedList<>();
        for (int i = 1; i < 16; i++) {
            StringBuilder large_txt = new StringBuilder();
            for (int j = 0; j < 50; j++) {
                large_txt.append("This is Page ").append(i).append(" of rubbish text ").append(j);
            }
            pages_data.add(large_txt.toString());
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(pages_data, new SimpleAdapter.FlipBookAdapterCallbacks() {
            @Override
            public void onPageClicked(int pos) {
                Toast.makeText(getApplicationContext(), "page " + pos + " is clicked", Toast.LENGTH_SHORT).show();
            }
        });
        dynamicFlipView.setAdapter(simpleAdapter);
        dynamicFlipView.setSelection(0);
        dynamicFlipView.setSystemUIChangeListeners(this, this);
        dynamicFlipView.setFlipSpeed(FlipSpeed.NORMAL)
//                .setMaxBackAlpha(0.5f)
//                .setPageBackColor(Color.BLACK)
                .setPageBackColorToDominant()
                .setPageType(PageType.MAGAZINE_SHEET)
                .setPageShadowType(PageShadowType.NO_SHADOW)
                .setFlipLister(this);
    }

    private static final String TAG = "SingleLayoutActivityWit";
    @Override
    public void onPageFlipped(View page, int page_no, long id) {
        Log.e(TAG, "onPageFlipped: page no is " + page_no);
    }

    @Override
    public void onPageFlippedBackward(View page, int page_no, long id) {
        Log.e(TAG, "onPageFlippedBackward: page no is " + page_no);
    }

    @Override
    public void onPageFlippedForward(View page, int page_no, long id) {
        Log.e(TAG, "onPageFlippedForward: page no is " + page_no);
    }

    @Override
    public void onFingerDown(View v, int pos) {
        Log.e(TAG, "onFingerDown: on page " + pos);
    }

    @Override
    public void onFingerUp(View v, int pos) {
        Log.e(TAG, "onFingerUp: on page " + pos);
    }

    @Override
    public void onFingerDownToFlip(View page, int page_no) {
        Log.e(TAG, "onFingerDownToFlip: on page " + page_no);
    }

    @Override
    public void onFingerUpToFlip(View page, int page_no) {
        Log.e(TAG, "onFingerUpToFlip: on page " + page_no);
    }

    @Override
    public void onFastFlipStart(View page, int page_no, boolean is_forward) {
        Log.e(TAG, "onFastFlipStart: page no is " + page_no);
    }

    @Override
    public void onFastFlipEnd(View page, int page_no, boolean is_forward) {
        Log.e(TAG, "onFastFlipEnd: page no is " + page_no);
    }

    @Override
    public void onUiVisible() {
        Log.e(TAG, "onSystemUiVisible: ");
    }

    @Override
    public void onUiHidden() {
        Log.e(TAG, "onSystemUiHidden: ");
    }
}
