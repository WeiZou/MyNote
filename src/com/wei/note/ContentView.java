package com.wei.note;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Scroller;

//主界面的Content图层，显示对应文件夹下的记事列表，并响应屏幕滑动事件
public class ContentView extends ViewGroup {

	private FrameLayout mContainer;
	private Scroller    mScroller;
	private VelocityTracker mVelocityTracker;
	private int mTouchSlop;
	private float mLastMotionX;
	private float mLastMotionY;
	//触摸屏幕的状态：按下、移动或者抬起
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private static final int SNAP_VELOCITY = 1000;
	private int menuWidth = 400;//Content界面右移的宽度
        
	public int mTouchState = TOUCH_STATE_REST;//初始触摸状态
        //初始化函数
	public ContentView(Context context) {
		super(context);
		init();
	}

	public ContentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ContentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	//重写容器的测量函数
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mContainer.measure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	//重写容器的布局函数
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = r - l;
		final int height = b - t;
		mContainer.layout(0, 0, width, height);
	}
        
	private void init() {
		mContainer = new FrameLayout(getContext());
		mScroller = new Scroller(getContext());
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();//触发移动的最短距离
		super.addView(mContainer);
	}

	public void setView(View v) {
		if (mContainer.getChildCount() > 0) {
			mContainer.removeAllViews();
		}
		mContainer.addView(v);
	}

	@Override
	//重写移动函数
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);//调用父类的scrollTo函数
		postInvalidate();//重新绘制
	}

	@Override
	//判断屏幕移动是否完成，并实现对应的操作
	public void computeScroll() {
		if (!mScroller.isFinished()) {
		        //滚动尚未完成
			if (mScroller.computeScrollOffset()) {
				int oldX = getScrollX();
				int oldY = getScrollY();
				int x = mScroller.getCurrX();
				int y = mScroller.getCurrY();
				if (oldX != x || oldY != y) {
					scrollTo(x, y);
				}
				invalidate();
				return;
			}
		}
	}
	@Override
	//重写屏幕触摸事件，实现手指在屏幕右滑时，Content界面右移，左边显示文件列表
	public boolean onTouchEvent(MotionEvent ev) {

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			mLastMotionY = y;
			if (getScrollX() == -200 && mLastMotionX < 200) {//判断移动
				return false;
			}
			Log.e("ad", "ACTION_DOWN");
			break;
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);
			final int yDiff = (int) Math.abs(y - mLastMotionY);
			final int touchSlop = mTouchSlop;
			boolean xMoved = xDiff > touchSlop;
			if (xMoved) {
				mTouchState = TOUCH_STATE_SCROLLING;
				Log.e("ad", "onInterceptTouchEvent  ACTION_MOVE  mTouchState=="
						+ (mTouchState == TOUCH_STATE_SCROLLING));
				enableChildrenCache();
			}
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final float deltaX = mLastMotionX - x;
				mLastMotionX = x;
				float oldScrollX = getScrollX();
				float scrollX = oldScrollX + deltaX;
				final float leftBound = 0;
				final float rightBound = -menuWidth;
				if (scrollX > leftBound) {
					scrollX = leftBound;
				} else if (scrollX < rightBound) {
					scrollX = rightBound;
				}
				scrollTo((int) scrollX, getScrollY());

			}
			Log.e("ad", "ACTION_MOVE");
			break;
		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int velocityX = (int) velocityTracker.getXVelocity();
				int oldScrollX = getScrollX();
				Log.e("ad", "oldScrollX  ==  " + oldScrollX);
				int dx = 0;
				if (oldScrollX < -100) {
					dx = -menuWidth - oldScrollX;
				} else {
					dx = -oldScrollX;
				}
				smoothScrollTo(dx);
				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}
			Log.e("ad", "ACTION_UP");
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.e("ad", "ACTION_CANCEL");
			mTouchState = TOUCH_STATE_REST;
		}

		return true;
	}

	void toggle() {
		int oldScrollX = getScrollX();
		if (oldScrollX == 0) {
			smoothScrollTo(-menuWidth);
		} else if (oldScrollX == -menuWidth) {
			smoothScrollTo(menuWidth);
		}
	}

	void smoothScrollTo(int dx) {
		int duration = 500;
		int oldScrollX = getScrollX();
		mScroller.startScroll(oldScrollX, getScrollY(), dx, getScrollY(),
				duration);
		invalidate();
	}

	void enableChildrenCache() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View layout = (View) getChildAt(i);
			layout.setDrawingCacheEnabled(true);
		}
	}

	void clearChildrenCache() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View layout = (View) getChildAt(i);
			layout.setDrawingCacheEnabled(false);
		}
	}

}
