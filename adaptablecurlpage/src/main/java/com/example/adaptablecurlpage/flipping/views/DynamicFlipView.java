package com.example.adaptablecurlpage.flipping.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.RequiresApi;

import com.eschao.android.widget.pageflip.Page;
import com.eschao.android.widget.pageflip.PageFlip;
import com.example.adaptablecurlpage.R;
import com.example.adaptablecurlpage.flipping.adapter.MultiAdapter;
import com.example.adaptablecurlpage.flipping.adapter.SingleAdapter;
import com.example.adaptablecurlpage.flipping.utils.BitmapLoader;
import com.example.adaptablecurlpage.flipping.model.FlipItem;
import com.example.adaptablecurlpage.flipping.enums.FlipSpeed;
import com.example.adaptablecurlpage.flipping.render.PageRender;
import com.example.adaptablecurlpage.flipping.enums.PageShadowType;
import com.example.adaptablecurlpage.flipping.enums.PageType;
import com.example.adaptablecurlpage.flipping.utils.DrawingState;
import com.example.adaptablecurlpage.flipping.utils.View_Utils;

import java.util.LinkedList;
import java.util.List;

import kotlin.Pair;
import kotlin.jvm.functions.Function4;

import static android.graphics.Color.DKGRAY;

/**
 * Created by Abu Muhsin on 21/11/2018.
 */

public class DynamicFlipView extends AdapterView<Adapter> {
    private static final int MAX_RELEASED_VIEW_SIZE = 1;
    public static final String TAG = "AdapterPageFlipView";
    public static final float CLICK_FORWARD_SLOP = 1.087613F;
    public static final float CLICK_BACK_SLOP = 12.416F;
    public static final float TRANS_BACK_ALPHA = 0.2f;

    //For Views
    private Context context;
    private PageFlipView mPageFlipView;
    private AttributeSet attrs;
    private int contentWidth;
    private int contentHeight;

    //For Adapters
    private Adapter adapter;
    private DataSetObserver adapterDataObserver;
    private LinkedList<View> bufferedViews = new LinkedList<>();
    public LinkedList<View> releasedViews = new LinkedList<>();
    private int bufferIndex = -1;
    public int adapterIndex = -1;
    private int adapterDataCount = 0;
    private final int sideBufferSize = 1;
    private boolean is_to_flip = true;
    private float last_position;
    public OnPageFlippedListener onPageFlippedListener;
    private DrawingState mDrawingState;
    private Handler mHandler = new Handler();
    private Runnable showAnimRunnable = new Runnable() {
        @Override
        public void run() {
            mPageFlipView.showAnimation();
        }
    };
    //----------------------------Constructors----------------------------------------------------

    public DynamicFlipView(Context context) {
        super(context);
        this.context = context;
        includePageFlipView(context);
    }

