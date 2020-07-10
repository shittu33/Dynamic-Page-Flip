package com.example.adaptablecurlpage.flipping;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;

import androidx.annotation.ColorInt;

import com.eschao.android.widget.pageflip.Page;
import com.eschao.android.widget.pageflip.PageFlip;
import com.example.adaptablecurlpage.flipping.utils.DrawingState;
import com.example.adaptablecurlpage.flipping.utils.View_Utils;

import java.util.LinkedList;

/**
 * Created by Abu Muhsin on 21/11/2018.
 */

public class DynamicFlipView extends AdapterView<Adapter> {
    private static final int MAX_RELEASED_VIEW_SIZE = 1;
    public static final String TAG = "AdapterPageFlipView";
    public static final float CLICK_FORWARD_SLOP = 1.087613F;
    public static final float CLICK_BACK_SLOP = 12.416F;

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
    private LinkedList<View> releasedViews = new LinkedList<>();
    private int bufferIndex = -1;
    public int adapterIndex = -1;
    private int adapterDataCount = 0;
    private final int sideBufferSize = 1;
    private boolean is_to_flip = true;
    private float last_position;
    public OnPageFlippedListener onPageFlippedListener;
    private DrawingState mDrawingState;
    //----------------------------Constructors----------------------------------------------------

