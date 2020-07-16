package com.example.sample.activities.java.simple;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.adaptablecurlpage.flipping.enums.FlipSpeed;
import com.example.adaptablecurlpage.flipping.views.DynamicFlipView;
import com.example.sample.R;
import com.example.sample.utils.ViewUtils;

import java.util.LinkedList;

import kotlin.Pair;

public class MultiLayoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flip_activity);

        DynamicFlipView dynamicFlipView = findViewById(R.id.dynamic_flip_view);
        LinkedList<Pair<Integer, PageData>> list = new LinkedList<>();
        list.add(new Pair<>(R.layout.item1, new PageData(R.id.img, R.drawable.google_fun)));
        list.add(new Pair<>(R.layout.item_simple, new PageData(R.id.img, R.drawable.dance)));
        list.add(new Pair<>(R.layout.item2, new PageData(R.id.img, R.drawable.dance)));
        list.add(new Pair<>(R.layout.item1, new PageData(R.id.img, R.drawable.google_fun)));
        list.add(new Pair<>(R.layout.item2, new PageData(R.id.img, R.drawable.dance)));
        dynamicFlipView.loadMultiLayoutPages(list, new DynamicFlipView.HandleMultiViewCallback<PageData>() {
            @Override
            public void HandleView(View v, final int position, final PageData data, @LayoutRes int layout) {
                final ImageView img = v.findViewById(data.id);
                final Button btn = v.findViewById(R.id.btn);
                switch (layout) {
                    case R.layout.item1:
                    case R.layout.item2:
                        final EditText tV = v.findViewById(R.id.tV);
                        tV.setText("Image");
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String alertMsg = data.id + "of position" + position;
                                tV.setText(alertMsg);
                            }
                        });
                    case R.layout.item_simple:
                        ViewUtils.loadImageWithGlide(data.result, img);
                }
            }
        });
        dynamicFlipView.setFlipSpeed(FlipSpeed.NORMAL);
    }

    public static class PageData {
        public @IdRes
        int id;
        @DrawableRes
        Integer result;
        PageData(int id, Integer result) {
            this.id = id;
            this.result = result;
        }
    }
}
