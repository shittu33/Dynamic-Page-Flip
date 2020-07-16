package com.example.sample.activities.java.simple;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adaptablecurlpage.flipping.enums.FlipSpeed;
import com.example.adaptablecurlpage.flipping.enums.PageShadowType;
import com.example.adaptablecurlpage.flipping.enums.PageType;
import com.example.adaptablecurlpage.flipping.views.DynamicFlipView;
import com.example.sample.R;

import java.util.LinkedList;

public class SingleLayoutActivity extends AppCompatActivity {
    private static final String TAG = "SingleLayoutActivityJav";

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
        dynamicFlipView.setFlipSpeed(FlipSpeed.NORMAL)
                .setPageBackColorToDominant()
                .setPageType(PageType.MAGAZINE_SHEET)
                .setPageShadowType(PageShadowType.NO_SHADOW)
                .loadSingleLayoutPages(R.layout.scroll_text_item, pages_data
                        , new DynamicFlipView.HandleSingleViewCallback<String>() {
                            @Override
                            public void HandleView(View v, int position, String data) {
                                final EditText tV = v.findViewById(R.id.tV);
                                tV.setText(data);
                            }
                        });
    }

}