    public DynamicFlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        includePageFlipView(context);
        this.context = context;
        HandleXmlAccessibility(context, attrs);
    }

    public DynamicFlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        HandleXmlAccessibility(context, attrs);
    }

    public void HandleXmlAccessibility(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DynamicFlipView);
            int backColor = a.getColor(R.styleable.DynamicFlipView_page_back_color
                    , DKGRAY);
            if (backColor != DKGRAY)
                setPageBackColor(backColor);
            int page_sheet = a.getInt(R.styleable.DynamicFlipView_shadow_type, 1);
            switch (page_sheet) {
                case 0:
                    setPageType(PageType.SOFT_SHEET);
                    break;
                case 1:
                    setPageType(PageType.MAGAZINE_SHEET);
                    break;
                case 2:
                    setPageType(PageType.HARD_SHEET);
                    break;
            }
            int shadow_type = a.getInt(R.styleable.DynamicFlipView_shadow_type, 1);
            switch (shadow_type) {
                case 0:
                    setPageShadowType(PageShadowType.NO_SHADOW);
                    break;
                case 1:
                    setPageShadowType(PageShadowType.NORMAL_SHADOW);
                    break;
                case 2:
                    setPageShadowType(PageShadowType.DEEP_SHADOW);
                    break;
            }
            int flip_speed = a.getInt(R.styleable.DynamicFlipView_flip_speed, 2);
            switch (flip_speed) {
                case 0:
                    setFlipSpeed(FlipSpeed.VERY_SLOW);
                    break;
                case 1:
                    setFlipSpeed(FlipSpeed.SLOW);
                    break;
                case 2:
                    setFlipSpeed(FlipSpeed.NORMAL);
                    break;
                case 3:
                    setFlipSpeed(FlipSpeed.FAST);
                    break;
                case 4:
                    setFlipSpeed(FlipSpeed.VERY_FAST);
                    break;
            }
            float back_alpha = a.getFloat(R.styleable.DynamicFlipView_page_back_alpha, TRANS_BACK_ALPHA);
            boolean opaque_back_page = a.getBoolean(R.styleable.DynamicFlipView_opaque_page_back, false);
            if (opaque_back_page)
                setMaxBackAlpha(1);
            else
                setMaxBackAlpha(back_alpha);
            a.recycle();
        } else {
            setPageShadowType(PageShadowType.NORMAL_SHADOW);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        setAdapter(adapter, 0);
    }

    public void setAdapter(Adapter adapter, int initialPosition) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(adapterDataObserver);
        }
        this.adapter = adapter;
        adapterDataCount = adapter.getCount();
        adapterDataObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                onDataChanged();
            }

            @Override
            public void onInvalidated() {
                onDataChanged();
            }
        };
        this.adapter.registerDataSetObserver(adapterDataObserver);
        if (adapterDataCount > 0) {
            setSelection(initialPosition);
        } else {
            Log.i(TAG, "No data was supplied!!!");
        }
    }

    public <T> void loadSingleLayoutPages(@LayoutRes final int layoutResources, LinkedList<T> data, final HandleSingleViewCallback<T> listener) {
        final SingleAdapter<T> adapter = new SingleAdapter<>(data, new Function4<SingleAdapter<T>, Integer, View, ViewGroup, View>() {
            @Override
            public View invoke(SingleAdapter<T> adapter, Integer position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null)
                    v = LayoutInflater.from(parent.getContext()).inflate(layoutResources, parent, false);
                listener.HandleView(v, position, adapter.getItem(position));
                return v;
            }
        });
        setAdapter(adapter);
    }

    public <T> void loadMultiLayoutPages(List<Pair<Integer, T>> data, final HandleMultiViewCallback<T> listener) {
        final MultiAdapter<T> adapter =
                new MultiAdapter<T>(data, new Function4<MultiAdapter<T>, Integer, View, ViewGroup, View>() {
                    @Override
                    public View invoke(MultiAdapter<T> thisAdapter, Integer position, View convertView, ViewGroup parent) {
                        View v = convertView;
                        final int layout = thisAdapter.getItemViewType(position);
                        if (v == null)
                            v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                        listener.HandleView(v, position, thisAdapter.getItem(position), layout);
                        return v;
                    }
                });
        setAdapter(adapter);
    }

    public interface HandleSingleViewCallback<T> {
        void HandleView(View v, @LayoutRes int position, T data);
    }

    public interface HandleMultiViewCallback<T> {
        void HandleView(View v, int position, T data, int layout);
    }

    public boolean is_selected_view = false;

    public void setAdapterDataCount(int adapterDataCount) {
        this.adapterDataCount = adapterDataCount;
    }

    @Override
    public void setSelection(int position) {
        if (adapter == null) {
            return;
        }
        Log.i(TAG, "start selection at" + position);
        adapterDataCount = adapter.getCount();
        Log.i(TAG, "adapter count is " + adapterDataCount);
        PageRender.setMaxPages(adapterDataCount);
        Log.i(TAG, "Render has set the max pages");
        releaseViews();
        //add the requested view
        Log.i(TAG, "Views were released");
        //Add it next and the previous
        View selectedView = null;
        for (int i = 1; i <= sideBufferSize; i++) {
            int previous = position - i;
            int next = position + i;
            if (previous >= 0) {
                bufferedViews.addFirst(viewFromAdapter(previous, false));
                Log.i(TAG, "previous added");
            }
            selectedView = viewFromAdapter(position, true);
            Log.i(TAG, "Position of the selected view is " + position);
            bufferedViews.add(selectedView);
            Log.i(TAG, "selected view added");
//            onPageFlippedListener.onPageFlipped(selectedView, position, selectedView.getId());
            Log.i(TAG, "after FlipListener");
            is_selected_view = true;
            if (next < adapterDataCount) {
                bufferedViews.addLast(viewFromAdapter(next, true));
                Log.i(TAG, "next added");
            }
        }
        Log.i(TAG, "buffer loaded");
        //set positions to the requested position
        bufferIndex = bufferedViews.indexOf(selectedView);
        adapterIndex = position;
        mPageFlipView.resetPageNo(position);
        Log.i(TAG, "page reset");
        listenForPageFlip();
        updateVisibleView(bufferIndex);
    }

    @Override
    public Adapter getAdapter() {
        return adapter;
    }

    @Override
    public int getSelectedItemPosition() {
        return adapterIndex;
    }

    @Override
    public View getSelectedView() {
        final int bufferSize = bufferedViews.size();
        return (bufferSize > 0 && bufferIndex < bufferSize && bufferIndex >= 0) ? bufferedViews.get(bufferIndex)
                : null;
    }

    public View getNextView() {
        final int bufferSize = bufferedViews.size();
        final int nextIndex = bufferIndex + 1;
        return (nextIndex < bufferSize) ? bufferedViews.get(nextIndex)
                : null;
    }

    public View getPreviousView() {
        final int bufferSize = bufferedViews.size();
        final int prevIndex = bufferIndex - 1;
        return (prevIndex < bufferSize && prevIndex >= 0) ? bufferedViews.get(prevIndex)
                : null;
    }

    public int getAdapterDataCount() {
        return adapterDataCount;
    }


    public void ResetFlipSpeed() {
        getmPageFlipView().setFlipDuration(1500);
        setInfiniteFlipForwardDuration(900);
        setInfiniteFlipForwardinterval(280);
        setInfiniteFlipBackwardDuration(350);
        setInfiniteFlipBackwardinterval(300);
        setFlipBackwardDuration(1000);
    }

    public DynamicFlipView setFlipSpeed(FlipSpeed flipSpeed) {
        switch (flipSpeed) {
            case VERY_SLOW:
                this.getmPageFlipView().setFlipDuration(4000);
                this.setInfiniteFlipForwardDuration(1300);
                this.setInfiniteFlipForwardinterval(900);
                this.setInfiniteFlipBackwardDuration(900);
                this.setInfiniteFlipBackwardinterval(820);
                this.setFlipBackwardDuration(2500);
                break;
            case SLOW:
                this.getmPageFlipView().setFlipDuration(2000);
                this.setInfiniteFlipForwardDuration(900);
                this.setInfiniteFlipForwardinterval(300);
                this.setInfiniteFlipBackwardDuration(400);
                this.setInfiniteFlipBackwardinterval(320);
                this.setFlipBackwardDuration(1200);
                break;
            case NORMAL:
                getmPageFlipView().setFlipDuration(1500);
                setInfiniteFlipForwardDuration(800);
                setInfiniteFlipForwardinterval(280);
                setInfiniteFlipBackwardDuration(200);
                setInfiniteFlipBackwardinterval(300);
                setFlipBackwardDuration(1000);
                break;
            case FAST:
                this.getmPageFlipView().setFlipDuration(800);
                this.setInfiniteFlipForwardDuration(700);
                this.setInfiniteFlipForwardinterval(200);
                this.setInfiniteFlipBackwardDuration(140);
                this.setInfiniteFlipBackwardinterval(200);
                this.setFlipBackwardDuration(300);
                break;
            case VERY_FAST:

                this.getmPageFlipView().setFlipDuration(400);
                this.setInfiniteFlipForwardDuration(400);
                this.setInfiniteFlipForwardinterval(200);
                this.setInfiniteFlipBackwardDuration(140);
                this.setInfiniteFlipBackwardinterval(180);
                this.setFlipBackwardDuration(230);
                break;
        }
        return this;
    }

    public DynamicFlipView setPageShadowType(PageShadowType pageShadowType) {
        switch (pageShadowType) {
            case NO_SHADOW:
                mPageFlipView.mPageFlip.setShadowWidthOfFoldEdges(0, 10, 0.1f)
                        .setShadowWidthOfFoldBase(0, 10, 0.1f);
//                        .setShadowColorOfFoldEdges(0.1f, 0.4f, 0.3f, 0.0f);
//                .setShadowColorOfFoldBase(0.1f,0.3f,0.1f,0.1f);
                break;
            case NORMAL_SHADOW:
                mPageFlipView.mPageFlip.setShadowWidthOfFoldEdges(5, 50, 0.21f)
                        .setShadowWidthOfFoldBase(5, 70, 0.22f)
                        .setShadowColorOfFoldEdges(0.1f, 0.4f, 0.3f, 0.0f);
//                .setShadowColorOfFoldBase(0.1f,0.3f,0.1f,0.1f);
                break;
            case DEEP_SHADOW:
                mPageFlipView.mPageFlip.setShadowWidthOfFoldEdges(5, 60, 0.23f)
                        .setShadowWidthOfFoldBase(5, 80, 0.4f)
                        .setShadowColorOfFoldEdges(0.1f, 0.8f, 0.3f, 0.0f);
//                .setShadowColorOfFoldBase(0.1f,0.3f,0.1f,0.1f);
                break;

        }
        return this;
    }

    public DynamicFlipView setPageType(PageType pageType) {
        switch (pageType) {
            case SOFT_SHEET:
                mPageFlipView.mPageFlip.setSemiPerimeterRatio(0.3f);
                break;
            case MAGAZINE_SHEET:
                mPageFlipView.mPageFlip.setSemiPerimeterRatio(0.65f);
                break;
            case HARD_SHEET:
                mPageFlipView.mPageFlip.setSemiPerimeterRatio(0.99f);
        }
        return this;
    }
    //----------------------------View methods---------------------------------------------------

    //    public LinkedList<View> getBufferedViews() {
