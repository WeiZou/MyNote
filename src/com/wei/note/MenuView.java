package com.wei.note;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
//主界面的Menu图层，显示全部的文件夹及其中记事的数目，被ContentView覆盖，移动Content界面时显示它
public class MenuView extends ViewGroup {

	private FrameLayout mContainer;
        //初始化
	public MenuView(Context context) {
		super(context);
		init();
	}

	public MenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MenuView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	//测量View尺度
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int width = getDefaultSize(0, widthMeasureSpec);
		int height = getDefaultSize(0, heightMeasureSpec);
		setMeasuredDimension(width, height);
		final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0,
				height);
		final int menuWidth = getChildMeasureSpec(widthMeasureSpec, 0,
				mContainer.getWidth());
		mContainer.measure(menuWidth, contentHeight);//确定菜单界面的尺寸
	}

	@Override
	//确定布局
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = r - l;
		final int height = b - t;
		mContainer.layout(0, 0, 400, height);
	}

	private void init() {
		mContainer = new FrameLayout(getContext());
		super.addView(mContainer);//添加到父ViewGroup中
	}

	public void setView(View v) {
		if (mContainer.getChildCount() > 0) {
			mContainer.removeAllViews();
		}
		mContainer.addView(v);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		View child;
		for (int i = 0; i < getChildCount(); i++) {
			child = getChildAt(i);
			child.setFocusable(true);
			child.setClickable(true);
		}
	}
}
