package com.example.sample;

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
import android.widget.TextView;

import java.util.LinkedList;

public class DynamicAdapter extends BaseAdapter {
    //    Context context;
    private LinkedList<String> pages_data;
    FlipBookAdapterCallbacks flipBookAdapterCallbacks;
    private TextView page_no;
    boolean page_contains_a_picture = false;

    public DynamicAdapter(LinkedList<String> pages_data, FlipBookAdapterCallbacks listener) {
//        this.context = flipBooKView.getContext();
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
//        int type = getItemViewType(position);
//        if (type == 0) {
//            v = LayoutInflater.from(context).inflate(R.layout.book_cover, null);
//            ImageView imgView = v.findViewById(R.id.cover_img);
//            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
//            v.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    OnSingleTapConfirmed();
//                }
//            });
//
//        } else if (type == getCount()-1) {
//            v = LayoutInflater.from(context).inflate(R.layout.book_back_cover, null);
//            v.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    OnSingleTapConfirmed();
//                }
//            });
//        } else  {
//        }
//        if (v == null)
        if (getItemViewType(position)==0)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item1, null,false);
        if (getItemViewType(position)==1)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item2, null,false);
        else
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item1, null,false);
        //        final TextView tV = v.findViewById(R.id.tV);
        final EditText tV = v.findViewById(R.id.tV);
        final Button btn = v.findViewById(R.id.btn);
        tV.setText(getItem(position));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tV.setText("Submitted");
            }
        });
        return v;
    }

//
//    public void switch_ImageZoomFlipMode() {
//        if (adapterPageFlipView.is_to_flip()) {
//            adapterPageFlipView.setIs_to_flip(false);
//            adapterPageFlipView.CancelAutoRefresh();
//            String page_path = flipBooKView.getFlip_list().get(flipBooKView.current_page_index).getImage_path();
//            int dest_width = View_Utils.getScreenResolution(context).width * 2;
//            int dest_height = View_Utils.getScreenResolution(context).height * 2;
//            Bitmap page_bitmap = FileUtils.getDownSizedBitmapFromPath(page_path, dest_width, dest_height);
//            ((ImageView) flipBooKView.current_page.findViewById(R.id.z_image)).setImageBitmap(page_bitmap);
//            adapterPageFlipView.hideFlipView();
//            flipBookAdapterCallbacks.onPageZoomed(flipBooKView.current_page_index);
//        } else {
//            adapterPageFlipView.ShowFlipView();
//            new android.os.Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    adapterPageFlipView.setIs_to_flip(true);
//                }
//            }, 500);
//            flipBookAdapterCallbacks.onPageUnZoomed(flipBooKView.current_page_index);
//        }
//    }
//
//    public boolean OnSingleTapConfirmed() {
////        View_Utils.hideSystemUI(context);
//        flipBookAdapterCallbacks.onPageClicked(flipBooKView.current_page_index);
//        if (!flipBooKView.dont_hide) {
//            if (flipBooKView.is_nav_visible()) {
//                if (flipBooKView.getHandlerC() != null && flipBooKView.getRunnable() != null) {
//                    flipBooKView.getHandlerC().removeCallbacks(flipBooKView.getRunnable());
//                }
//                flipBooKView.HideNavigations();
//            } else {
//                flipBooKView.PostDisplayNavigations(5000);
//            }
//        }
//        if (adapterPageFlipView.is_to_flip()) {
//            flipBooKView.ManuallyRefresh_Page(50);
//        }
//        return false;
//    }

    public interface FlipBookAdapterCallbacks {
        void onPageClicked(int pos);
//        void onPageZoomed(int current_page_index);
//        void onPageUnZoomed(int current_page_index);
//        void onPageFullyLoaded(Bitmap resource, int current_page_index);
//        void onLastPageBtnClicked(View v);
    }
}