//        return bufferedViews;
//    }
    public static final CharSequence IMAGE_LOADED = "loaded";
    public static final CharSequence YET_LOADED = "yet_loaded";

    /**
     * This method return the view received from Adapter getCurrentView() method
     */
    public View viewFromAdapter(int position, boolean addToTop) {
        final boolean isMultiType = adapter.getViewTypeCount() > 1;
        View releasedView = releasedViews.isEmpty() ? null : releasedViews.removeFirst();
        if (isMultiType) {
            Object releaseTag = null;
            if (releasedView != null) {
                releaseTag = releasedView.getTag();
            }
            if (releaseTag != null && !releaseTag.equals(adapter.getItemViewType(position))) {
                releasedView = null;
            }
        }
        Log.i(TAG, "viewFromAdapter: After releasedView");
        View view = adapter.getView(position, releasedView, this);
        if (isMultiType) {
            final int viewType = adapter.getItemViewType(position);
            view.setTag(viewType);
        }
        Log.i(TAG, "viewFromAdapter: got View");
        if (releasedView != null && view != releasedView) {
            addReleasedView(releasedView);
        }
        Log.i(TAG, "viewFromAdapter: b4 settingView");
        setupAdapterView(view, addToTop, view == releasedView);
        Log.i(TAG, "viewFromAdapter: After setting Up layout");


        return view;
    }

    /**
     * This method set the collected views to Our AdapterView
     */
    private void setupAdapterView(View view, boolean addToTop, boolean isReusedView) {
        LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params =
                    new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT, 0);
        }

        if (isReusedView) {
            attachViewToParent(view, addToTop ? 0 : 1, params); //Add the Adapter View to the Parent.
        } else {
            addViewInLayout(view, addToTop ? 0 : 1, params, true);//Add it directly to the layout.
        }
    }

    public DynamicFlipView setMaxBackAlpha(float alpha) {
//        Page.UseDominantColorForFoldBack(true);
        mPageFlipView.setMaxBackAlpha(alpha);
        return this;
    }

    private void useDominantColorForFoldBack(boolean use) {
        Page.UseDominantColorForFoldBack(use);
    }

    public DynamicFlipView setPageBackColor(@ColorInt int color) {
        useDominantColorForFoldBack(false);
        Page.setFoldPageBackColor(color);
        return this;
    }

    public DynamicFlipView setPageBackColorToDominant() {
        useDominantColorForFoldBack(true);
        mPageFlipView.setMaxBackAlpha(1);
        return this;
    }


    private int backgroundColor = Color.WHITE;

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for (View child : bufferedViews) {
            child.measure(widthMeasureSpec, heightMeasureSpec);
        }

        mPageFlipView.measure(widthMeasureSpec, heightMeasureSpec);
    }


    //----------------------------Adapter Utils---------------------------------------------------

    private void onDataChanged() {
        adapterDataCount = adapter.getCount();
        int activeIndex;
        if (adapterIndex < 0) {
            activeIndex = 0;
        } else {
            activeIndex = Math.min(adapterIndex, adapterDataCount - 1);
        }
        releaseViews();
        setSelection(activeIndex);
    }

    private void releaseViews() {
        for (View view : bufferedViews) {
            releaseView(view);
        }
        bufferedViews.clear();
//        typed_bufferedViews[viewType].clear();
        bufferIndex = -1;
        adapterIndex = -1;
    }

    private void releaseView(View view) {
//        Assert.assertNotNull(view);
        detachViewFromParent(view);
        addReleasedView(view);
    }

    private void addReleasedView(View view) {
        if (releasedViews.size() < MAX_RELEASED_VIEW_SIZE) {
            releasedViews.add(view);
//            typed_releasedViews[viewType].add(view);
        }
    }

    public View getCurrentView() {
        return adapter.getView(getSelectedItemPosition(), null, this);
    }

    public void clearAdapter() {
        releaseViews();
        bufferedViews.clear();
        adapterIndex = 0;
        mPageFlipView = null;
    }


