package com.example.sample.adapter;

/**
 * Created by Abu Muhsin on 26/09/2018.
 */

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.sample.R;
import com.example.sample.utils.GlideApp;

import java.util.LinkedList;

public class SimpleAdapter extends BaseAdapter {
    private LinkedList<String> pages_data;
    private FlipBookAdapterCallbacks flipBookAdapterCallbacks;

    public SimpleAdapter(LinkedList<String> pages_data, FlipBookAdapterCallbacks listener) {
        this.pages_data = pages_data;
        this.flipBookAdapterCallbacks = listener;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return pages_data.size();
    }

    @Override
    public String getItem(int i) {
        return pages_data.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(final int position, final View view, ViewGroup viewGroup) {
        View v = view;
        if (v == null)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.scroll_text_item, null, false);
        final EditText tv = v.findViewById(R.id.tV);
        tv.setText(getItem(position));
        return v;
    }

    public interface FlipBookAdapterCallbacks {
        void onPageClicked(int pos);
    }
}

