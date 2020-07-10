package com.example.adaptablecurlpage.flipping;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class CustomFlipViewBaseAdapter extends BaseAdapter {
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public abstract boolean is_viewFullyLoaded();

    public abstract void set_viewContentLoaded(boolean is_content_loaded);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public View_status getView_with_Status(int position, View convertView, ViewGroup parent){
        return new View_status(getView(position,convertView,parent),is_viewFullyLoaded());
    }

    public  class View_status {
        View v;
        boolean is_loaded;
        View_status(View view,boolean is_loaded){
            v= view;
            this.is_loaded = is_loaded;
        }
    }
}