//----------------------------PageFlipView Utils(Our Surface View)----------------------------

    public DynamicFlipView setFlipLister(OnPageFlippedListener onPageFlippedListener) {
        this.onPageFlippedListener = onPageFlippedListener;
        return this;
    }

    private void includePageFlipView(Context context) {
        if (attrs != null) {
            mPageFlipView = new PageFlipView(context, this, attrs);
        } else {
            mPageFlipView = new PageFlipView(context, this);
        }
        setPageType(PageType.MAGAZINE_SHEET);
        mPageFlipView.setAlpha(0);
        Log.i(TAG, "before addViewInLayout");
        addViewInLayout(mPageFlipView, -1, new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT), false);
        Log.i(TAG, "After addViewInLayout");
    }

    private void updateVisibleView(int index) {
        for (int i = 0; i < bufferedViews.size(); i++) {
//            final View view = bufferedViews.get(i);
//            view.setAlpha(index == i ? 1 : 0);
            bufferedViews.get(i).setVisibility(index == i ? VISIBLE : GONE);
        }
    }

    public void postFlippedToView(final int indexInAdapter) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    flippedToView(indexInAdapter);
                } catch (Exception e) {
                    Log.i(TAG, "smtn went wrong");
                    e.printStackTrace();
                }
            }
        });
    }

    int last_index = -1;
    public boolean is_moving_fwd = true;

    public void listenForPageFlip() {
        if (onPageFlippedListener != null)
            onPageFlippedListener.onPageFlipped(
                    bufferedViews.get(bufferIndex)
                    , adapterIndex
                    , bufferedViews.get(bufferIndex).getId());
        if (last_index == -1) {
            last_index = adapterIndex;
        } else {
            if (last_index < adapterIndex) {
                //Forward
                if (onPageFlippedListener != null) {
                    is_moving_fwd = true;
                    onPageFlippedListener.onPageFlippedForward(
                            bufferedViews.get(bufferIndex)
                            , adapterIndex
                            , bufferedViews.get(bufferIndex).getId());
                }
            } else if (last_index > adapterIndex) {
                if (onPageFlippedListener != null) {
                    is_moving_fwd = false;
                    onPageFlippedListener.onPageFlippedBackward(
                            bufferedViews.get(bufferIndex)
                            , adapterIndex
                            , bufferedViews.get(bufferIndex).getId());
                }
                //Backward
            }
            last_index = adapterIndex;
        }
    }

    public void flippedToView(int indexInAdapter) {
        Log.i(TAG, String.format("flippedToView: %d, isPost %s", indexInAdapter, null));
        if (indexInAdapter >= 0 && indexInAdapter < adapterDataCount) { //let say count is 5
            if (indexInAdapter == adapterIndex + 1) { //forward one page e.g from 3 to 4
                if (adapterIndex < adapterDataCount - 1) {// 3<4, proceed...
                    adapterIndex++; //e.g 3 -> 4
                    //get the old view
                    View old = bufferedViews.get(bufferIndex);//e.g previous buffer index 1 has view 3
                    if (bufferIndex + 1 > sideBufferSize) { //e.g 1+1 > 1(greater), continue...
                        //remove first view from buffer and from the layout, which is the old View
                        // then add it to list of released views
                        releaseView(bufferedViews.removeFirst());//e.g release view at first of buffer
                    }
                    if (adapterIndex + sideBufferSize < adapterDataCount) {//e.g 5  == 5 (not less than 5),skip...
                        //Add the next view to the end of buffer,
                        //here by reloading the adapter View with the next view.
//                        is_selected_view = true;
                        bufferedViews.addLast(viewFromAdapter(adapterIndex + sideBufferSize, true)); //e.g add view of index 4 at last of buffer
                    }
                    //index of buffer changed to the recent index of the old view +1
                    bufferIndex = bufferedViews.indexOf(old) + 1; //e.g 0+1, buffer index of old view shifted from 0 to 1
                    //query onLayout()
                    requestLayout();
                    //make the next view visible, and hide others
                    updateVisibleView(bufferIndex);
                }
            } else if (indexInAdapter == adapterIndex - 1) {//Backward e.g from 2 to 1 ....Size 5
                if (adapterIndex > 0) {//if old adapter index is greater than 0
                    adapterIndex--; //e.g from 2 to 1

                    View old = bufferedViews.get(bufferIndex); // e.g previous buffer index 1 has view 2
                    if (bufferIndex - 1 + sideBufferSize < bufferedViews.size() - 1) { // e.g 1<2, continue...
                        //Remove the old next, and add it to released list.
                        releaseView(bufferedViews.removeLast()); //remove the last view in buffer
                    }
                    if (adapterIndex - sideBufferSize >= 0) {
                        //e.g add the view 0 at the top of buffer
//                        is_selected_view = true;
                        bufferedViews.addFirst(viewFromAdapter(adapterIndex - sideBufferSize, false));
                    }
                    //index of buffer changed to the recent index of the old view +1
                    bufferIndex = bufferedViews.indexOf(old) - 1; //e.g the current index-1 of view 4 is 1
                    //query onLayout
                    requestLayout();
                    //make the previous view visible, and hide others
                    updateVisibleView(bufferIndex);
                } else {
                    Log.i(TAG, "index is less than 0");
                }
//                }
            } else {
                Log.i(TAG, "Should not happen," + adapterIndex + "," + indexInAdapter + "," + adapterIndex);
            }
            listenForPageFlip();
        } else {
//            Assert.fail("Invalid indexInAdapter: " + indexInAdapter);
        }
    }

    public PageFlipView getmPageFlipView() {
        return mPageFlipView;
    }

    public void SupplyViewsToBitmapLoader() {
        if (bufferedViews.size() >= 1) {
//            View selectedView = bufferedViews.get(bufferIndex);
//            View previousView = null;
//            if (bufferIndex - 1 >= 0)
//                previousView = bufferedViews.get(bufferIndex - 1);
//            View nextView = null;
//            if (bufferIndex < bufferedViews.size() - 1)
//                nextView = bufferedViews.get(bufferIndex + 1);
            final FlipItem previous = new FlipItem(adapterIndex - 1, getPreviousView());
            final FlipItem current = new FlipItem(adapterIndex, getSelectedView());
            final FlipItem next = new FlipItem(adapterIndex + 1, getNextView());
            BitmapLoader.get(context).loadViewsForFlipping(this, previous, current, next);
        }
    }


    private void reloadAnimationPage() {
        try {
            SupplyViewsToBitmapLoader();
            Log.e(TAG, "reloadAnimationPage: supplied");
        } catch (Exception e) {
            Log.e(TAG, "reloadAnimationPage: Unable to supply");
            e.printStackTrace();
        }
        RefreshFlip();
    }

    public void setSystemUIChangeListeners(Activity activity, final SystemUIVisibilityLister visibilityListener) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // TODO: The system bars are visible. Make any desired
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            visibilityListener.onUiVisible();
                        } else {
                            // TODO: The system bars are NOT visible. Make any desired
                            reloadAnimationPage();
                            visibilityListener.onUiHidden();
//                            layoutParams.bottomMargin = 0;
                        }
                    }
                });
    }

    public interface SystemUIVisibilityLister {
        void onUiVisible();

        void onUiHidden();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (View child : bufferedViews) {
            child.layout(0, 0, right - left, bottom - top);
        }
        if (changed || contentWidth == 0) {
            int w = right - left;
            int h = bottom - top;
            mPageFlipView.layout(0, 0, w, h);
            if (contentWidth != w || contentHeight != h) {
                contentWidth = w;
                contentHeight = h;
            }
        }
//        reloadAnimationPage();
//        Log.e(TAG, "onLayout: supplied");
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
//        reloadAnimationPage();
        Log.e(TAG, "dispatchDraw:");
    }

    Runnable mLChildDownRun = new Runnable() {
        @Override
        public void run() {
            if (onPageFlippedListener != null) {
                onPageFlippedListener.onFingerDown(getSelectedView(), adapterIndex);
            }
        }
    };
    Runnable mLChildUpRun = new Runnable() {
        @Override
        public void run() {
            if (onPageFlippedListener != null) {
                onPageFlippedListener.onFingerUp(getSelectedView(), adapterIndex);
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_UP) {
            mHandler.postDelayed(mLChildUpRun, 10);
            reloadAnimationPage();
        } else if (action == MotionEvent.ACTION_DOWN) {
            reloadAnimationPage();
        }
        mHandler.postDelayed(mLChildDownRun, 10);

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        if (is_to_flip()) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    last_position = ev.getX();
                    return !isClickToFlip(ev);
                case MotionEvent.ACTION_UP:
                    return !isClickToFlip(ev);
                case MotionEvent.ACTION_MOVE:
                    float distance_move = last_position - ev.getX();
                    if (can_start_flipping(distance_move)) {
                        return true;
                    }
                    last_position = ev.getX();
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (is_to_flip()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mHandler.removeCallbacks(mLChildDownRun);
                    if (onPageFlippedListener != null) {
                        onPageFlippedListener.onFingerDownToFlip(getSelectedView(), adapterIndex);
                    }
                    //make flip visible
                    mHandler.removeCallbacks(showAnimRunnable);
                    mHandler.postDelayed(showAnimRunnable, 40);
                    mPageFlipView.onFingerDown(event.getX(), event.getY());
                    infinite_flip_run_down = new Runnable() {
                        @Override
                        public void run() {
                            if (is_click_toFlipForward(event)) {
                                is_infiniteFlipForward = true;
                                if (onPageFlippedListener != null)
                                    onPageFlippedListener.onFastFlipStart(getSelectedView(), adapterIndex, true);
                                flipInfiniteLongtouch(event.getX(), event.getY(), infiniteFlipForwardDuration, true);
                            } else if (is_click_toFlipBack(event)) {
                                is_infiniteFlipBackward = true;
                                if (onPageFlippedListener != null)
                                    onPageFlippedListener.onFastFlipStart(getSelectedView(), adapterIndex, false);
                                flipInfiniteLongtouch(event.getX(), event.getY(), infiniteFlipBackwardDuration, false);
                            }
                        }
                    };
                    infinite_flip_thread_down.postDelayed(infinite_flip_run_down, ViewConfiguration.getLongPressTimeout());
                    return true;
                case MotionEvent.ACTION_UP:
                    mHandler.removeCallbacks(mLChildDownRun);
                    mHandler.removeCallbacks(mLChildUpRun);

                    if (is_click_toFlipBack(event)) {
                        mPageFlipView.setFlipDuration(FlipBackwardDuration);
                    }
                    mPageFlipView.onFingerUp(event.getX(), event.getY());
                    infinite_flip_thread_down.removeCallbacks(infinite_flip_run_down);
                    infinite_flip_thread.removeCallbacks(infinite_flip_run);
                    if (is_infiniteFlipForward || is_infiniteFlipBackward) {
                        if (onPageFlippedListener != null) {
                            onPageFlippedListener.onFastFlipEnd(getSelectedView(), adapterIndex
                                    , is_infiniteFlipForward);
                        }
                    }
                    if (!is_infiniteFlipBackward || !is_infiniteFlipForward) {
                        if (onPageFlippedListener != null)
                            onPageFlippedListener.onFingerUpToFlip(getSelectedView(), adapterIndex);
                    }
                    is_infiniteFlipForward = false;
                    is_infiniteFlipBackward = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    mHandler.removeCallbacks(mLChildDownRun);
                    mHandler.removeCallbacks(mLChildUpRun);
                    mPageFlipView.onFingerMove(event.getX(), event.getY());
//                    is_click_trigger = false;
                    return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(final WindowInsets insets) {
        if (mFit) {
            setPadding(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom()
            );
            return insets.consumeSystemWindowInsets();
        } else {
            setPadding(0, 0, 0, 0);
            return insets;
        }
    }

    private boolean mFit = false;

    public boolean isFit() {
        return mFit;
    }

    public void setFit(final boolean fit) {
        if (mFit == fit) {
            return;
        }

        mFit = fit;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            requestApplyInsets();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                //noinspection deprecation
                requestFitSystemWindows();
            }
        }
    }

    @Override
    protected boolean fitSystemWindows(final Rect insets) {
        if (mFit) {
            setPadding(insets.left, insets.top, insets.right, insets.bottom);
            // Do not propagate the system insets further.
            return true;
        } else {
            setPadding(0, 0, 0, 0);
            // Do not consume the insets and allow other views handle them.
            return false;
        }
    }

    public void flip_forward_to(int page_no) {
        flip_to_where(getX_position_to_flipForward(), getY_position_to_flipForward(), page_no);
    }

    public void flip_backward_to(int page_no) {
        flip_to_where(getX_position_to_flipBackward(), getY_position_to_flipBackward(), page_no);
    }

    public void flip_to_where(final float x, final float y, final int page_no) {

        PageFlip mPageFlip = getmPageFlip();

        if (!mPageFlip.isAnimating() && mPageFlip.getFirstPage() != null) {
            mPageFlip.onFingerDown(x, y);
        }

        if (!mPageFlip.isAnimating()) {
            mPageFlip.onFingerUp(x, y, mPageFlipView.mDuration);
            try {
                mPageFlipView.mDrawLock.lock();
                if (mPageFlipView.mPageRender != null && mPageFlipView.mPageRender.onFingerUp(x, y)) {
//                    mPageFlipView.UpdateFromAdapter();
                    mPageFlipView.requestRender();
                    if (adapterIndex != page_no - 1) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                flip_to_where(x, y, page_no);
                            }
                        }, 200);
                    }
                }
            } finally {
                mPageFlipView.mDrawLock.unlock();
            }
        }
    }

    public void flipInfiniteLongtouch(float x, float y, int duration,
                                      boolean flip_forward) {
        flip_infinitely(x, y, duration, flip_forward);
    }

    public void flip_Infinitely_Forward(int flip_duration) {
        flip_infinitely(getX_position_to_flipForward(), getY_position_to_flipForward(), flip_duration, true);
    }

    public void flip_Infinitely_Backward(int flip_duration) {
        flip_infinitely(getX_position_to_flipBackward(), getY_position_to_flipBackward(), flip_duration, false);
    }


    Handler infinite_flip_thread = new Handler();
    Handler infinite_flip_thread_down = new Handler();
    Runnable infinite_flip_run_down;
    Runnable infinite_flip_run;
    boolean is_infiniteFlipForward = false;
    boolean is_infiniteFlipBackward = false;

    int FlipBackwardDuration = 800;
    int infiniteFlipForwardDuration = 800;
    int infiniteFlipBackwardDuration = 350;
    int infiniteFlipBackwardinterval = 250;
    int infiniteFlipForwardinterval = 300;

    public int getFlipBackwardDuration() {
        return FlipBackwardDuration;
    }

    public int getInfiniteFlipForwardDuration() {
        return infiniteFlipForwardDuration;
    }

    public int getInfiniteFlipForwardinterval() {
        return infiniteFlipForwardinterval;
    }

    public int getInfiniteFlipBackwardinterval() {
        return infiniteFlipBackwardinterval;
    }

    public void setFlipBackwardDuration(int flipBackwardDuration) {
        FlipBackwardDuration = flipBackwardDuration;
    }

    public void setInfiniteFlipBackwardinterval(int infiniteFlipBackwardinterval) {
        this.infiniteFlipBackwardinterval = infiniteFlipBackwardinterval;
    }

    public void setInfiniteFlipForwardinterval(int infiniteFlipForwardinterval) {
        this.infiniteFlipForwardinterval = infiniteFlipForwardinterval;
    }

    public void setInfiniteFlipBackwardDuration(int infiniteFlipBackwardDuration) {
        this.infiniteFlipBackwardDuration = infiniteFlipBackwardDuration;
    }

    public void setInfiniteFlipForwardDuration(int infiniteFlipForwardDuration) {
        this.infiniteFlipForwardDuration = infiniteFlipForwardDuration;
    }

    public void flip_infinitely(final float x, final float y, final int flip_duration,
                                final boolean flip_forward) {
        if (adapterDataCount > 1 && adapterIndex == adapterDataCount - 2) {
            Log.i(TAG, "this is the last page page " + adapterIndex + "of count " + adapterDataCount);
        }
        mPageFlipView.showAnimation();
        PageFlip mPageFlip = getmPageFlip();
        PageFlipView mPageFlipView = getmPageFlipView();
        if (!mPageFlip.isAnimating() && mPageFlip.getFirstPage() != null) {
            mPageFlip.onFingerDown(x, y);
        }

        if (!mPageFlip.isAnimating()) {
            mPageFlip.onFingerUp(x, y, flip_duration);
            try {
                mPageFlipView.mDrawLock.lock();
                if (mPageFlipView.mPageRender != null && mPageFlipView.mPageRender.onFingerUp(x, y)) {
//                    mPageFlipView.UpdateFromAdapter();
                    mPageFlipView.requestRender();
                    infinite_flip_run = new Runnable() {
                        @Override
                        public void run() {
                            flip_infinitely(x, y, flip_duration, flip_forward);
                            Log.i(TAG, "is_flipping_infinitely");
                        }
                    };
                    //400-200(very fast forward),150-120(very fast backward)
                    //700-220(fast forward),250-200(fast backward)
                    //800-250(normal forward),350-300(normal backward)
                    //1000-300(slow forward),400-320(slow backward)
                    int interval = flip_forward ? infiniteFlipForwardinterval : infiniteFlipBackwardinterval;
                    infinite_flip_thread.postDelayed(infinite_flip_run, interval);
                }
            } finally {
                mPageFlipView.mDrawLock.unlock();
            }
        }
    }

    public void StopInfiniteFlip() {
        if (infinite_flip_run != null && infinite_flip_thread != null) {
            infinite_flip_thread.removeCallbacks(infinite_flip_run);
            Log.i(TAG, "StopInfiniteFlip");
        }
    }

    public void flip_toNextPage(int sleep_duration) {
//        mPageFlipView.onUserFinger_isDown(getX_position_to_flipForward(), getY_position_to_flipForward());
        PageFlip mPageFlip = getmPageFlip();
        final PageFlipView mPageFlipView = getmPageFlipView();
        if (!mPageFlip.isAnimating() && mPageFlip.getFirstPage() != null) {
            mPageFlip.onFingerDown(getX_position_to_flipForward(), getY_position_to_flipForward());
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPageFlipView.onFingerUp(getX_position_to_flipForward(), getY_position_to_flipForward());
            }
        }, sleep_duration);

    }

    public void flip_toPreviousPage(int sleep_duration) {
//        mPageFlipView.onUserFinger_isDown(getX_position_to_flipBackward(), getY_position_to_flipBackward());
        PageFlip mPageFlip = getmPageFlip();
        final PageFlipView mPageFlipView = getmPageFlipView();
        if (!mPageFlip.isAnimating() && mPageFlip.getFirstPage() != null) {
            mPageFlip.onFingerDown(getX_position_to_flipBackward(), getY_position_to_flipBackward());
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPageFlipView.onFingerUp(getX_position_to_flipBackward(), getY_position_to_flipBackward());
            }
        }, sleep_duration);

    }

    private boolean can_start_flipping(float minimum_gap) {
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        return Math.abs(minimum_gap) > configuration.getScaledTouchSlop() * 4;
    }


    public boolean is_to_flip() {
        return is_to_flip;
    }

    public boolean setIs_to_flip(boolean is_to_flip) {
        this.is_to_flip = is_to_flip;
        return is_to_flip;
    }

