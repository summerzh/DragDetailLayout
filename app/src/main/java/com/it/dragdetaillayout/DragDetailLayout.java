package com.it.dragdetaillayout;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.Scroller;

/**
 * created by summer on 2016/11/12
 */
public class DragDetailLayout extends ViewGroup {


    private final int  mTouchSlop;
    private       View mBottomView;
    private       View mTopView;

    private STATUS mStatus = STATUS.CLOSE;
    private View     mTargetView;
    private float    mDownX;
    private float    mDownY;
    private Scroller mScroller;
    private float    mStartX;
    private float    mStartY;

    private static final String TAG = DragDetailLayout.class.getSimpleName();
    private float mDy;
    private int mHeight;
    private float mLastY;
    private float mDyMove;

    public DragDetailLayout(Context context) {
        this(context, null);
    }

    public DragDetailLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragDetailLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 触发移动的最小距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mScroller = new Scroller(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int childCount = getChildCount();
        if (childCount <= 1) {
            throw new RuntimeException("child count should be 2");
        }

        mTopView = getChildAt(0);
        mBottomView = getChildAt(1);
    }

    /**
     * 枚举
     */
    private enum STATUS {
        CLOSE,
        OPEN;

        private static STATUS valueOf(int value) {
            if (value == 0) {
                return CLOSE;
            } else if (value == 1) {
                return OPEN;
            } else {
                return CLOSE;
            }
        }
    }


    /**
     * 测量孩子和自己的尺寸
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 无论父容器的测量要求是什么,都按照EXACTLY进行测量
        int parentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        int parentHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, parentWidthSpec, parentHeightSpec);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        mHeight = getHeight();
        Log.d(TAG, "height= " + mHeight);
        mTopView.layout(0, 0, mTopView.getMeasuredWidth(), mHeight);
        mBottomView.layout(0, mHeight, mBottomView.getMeasuredWidth(), mHeight + mBottomView.getMeasuredHeight());
    }

    /**
     * 如果top和bottom自己可以滑动则不拦截让其滑动,
     * 如果不能滑动或者已经滑动到了底则进行拦截,
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //        return super.onInterceptTouchEvent(ev);
        ensureTarget();
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                return true;
            case MotionEvent.ACTION_MOVE:

                float moveX = ev.getX();
                float moveY = ev.getY();

                float dx = moveX - mDownX;
                float dy = moveY - mDownY;

                if (isScrollVertically(dx, dy)) {
                    // 判断childView是否可以滑动'
                    if (canChildScrollVertically((int) dy)) {
                        return false;
                    } else {
                        return true;
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 是否可以判断为垂直方法的移动
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isScrollVertically(float dx, float dy) {
        if (Math.abs(dy) > Math.abs(dx) && dy >= mTouchSlop) {
            return true;
        }
        return false;
    }

    /**
     * ViewCompat.canScrollVertically(view, direction)
     * direction 相对应
     *
     * @param direction 小于0是向上滑动, 大于0是向下滑动
     * @return
     */
    private boolean canChildScrollVertically(int direction) {
        return innerCanChildScrollVertically(mTargetView, direction);
    }


    private boolean innerCanChildScrollVertically(View child, int direction) {
        if (child instanceof AbsListView) {
            AbsListView absListView = (AbsListView) child;
            return ViewCompat.canScrollVertically(absListView, direction);
        } else if (child instanceof WebView) {
            WebView webView = (WebView) child;
            return ViewCompat.canScrollVertically(webView, direction);
        } else if (child instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) child;
            int childCount = viewGroup.getChildCount();
            boolean scrollVertically = false;
            for (int i = 0; i < childCount; i++) {
                View view = viewGroup.getChildAt(i);
                scrollVertically = scrollVertically || innerCanChildScrollVertically(view, direction);
            }
            return scrollVertically;
        }
        return ViewCompat.canScrollVertically(child, direction);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartY = event.getY();

                mLastY = mStartY;
                return true;
            case MotionEvent.ACTION_MOVE:

                float moveY = event.getY();
                mDyMove = moveY - mLastY;
                mLastY = moveY;

                if (mStatus == STATUS.CLOSE && mDyMove >= 0 || mStatus == STATUS.OPEN && mDyMove <= 0) {
                    return false;
                }
                scrollBy(0, -(int) mDyMove);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                if (mStatus == STATUS.CLOSE && mDyMove >= 0 || mStatus == STATUS.OPEN && mDyMove <= 0) {
                    return false;
                }
                int scrollY = getScrollY();
                Log.d(TAG, "scrolly = " + scrollY);
                mDy = event.getY() - mStartY;
                Log.d(TAG, "mDy = " + mDy);
                dragMove(scrollY, mDy);
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void dragMove(float scrollY, float dy) {
        if (Math.abs(dy) >= 200) {
            if (mStatus == STATUS.CLOSE) {
                mScroller.startScroll(0, (int) scrollY, 0, (int) (mHeight - scrollY), 500);
                mStatus = STATUS.OPEN;
            } else if(mStatus == STATUS.OPEN){
                mScroller.startScroll(0, (int) scrollY, 0, (int) (-scrollY), 500);
                mStatus = STATUS.CLOSE;
            }
        } else {
            if (mStatus == STATUS.CLOSE) {
                mScroller.startScroll(0, (int) scrollY, 0, (int) -scrollY, 500);
                mStatus = STATUS.CLOSE;
            } else if(mStatus == STATUS.OPEN){
                mScroller.startScroll(0, (int) scrollY, 0, (int) (mHeight - scrollY), 500);
                mStatus = STATUS.OPEN;
            }
        }
        invalidate();
    }

    private void dragClose() {

    }

    private void dragOpen() {
        if (mStatus == STATUS.CLOSE) {
            mScroller.startScroll(0, 0, 0, mHeight, 0);
        } else {

        }
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            Log.d(TAG, "currentY = " + mScroller.getCurrY());
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    private void ensureTarget() {
        if (mStatus == STATUS.CLOSE) {
            mTargetView = mTopView;
        } else {
            mTargetView = mBottomView;
        }
    }
}
