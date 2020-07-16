package com.example.sample.activities.java.custom;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adaptablecurlpage.flipping.enums.FlipSpeed;
import com.example.adaptablecurlpage.flipping.enums.PageShadowType;
import com.example.adaptablecurlpage.flipping.enums.PageType;
import com.example.adaptablecurlpage.flipping.views.DynamicFlipView;
import com.example.sample.R;
import com.example.sample.adapter.DynamicAdapter;

import java.util.LinkedList;

public class MultiLayoutActivity extends AppCompatActivity
        implements DynamicFlipView.OnPageFlippedListener
        , DynamicFlipView.SystemUIVisibilityLister {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flip_activity);
        final LinkedList<Pair<Integer, String>> pages_data = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            if (i == 0)
                pages_data.add(new Pair<>(R.layout.item1, "Page " + i));
            else if (i == 1)
                pages_data.add(new Pair<>(R.layout.item2, "Page " + i));
            else
                pages_data.add(new Pair<>(R.layout.item_simple, ""));
        }
        DynamicAdapter dynamicAdapter = new DynamicAdapter(pages_data, new DynamicAdapter.FlipBookAdapterCallbacks() {
            @Override
            public void onPageClicked(int pos) {
                Toast.makeText(getApplicationContext(), "page " + pos + " is clicked", Toast.LENGTH_SHORT).show();
            }
        });
        DynamicFlipView dynamicFlipView = findViewById(R.id.dynamic_flip_view);
        dynamicFlipView.setAdapter(dynamicAdapter);
        dynamicFlipView.setSelection(1);
        dynamicFlipView.setSystemUIChangeListeners(this, this);
        dynamicFlipView.setFlipSpeed(FlipSpeed.NORMAL)
//                .setMaxBackAlpha(0.5f)
//                .setPageBackColor(Color.BLACK)
                .setPageBackColorToDominant()
                .setPageType(PageType.MAGAZINE_SHEET)
                .setPageShadowType(PageShadowType.NO_SHADOW)
                .setFlipLister(this);
    }

    private static final String TAG = "MultiLayoutActivityWith";
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
