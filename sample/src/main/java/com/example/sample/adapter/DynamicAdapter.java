package com.example.sample.adapter;

/**
 * Created by Abu Muhsin on 26/09/2018.
 */

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.sample.R;
import com.example.sample.utils.GlideApp;

import java.util.ArrayList;
import java.util.LinkedList;

public class DynamicAdapter extends BaseAdapter {
    private LinkedList<Pair<Integer, String>> pages_data;
    private FlipBookAdapterCallbacks flipBookAdapterCallbacks;
    private ArrayList<Integer> typeList = new ArrayList<>();

    public DynamicAdapter(LinkedList<Pair<Integer, String>> pages_data, FlipBookAdapterCallbacks listener) {
        this.pages_data = pages_data;
        this.flipBookAdapterCallbacks = listener;
    }

    @Override
    public int getViewTypeCount() {
        for (Pair<Integer, String> data : pages_data) {
            int type = data.first;
            if (typeList.isEmpty() || !typeList.contains(type)) {
                typeList.add(type);
            }
        }
        Log.e("MultiAdapter", "this view has " + typeList.size() + " types");
        return typeList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return pages_data.get(position).first;
    }

    @Override
    public int getCount() {
        return pages_data.size();
    }

    @Override
    public String getItem(int i) {
        return pages_data.get(i).second;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(final int position, final View view, ViewGroup viewGroup) {
        View v = view;
        int type = getItemViewType(position);
        if (v == null)
            if (type == R.layout.item1)
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item1, null, false);
            else if (type == R.layout.item2)
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item2, null, false);
            else
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_simple, null, false);
        final ImageView img = v.findViewById(R.id.img);
        switch (type) {
            case R.layout.item1:
            case R.layout.item2:
                final EditText tV = v.findViewById(R.id.tV);
                final Button btn = v.findViewById(R.id.btn);
                tV.setText(getItem(position));
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        flipBookAdapterCallbacks.onPageClicked(position);
                    }
                });
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tV.setText("Submitted");
                    }
                });
                break;
            case R.layout.item_simple:
                //Handle item_simple
        }
        GlideApp.with(v.getContext())
                .load(R.drawable.google_fun)
                .placeholder(R.drawable.ic_launcher_background)
                .thumbnail(0.5f)
                .into(img);
        return v;
    }

    public interface FlipBookAdapterCallbacks {
        void onPageClicked(int pos);
    }
}