    public DynamicFlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        includePageFlipView(context);
        this.context = context;

    }

    public DynamicFlipView(Context context) {
        super(context);
        this.context = context;
        includePageFlipView(context);
    }
    //----------------------------View Adapter Methods--------------------------------------------

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
//        MakeAdapterVisible();
    }

    public boolean is_selected_view = false;

    public void set_flexible_Selection(int position) {
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
        View selectedView = viewFromAdapter(position, true);
        Log.i(TAG, "Position of the selected view is " + position);
        bufferedViews.add(selectedView);
        Log.i(TAG, "selected view added");
//        onPageFlippedListener.onPageFlipped(selectedView, position, selectedView.getId());
        Log.i(TAG, "after FlipListener");
        for (int i = 1; i <= sideBufferSize; i++) {
            int next = position + i;
            if (next < adapterDataCount) {
                is_selected_view = true;
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
        updateVisibleView(bufferIndex);
        listenForPageFlip();
    }

    public void setAdapterDataCount(int adapterDataCount) {
        this.adapterDataCount = adapterDataCount;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        RefreshFlip();
        Log.i(TAG, "AdapterFlipView onDraw is called");
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
        return (bufferIndex < bufferedViews.size() && bufferIndex >= 0) ? bufferedViews.get(bufferIndex)
                : null;
    }

    public int getAdapterDataCount() {
        return adapterDataCount;
    }


    public enum FlipSpeed {
        VERY_FAST,
        FAST,
        SLOW,
        NORMAL
    }

    public void ResetFlipSpeed() {
        getmPageFlipView().setFlipDuration(1500);
        setInfiniteFlipForwardDuration(900);
        setInfiniteFlipForwardinterval(280);
        setInfiniteFlipBackwardDuration(350);
        setInfiniteFlipBackwardinterval(300);
        setFlipBackwardDuration(1000);
    }

    public void setFlipSpeed(FlipSpeed flipSpeed) {
        switch (flipSpeed) {
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
    private View viewFromAdapter(int position, boolean addToTop) {
        View releasedView = releasedViews.isEmpty() ? null : releasedViews.removeFirst();
        Log.i(TAG, "viewFromAdapter: After releasedView");
        View view = adapter.getView(position, releasedView, this);
//        if (adapter instanceof CustomFlipViewBaseAdapter) {
//            onPageFlippedListener.onImageLoadingStatus(view, position, ((CustomFlipViewBaseAdapter) adapter).is_viewFullyLoaded());
//        }
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

    public void setMaxBackAlpha(float alpha) {
        mPageFlipView.setMaxBackAlpha(alpha);
    }

    public void useDominantColorForFoldBack(boolean useDominantColorForFoldBack) {
        Page.UseDominantColorForFoldBack(useDominantColorForFoldBack);
    }

    public void setPageBackColor(@ColorInt int color) {
        Page.setFoldPageBackColor(color);
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
        SupplyViewsToBitmapLoader();
    }

    public LinkedList<View> getBufferedViews() {
        return bufferedViews;
    }

    public int getBufferIndex() {
        return bufferIndex;
    }

    public void SupplyViewsToBitmapLoader() {
//        for (int i = 1; i <= sideBufferSize; i++) {
        if (bufferedViews.size() >= 1) {
            //buffer index 0
            View previousView = null;
            View selectedView = bufferedViews.get(bufferIndex);//e.g view 0, index 0
            if (onPageFlippedListener != null) {
                onPageFlippedListener.onAfterViewLoadedToFlip(bufferIndex);
            }

            Log.i(TAG, " bufferView: selected view was added and index is " + adapterIndex);
            View nextView = null;
            if (bufferIndex - 1 >= 0) {
                previousView = bufferedViews.get(bufferIndex - 1);
                Log.i(TAG, " bufferView: previous view was added");
            } else {
                Log.i(TAG, "No previous View here");
            }
            if (bufferIndex < bufferedViews.size() - 1) {
                nextView = bufferedViews.get(bufferIndex + 1);//e.g view 1, index 1
                Log.i(TAG, " bufferView: next view was added");
            } else {
                Log.i(TAG, "view is null, no more view");
            }
            final FlipItem previous = new FlipItem(adapterIndex - 1, previousView);
            final FlipItem current = new FlipItem(adapterIndex, selectedView);
            final FlipItem next = new FlipItem(adapterIndex + 1, nextView);
            BitmapLoader.get(context).loadViewsForFlipping(this, previous, current, next);
//            BitmapLoader.get(context).loadViewsForFlipping(this
//                    , adapterIndex - 1, previousView, adapterIndex, selectedView,
//                    adapterIndex + 1, nextView);
        }
    }

    public void getViewForBitmap(int which) {

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
        int changed_active_index = onPageFlippedListener.onChangeActiveIndex(activeIndex);
        Log.i(TAG, "changed_active_index is " + changed_active_index);
        if (changed_active_index > -1) {
            activeIndex = changed_active_index;
        }
        releaseViews();
        setSelection(activeIndex);
    }

    private void releaseViews() {
        for (View view : /*typed_bufferedViews[viewType]*/bufferedViews) {
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
//        Assert.assertNotNull(view);
        if (releasedViews /*typed_releasedViews[viewType]*/.size() < MAX_RELEASED_VIEW_SIZE) {
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
//        typed_bufferedViews[viewType].clear();
        adapterIndex = 0;
        mPageFlipView = null;
    }


//----------------------------PageFlipView Utils(Our Surface View)----------------------------

    public void setOnPageFlippedListener(OnPageFlippedListener onPageFlippedListener) {
        this.onPageFlippedListener = onPageFlippedListener;
    }

    private void includePageFlipView(Context context) {
        if (attrs != null) {
            mPageFlipView = new PageFlipView(context, this, attrs);
        } else {
            mPageFlipView = new PageFlipView(context, this);
        }
        Log.i(TAG, "before addViewInLayout");
        addViewInLayout(mPageFlipView, -1, new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT), false);
        Log.i(TAG, "After addViewInLayout");
    }


    public void MakeAdapterVisible() {
        mPageFlipView.setAlpha(0);
    }


    public void MakeFlipVisible() {
        mPageFlipView.setAlpha(1);
    }

    public void ShowFlipView() {
        if (mPageFlipView.getVisibility() == GONE) {
            try {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        setSelection(adapterIndex);
                    }
                });
                SupplyViewsToBitmapLoader();
                mPageFlipView.setVisibility(VISIBLE);
                AutoRefreshAfter(500);
                Log.i(TAG, "FlipView is visible now");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void hideFlipView() {
        setIs_to_flip(false);
        if (mPageFlipView.getVisibility() == VISIBLE) {
            try {
//                SupplyViewsToBitmapLoader();
//                AutoRefreshAfter(500);
//                getmPageFlipView().startAnimation(AnimationUtils.loadAnimation(context, R.anim.flip_fade_in));
                mPageFlipView.setVisibility(GONE);
                Log.i(TAG, "View is gone");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public void hideFlipViewForRefresh() {

        if (mPageFlipView.getVisibility() == VISIBLE /*&& is_to_flip*/) {
            try {
                setSelection(adapterIndex);
//                View_Utils.ShowView_with_ZoomOut((ViewGroup) mPageFlipView.getParent());
                mPageFlipView.setVisibility(GONE);
                Log.i(TAG, "View is gone");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void ShowFlipViewForRefresh() {
        if (mPageFlipView.getVisibility() == GONE /*&& !is_to_flip*/) {
            try {
                setSelection(adapterIndex);
                mPageFlipView.setVisibility(VISIBLE);
                Log.i(TAG, "FlipView is visible now");
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setIs_to_flip(true);
                }
            }, 100);
        }
    }

    private void updateVisibleView(int index) {
        for (int i = 0; i < bufferedViews/*typed_bufferedViews[viewType]*/.size(); i++) {
            bufferedViews/*typed_bufferedViews[viewType]*/.get(i).setVisibility(index == i ? VISIBLE : GONE);
        }
    }

    public void PostflippedToView(final int indexInAdapter) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    flippedToView(indexInAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    int last_index = -1;

    public void listenForPageFlip() {
        if (onPageFlippedListener != null)
            onPageFlippedListener.onPageFlipped(
                    bufferedViews/*typed_bufferedViews[viewType]*/.get(bufferIndex)
                    , adapterIndex
                    , bufferedViews/*typed_bufferedViews[viewType]*/.get(bufferIndex).getId());
        if (last_index == -1) {
            last_index = adapterIndex;
        } else {
            if (last_index < adapterIndex) {
                //Forward
                if (onPageFlippedListener != null)
                    onPageFlippedListener.onPageFlippedForward(
                            bufferedViews/*typed_bufferedViews[viewType]*/.get(bufferIndex)
                            , adapterIndex
                            , bufferedViews/*typed_bufferedViews[viewType]*/.get(bufferIndex).getId());
            } else if (last_index > adapterIndex) {
                if (onPageFlippedListener != null)
                    onPageFlippedListener.onPageFlippedBackward(
                            bufferedViews/*typed_bufferedViews[viewType]*/.get(bufferIndex)
                            , adapterIndex
                            , bufferedViews/*typed_bufferedViews[viewType]*/.get(bufferIndex).getId());
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
                    if (adapterIndex - sideBufferSize >= 0) { // e.g 0 == 0, continue...

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

    public void checkViewStatus() {
        if (adapter instanceof CustomFlipViewBaseAdapter) {
            if (onPageFlippedListener != null)
                onPageFlippedListener.onImageLoadingStatus(getSelectedView(), adapterIndex, ((CustomFlipViewBaseAdapter) adapter).is_viewFullyLoaded());
        }
    }

    public PageFlipView getmPageFlipView() {
        return mPageFlipView;
    }

    //----------------------------Handling Touch Events----------------------------------------------------------


    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        if (is_to_flip()) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (onPageFlippedListener != null) {
                        onPageFlippedListener.onHold(getSelectedView(), adapterIndex);
                    }
                    is_click_trigger = true;
                    mPageFlipView.onFingerDown(ev.getX(), ev.getY());
                    last_position = ev.getX();
                    return !isClickToFlip(ev);
//                     return false;
                case MotionEvent.ACTION_UP:
//                    long time_now = System.currentTimeMillis();
                    if (onPageFlippedListener != null) {
                        onPageFlippedListener.onRelease(getSelectedView(), adapterIndex);
                    }
                    if (mPageFlipView.isShown()) {
                        mPageFlipView.onFingerUp(ev.getX(), ev.getY());
                        return !isClickToFlip(ev);
//                        return false;
                    }
                case MotionEvent.ACTION_MOVE:
                    float distance_move = last_position - ev.getX();
                    if (can_start_flipping(distance_move)  /*&& !isClickToFlip(ev)*/) {
                        is_click_trigger = false;
                        mPageFlipView.onFingerMove(ev.getX(), ev.getY());
                        return true;
                    }
                    last_position = ev.getX();

            }
        }
        return false;
    }

    private boolean can_start_moving(long time) {
        Log.e(TAG, "onInterceptTouchEvent: time long is " +
                ViewConfiguration.getLongPressTimeout());
        return time >= ViewConfiguration.getLongPressTimeout();

    }

    boolean is_click_trigger = false;

    public void setIs_click_trigger(boolean is_click_trigger) {
        this.is_click_trigger = is_click_trigger;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (is_to_flip()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    is_click_trigger = true;
                    if (onPageFlippedListener != null) {
                        onPageFlippedListener.onUserFinger_isDown(getSelectedView(), adapterIndex);
                    }
                    mPageFlipView.setAlpha(1);
                    mPageFlipView.onFingerDown(event.getX(), event.getY());
                    infinite_flip_run_down = new Runnable() {
                        @Override
                        public void run() {
                            if (is_click_toFlipForward(event)) {
                                is_click_trigger = true;
                                is_infiniteFlipForward = true;
                                if (onPageFlippedListener != null)
                                    onPageFlippedListener.onFastFlippingStarted(getSelectedView(), adapterIndex, true);
                                flip_InfinitelyWithLongtouch(event.getX(), event.getY(), infiniteFlipForwardDuration, true);
                            } else if (is_click_toFlipBack(event)) {
                                is_click_trigger = true;
                                is_infiniteFlipBackward = true;
                                if (onPageFlippedListener != null)
                                    onPageFlippedListener.onFastFlippingStarted(getSelectedView(), adapterIndex, false);
                                flip_InfinitelyWithLongtouch(event.getX(), event.getY(), infiniteFlipBackwardDuration, false);
                            }
                        }
                    };
                    infinite_flip_thread_down.postDelayed(infinite_flip_run_down, ViewConfiguration.getLongPressTimeout());
                    return true;
                case MotionEvent.ACTION_UP:
                    infinite_flip_thread_down.removeCallbacks(infinite_flip_run_down);
                    infinite_flip_thread.removeCallbacks(infinite_flip_run);
                    if (is_infiniteFlipForward) {
                        checkViewStatus();
                        if (onPageFlippedListener != null) {
                            onPageFlippedListener.onFastFlippingEnded(getSelectedView(), adapterIndex, true);
                        }
                    } else if (is_infiniteFlipBackward) {
                        checkViewStatus();
                        if (onPageFlippedListener != null)
                            onPageFlippedListener.onFastFlippingEnded(getSelectedView(), adapterIndex, false);
                    }
                    if (is_click_toFlipBack(event)) {
                        mPageFlipView.setFlipDuration(FlipBackwardDuration);
                    }
                    mPageFlipView.onFingerUp(event.getX(), event.getY());
                    if (is_click_toFlipBack(event) && !(is_infiniteFlipForward || is_infiniteFlipBackward)) {
                        ResetFlipDuration(infiniteFlipBackwardDuration - 100);
                    }
                    if (!is_infiniteFlipBackward || !is_infiniteFlipForward) {
                        if (onPageFlippedListener != null)
                            onPageFlippedListener.onUserFinger_isUp(getSelectedView(), adapterIndex);
                    }
                    is_infiniteFlipForward = false;
                    is_infiniteFlipBackward = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    mPageFlipView.onFingerMove(event.getX(), event.getY());
//                    is_click_trigger = false;
                    return true;
            }
        }
        return false;
    }

    public boolean is_click_trigger() {
        return is_click_trigger;
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
                    mPageFlipView.UpdateFromAdapter();
                    mPageFlipView.requestRender();
                    if (adapterIndex != page_no - 1) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                MakeFlipVisible();
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

    public void flip_InfinitelyWithLongtouch(float x, float y, int duration,
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
            if (onPageFlippedListener != null)
                onPageFlippedListener.onEndPageReached(getSelectedView(), adapterIndex, flip_forward);
        }
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
                    mPageFlipView.UpdateFromAdapter();
                    mPageFlipView.requestRender();
//                    checkViewStatus();
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
        return Math.abs(minimum_gap) > configuration.getScaledTouchSlop() *4;
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

    public interface OnPageFlippedListener {
        void onPageFlipped(View page, int page_no, long id);

        void onPageFlippedBackward(View page, int page_no, long id);

        void onPageFlippedForward(View page, int page_no, long id);

        void onUserFinger_isDown(View page, int page_no);

        void onFastFlippingStarted(View page, int page_no, boolean is_forward);

        int onChangeActiveIndex(int active_index);

        void onFastFlippingEnded(View page, int page_no, boolean is_forward);

        void onRefreshForwardBackwardFinished(int time_taken);

        void onUserFinger_isUp(View page, int page_no);

        void onImageLoadingStatus(View page, int page_no, boolean is_ViewFullyLoaded);

        void onAfterViewLoadedToFlip(int page_no);

        void onRestorePage(int page_no, boolean is_forward);

        void onResetFlipSpeed();

        void onHold(View v, int pos);

        void onRelease(View v, int pos);

        void onEndPageReached(View v, int pos, boolean flip_forward);
    }

    //Refresh trials...........

    private Handler handing_auto_refresh;
    private Runnable auto_refresh_runnable;

    public void CancelAutoRefresh() {
        if (handing_auto_refresh != null && auto_refresh_runnable != null) {
            handing_auto_refresh.removeCallbacks(auto_refresh_runnable);
        }
    }

    public void AutoRefreshAfter(int duration) {
        CancelAutoRefresh();
        handing_auto_refresh = new Handler();
        auto_refresh_runnable = new Runnable() {
            @Override
            public void run() {
//                adapterPageFlipView.getmPageFlipView().startAnimation(AnimationUtils.loadAnimation(context, R.anim.flip_fade_in));
                RefreshFlip();
                MakeFlipVisible();
            }
        };

        handing_auto_refresh.postDelayed(auto_refresh_runnable, duration);
    }


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

    public boolean is_from_refresh;

    // First attempts to refresh pages...........................................
    public void Refresh_Forward() {
        MakeAdapterVisible();
        if (adapterDataCount <= 1) {
            return;
        }
        if (adapterIndex == 0) {
            RefreshBack_Forward();
            return;
        }
        setSelection(adapterIndex > 0 ? adapterIndex - 1 : adapterIndex);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                is_from_refresh = true;
                mPageFlipView.setFlipDuration(900);
                flip_toNextPage(100);
            }
        }, 300);
        ResetFlipDuration(1000);
    }


    public void RefreshBack_Forward() {
        if (adapterDataCount <= 1) {
            MakeAdapterVisible();
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                is_from_refresh = true;
                mPageFlipView.setFlipDuration(600);
                flip_toNextPage(100);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flip_toPreviousPage(100);
                    }
                }, 400);
            }
        }, 300);
        ResetFlipDuration(1500);
    }

    public void MultipleFlipRefresh(final boolean forward) {
        if (adapterIndex == 0) {
            RefreshBack_Forward();
            return;
        }
        setSelection(adapterIndex > 0 ? adapterIndex - 1 : adapterIndex);
//        View_Utils.ShowView_with_ZoomOut((ViewGroup) getmPageFlipView().getParent());
        is_from_refresh = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getmPageFlipView().setFlipDuration(600);
                flip_forward_to(adapterIndex + 2);
            }
        }, 200);
        mPageFlipView.setFlipDuration(400);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                flip_toPreviousPage(100);
            }
        }, 800);
        ResetFlipDuration(800);
    }

    Handler refresh_handler = new Handler();
    Runnable r_run;

    public void CancleRefresh() {
        refresh_handler.removeCallbacks(r_run);
    }
//    ........................................................................


    public DrawingState getmDrawingState() {
        return mDrawingState;
    }

    public void setmDrawingState(DrawingState mDrawingState) {
        this.mDrawingState = mDrawingState;
    }

    public synchronized void ResetFlipDuration(final int wait_duration) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (is_from_refresh) {
                    MakeFlipVisible();
                    if (onPageFlippedListener != null)
                        onPageFlippedListener.onRefreshForwardBackwardFinished(wait_duration);
                    is_from_refresh = false;
                }
                if (onPageFlippedListener != null)
                    onPageFlippedListener.onResetFlipSpeed();
            }
        }, wait_duration);
    }

}
