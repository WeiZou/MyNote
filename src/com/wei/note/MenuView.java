package com.wei.note;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
//�������Menuͼ�㣬��ʾȫ�����ļ��м����м��µ���Ŀ����ContentView���ǣ��ƶ�Content����ʱ��ʾ��
public class MenuView extends ViewGroup {

	private FrameLayout mContainer;
        //��ʼ��
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
	//����View�߶�
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int width = getDefaultSize(0, widthMeasureSpec);
		int height = getDefaultSize(0, heightMeasureSpec);
		setMeasuredDimension(width, height);
		final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0,
				height);
		final int menuWidth = getChildMeasureSpec(widthMeasureSpec, 0,
				mContainer.getWidth());
		mContainer.measure(menuWidth, contentHeight);//ȷ���˵�����ĳߴ�
	}

	@Override
	//ȷ������
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = r - l;
		final int height = b - t;
		mContainer.layout(0, 0, 400, height);
	}

	private void init() {
		mContainer = new FrameLayout(getContext());
		super.addView(mContainer);//��ӵ���ViewGroup��
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