//----------------------------Others----------------------------------------------------------

    public boolean isClickToFlip(MotionEvent e) {
        InputDevice inputDevice = e.getDevice();
        if (inputDevice != null) {
            float max_X = inputDevice.getMotionRange(MotionEvent.AXIS_X).getMax();
            return ((max_X / e.getX()) >= CLICK_FORWARD_SLOP && (max_X / e.getX()) <= CLICK_BACK_SLOP);
        } else {
            float max_X = View_Utils.getScreenResolution(getContext()).width;
            return ((max_X / e.getX()) >= CLICK_FORWARD_SLOP && (max_X / e.getX()) <= CLICK_BACK_SLOP);
        }
    }

    public boolean is_click_toFlipBack(MotionEvent e) {
        InputDevice inputDevice = e.getDevice();
        if (inputDevice != null) {
            float max_X = inputDevice.getMotionRange(MotionEvent.AXIS_X).getMax();
            return (max_X / e.getX()) >= CLICK_BACK_SLOP;
        } else {
            float max_X = View_Utils.getScreenResolution(getContext()).width;
            return (max_X / e.getX()) >= CLICK_BACK_SLOP;
        }
//            float max_X = e.getDevice().getMotionRange(MotionEvent.AXIS_X).getMax();

    }

    public boolean is_click_toFlipForward(MotionEvent e) {
        InputDevice inputDevice = e.getDevice();
        if (inputDevice != null) {
            float max_X = inputDevice.getMotionRange(MotionEvent.AXIS_X).getMax();
            return (max_X / e.getX()) <= CLICK_FORWARD_SLOP;
        } else {
            float max_X = View_Utils.getScreenResolution(getContext()).width;
            return (max_X / e.getX()) <= CLICK_FORWARD_SLOP;
        }
//        float max_X = e.getDevice().getMotionRange(MotionEvent.AXIS_X).getMax();
    }

    public void onResume() {
        if (mPageFlipView != null) {
            mPageFlipView.onResume();
        }
    }

    public void onPause() {

        if (mPageFlipView != null) {
            mPageFlipView.onPause();
        }
    }

    int x;

    public PageFlip getmPageFlip() {
        return mPageFlipView.getmPageFlip();
    }


    public float getX_position_to_flipForward() {
//        context.getResources().getDisplayMetrics().
        return View_Utils.getScreenResolution(getContext()).getWidth() / 1.01f /*- 3.95282f*/;
    }

    public float getY_position_to_flipForward() {
//        context.getResources().getDisplayMetrics().
        return View_Utils.getScreenResolution(getContext()).getHeight() / 1.3f /*- 366.662f*/;
    }

    public float getX_position_to_flipBackward() {
//        context.getResources().getDisplayMetrics().
        return 0 + 3.95282f;
    }

    public float getY_position_to_flipBackward() {
//        context.getResources().getDisplayMetrics().
        return View_Utils.getScreenResolution(getContext()).getHeight() / 3.0f /*- 300.662f*/;
    }

    //Refresh trials...........

    private Handler handing_auto_refresh;
    private Runnable auto_refresh_runnable;

    public void CancelAutoRefresh() {
        if (handing_auto_refresh != null && auto_refresh_runnable != null) {
            handing_auto_refresh.removeCallbacks(auto_refresh_runnable);
        }
    }


    Handler refresh_handler = new Handler();
    Runnable r_run;


    public void RefreshFlip() {
        r_run = new Runnable() {
            public void run() {
                PageFlip mPageFlip = getmPageFlip();
                if (!mPageFlip.isAnimating()) {
                    try {
                        mPageFlipView.mDrawLock.lock();
                        if (mPageFlipView.mPageRender != null && mPageFlipView.mPageRender.onRefreshPage()) {
                            mDrawingState = DrawingState.REFRESH_DRAW;
                            mPageFlipView.requestRender();
//                            mPageFlipView.showAnimation();
                        }
                    } finally {
                        mPageFlipView.mDrawLock.unlock();
                    }
                }
            }
        };
        refresh_handler.post(r_run);
        Log.i(TAG, "RefreshFlip: page is refreshed");
    }


    public interface OnPageFlippedListener {
        void onPageFlipped(View page, int page_no, long id);

        void onPageFlippedBackward(View page, int page_no, long id);

        void onPageFlippedForward(View page, int page_no, long id);

        void onFingerDown(View v, int pos);

        void onFingerUp(View v, int pos);

        void onFingerDownToFlip(View page, int page_no);

        void onFingerUpToFlip(View page, int page_no);

        void onFastFlipStart(View page, int page_no, boolean is_forward);

        void onFastFlipEnd(View page, int page_no, boolean is_forward);

    }


    public DrawingState getmDrawingState() {
        return mDrawingState;
    }

    public void setmDrawingState(DrawingState mDrawingState) {
        this.mDrawingState = mDrawingState;
    }
}
