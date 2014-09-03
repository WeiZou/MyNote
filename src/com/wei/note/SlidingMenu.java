package com.wei.note;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
//ContentView和MenuView的父容器
public class SlidingMenu extends RelativeLayout {

	private MenuView mMenuView;
	private ContentView mContentView;
        //初始函数
	public SlidingMenu(Context context) {
		super(context);
		init(context);
	}

	public SlidingMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
	        //将ContentView和MenuView设置为它的子容器，其中ContentView在上层，MenuView在下层暂时被覆盖，只有ContentView右移才会显示
		LayoutParams behindParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		mMenuView = new MenuView(context);
		addView(mMenuView, behindParams);
		LayoutParams aboveParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		mContentView = new ContentView(context);
		addView(mContentView, aboveParams);
	}

	public void setMenu(View v) {
		mMenuView.setView(v);
		mMenuView.invalidate();
	}

	public void setContent(View v) {
		mContentView.setView(v);
		mContentView.invalidate();
	}

	public void showMenu() {
		mContentView.toggle();
	}
	

}
