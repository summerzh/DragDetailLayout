package com.it.dragdetaillayout;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;


/**
 * Created by gyt on 2016/11/14
 */
public class DragDetailLayout2 extends ViewGroup {


    private static final String TAG = DragDetailLayout.class.getSimpleName();
    private final int            mTouchSlop;
    private final ViewDragHelper mViewDragHelper;
    private       View           mBottomView;

    private View mTopView;
    private STATUS mStatus = STATUS.CLOSE;
    private View  mTargetView;
    private float mDownX;
    private float mDownY;

    private int   mHeight;
    private float mTopOffset;
    // 可导致打开的最小滑动距离
    private float threshold = 0.1f;
    private float mDx;
    private float mDy;
    private int   mCurTop;

    public DragDetailLayout2(Context context) {
        this(context, null);
    }

    public DragDetailLayout2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragDetailLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 触发移动的最小距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new DetailsCallBack());

    }


    private class DetailsCallBack extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mTopView || child == mBottomView;
        }

        /**
         * 限制在Y方向的位置
         *
         * @param child
         * @param top
         * @param dy
         * @return
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (child == mTopView) {
                if (top > 0) {
                    top = 0;
                }
            } else if (child == mBottomView) {
                if (top < 0) {
                    top = 0;
                }
            }
            return top;
        }

        /**
         * @param changedView
         * @param left        当前changeView实时的left
         * @param top         当前changeView实时的top
         * @param dx          并不是整个滑动过程的位移，而是竖直方向的瞬时偏移量,
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            Log.d(TAG, "onViewPositionChanged top=" + top + "  dy= " + dy);
            if (changedView == mTopView) {
                // 当两个孩子相互完全覆盖时GONE掉，放置过度绘制
                mBottomView.setVisibility(top == 0 ? GONE : VISIBLE);
                // 通过全局变量mCurTop来同时移动两个孩子的位置
//                mCurTop = top;
                // 通过让每次的偏移量是原来的一般，使控件的滑动有粘滞的效果
                mCurTop = mCurTop + dy / 2;
                requestLayout();
            } else if (changedView == mBottomView) {
                mTopView.setVisibility(top == 0 ? GONE : VISIBLE);
                Log.d(TAG, "onViewPositionChanged mcurtop=" + mCurTop);
//                mCurTop = top - mTopView.getHeight();
                mCurTop = mCurTop + dy / 2;
                requestLayout();
            }
        }

        /**
         * 如果拖动的距离超过了设定值，则打开；否则关闭
         *
         * @param releasedChild
         * @param xvel          手指离开屏幕时的X或Y轴方向的速度
         * @param yvel
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            float offset = 0.0f;
            if (releasedChild == mTopView) {
                // 注意这里移动要强制至float，否则为0.0
                offset = (float) releasedChild.getTop() / releasedChild.getHeight();
                // 该方法也可以移动view，但是只能在onViewReleased方法中调用。
                // 还有一点区别就是：settleCapturedViewAt可以根据最后松手的速度而调整移动速度
                //                                mViewDragHelper.settleCapturedViewAt(0, Math.abs(offset) >= threshold ? -mTopView.getHeight() : 0);
                if (Math.abs(offset) >= threshold) {
                    open();
                } else {
                    close();
                }
            } else if (releasedChild == mBottomView) {
                offset = -(float) mBottomView.getTop() / releasedChild.getHeight();
                //                mViewDragHelper.settleCapturedViewAt(0, Math.abs(offset) >= threshold ? mTopView.getMeasuredHeight() : 0);
                if (Math.abs(offset) >= threshold) {
                    close();
                } else {
                    open();
                }
            }
            //                        invalidate();// 此处不能使用requestLayout
            Log.d(TAG, "offset= " + offset);
        }

        /**
         * 必须返回一个不是0的数，否则父容器不能滑动
         *
         * @param child
         * @return
         */
        @Override
        public int getViewVerticalDragRange(View child) {
            return 1;
        }
    }

    /**
     * 打开
     */
    public void open() {
        mViewDragHelper.smoothSlideViewTo(mTopView, 0, -mTopView.getHeight());
        invalidate();
        mStatus = STATUS.OPEN;
    }

    /**
     * 关闭
     */
    public void close() {
        mViewDragHelper.smoothSlideViewTo(mTopView, 0, 0);
        invalidate();
        mStatus = STATUS.CLOSE;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int childCount = getChildCount();
        if (childCount != 2) {
            throw new RuntimeException("child count should be 2");
        }

        mTopView = getChildAt(0);
        mBottomView = getChildAt(1);
        // 一开始不显示，GONE掉
        mBottomView.setVisibility(GONE);
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
        mTopView.layout(0, mCurTop, mTopView.getMeasuredWidth(), mCurTop + mHeight);
        mBottomView.layout(0, mCurTop + mHeight, mBottomView.getMeasuredWidth(), mCurTop + mHeight * 2);
    }

    /**
     * topView中有scrollView默认是要消费触摸事件的
     * 在ACTION_DOWN不拦截，事件传递到孩子scrollView,scrollView自己就可以滑动了。
     * 在滑动时要执行此处的onInterceptTouchEvent中的ACTION_MOVE，我们要在move时进行判断
     * 当scrollView已经滑动到底时进行事件的拦截，自己处理滑动事件。否则让其自己滑动。
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mViewDragHelper.cancel();
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN");
                mDownX = ev.getX();
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE");
                float moveX = ev.getX();
                float moveY = ev.getY();

                mDx = moveX - mDownX;
                mDy = moveY - mDownY;
                if (canChildScrollVertically((int) mDy)) {
                    Log.d(TAG, "child can scroll, so not intercept");
                    return false;
                } else {
                    Log.d(TAG, "mDy = " + mDy + " mdx= " + mDx + " mTouchSlop=" + mTouchSlop);
                    if (Math.abs(mDy) > Math.abs(mDx) && Math.abs(mDy) > mTouchSlop && !(mStatus == STATUS.CLOSE && mDy > 0 || mStatus == STATUS.OPEN && mDy < 0)) {
                        Log.d(TAG, "can not scroll , so intercept");
                        return true;
                    }
                }
                break;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    /**
     * 如果拦截了事件，则事件就又传到了这里，有父容器处理
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }


    /**
     * ViewCompat.canScrollVertically(view, direction)
     * direction 相对应
     *
     * @param direction 小于0是向上滑动, 大于0是向下滑动
     * @return
     */
    private boolean canChildScrollVertically(int direction) {
        Log.d(TAG, "direction= " + direction);
        return innerCanChildScrollVertically(mTargetView, direction);
    }


    private boolean innerCanChildScrollVertically(View view, int direction) {
        if (view instanceof AbsListView) {
            AbsListView absListView = (AbsListView) view;
            return ViewCompat.canScrollVertically(absListView, direction);
        } else if (view instanceof WebView) {
            WebView webView = (WebView) view;
            return ViewCompat.canScrollVertically(webView, direction);
        } else if (view instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) view;
            return ViewCompat.canScrollVertically(scrollView, direction);
        } else if (view instanceof ViewGroup) {
            final ViewGroup vGroup = (ViewGroup) view;
            View child;
            boolean result = false;
            for (int i = 0; i < vGroup.getChildCount(); i++) {
                child = vGroup.getChildAt(i);

                // 无论是viewGroup还是view都要先判断自己是否可以滑动
                result = result | child.canScrollVertically(direction);
                if (result) {
                    return result;
                }
                if (child instanceof ViewGroup) {
                    result = result | innerCanChildScrollVertically(child, direction);
                } else {
                    result = result | child.canScrollVertically(direction);
                }
            }
            return result;
        }
        return view.canScrollVertically(direction);
    }


    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 确定当前的孩子是哪个
     */
    private void ensureTarget() {
        if (mStatus == STATUS.CLOSE) {
            mTargetView = mTopView;
        } else {
            mTargetView = mBottomView;
        }
    }
}
