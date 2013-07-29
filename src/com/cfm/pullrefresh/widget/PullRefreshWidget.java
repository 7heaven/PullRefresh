package com.cfm.pullrefresh.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.cfm.pullrefresh.R;

public class PullRefreshWidget extends LinearLayout {
	
	private static final String TAG = "PullRefreshWidget";
	
	private View mHeadView;
	private AdapterView<?> mAdapterView;
	private ScrollView mScrollView;
	private View mNormalView;
	private PullWidget mPullWidget;
	
	private float dx,dy;
	private int moveDistance;
	private int headOriginalHeight;

	public PullRefreshWidget(Context context){
		super(context);
		init(context);
	}
	
	public PullRefreshWidget(Context context, AttributeSet attrs){
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context){
		setOrientation(LinearLayout.VERTICAL);
		LayoutInflater inflater = LayoutInflater.from(context);
		mHeadView = inflater.inflate(R.layout.head_pullrefresh, this, false);
		measureChild(mHeadView);
		mPullWidget = (PullWidget) mHeadView.findViewById(R.id.pull_widget);
		
		headOriginalHeight = mHeadView.getMeasuredHeight();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, headOriginalHeight);
		params.topMargin = -headOriginalHeight;
		addView(mHeadView, params);
	}
	
	@Override
	public void onFinishInflate(){
		super.onFinishInflate();
		
		initContent();
	}
	
	private void initContent(){
		int count = getChildCount();
		if(count != 2) throw new ArrayIndexOutOfBoundsException("this widget must and at most contain one child view");
		
		View view = getChildAt(1);
		if(view instanceof AdapterView<?>){
			mAdapterView = (AdapterView<?>) view;
		}else if(view instanceof ScrollView){
			mScrollView = (ScrollView) view;
		}else{
			mNormalView = view;
		}
	}
	
	private void measureChild(View child){
		ViewGroup.LayoutParams params = child.getLayoutParams();
		if(null == params){
			params = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}
		
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, params.width);
		int childHeightSpec;
		if(params.height > 0){
			childHeightSpec = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY);
		}else{
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		
		child.measure(childWidthSpec, childHeightSpec);
	}
	
	private void setHeadMargin(int height){
		LayoutParams params = (LayoutParams) mHeadView.getLayoutParams();
		params.topMargin = height;
		
		mHeadView.setLayoutParams(params);
	}
	
	private boolean shouldRefreshStart(int dy){
		if(dy < 0) return false;
		if(null != mNormalView) return true;
		if(null != mScrollView){
			if(mScrollView.getChildAt(0).getScrollY() == 0) return true;
		}
		if(null != mAdapterView){
			int top = mAdapterView.getChildAt(0).getTop();
			int padding = mAdapterView.getPaddingTop();
			if(mAdapterView.getFirstVisiblePosition() == 0){
				if(top == 0 || Math.abs(top - padding) <= 8){
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event){
		switch(event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			dx = event.getX();
			dy = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			dx = event.getX() - dx;
			dy = event.getY() - dy;
			if(Math.abs(dx) < Math.abs(dy)){
				if(shouldRefreshStart((int) dy)){
					return true;
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			break;
		}
		
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		switch(event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			dx = event.getX();
			dy = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			dy = event.getY() - dy;
			if(dy <= headOriginalHeight){
				setHeadMargin((int) dy);
			}else{
				mPullWidget.setHeight((int) dy);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			break;
		}
		return super.onTouchEvent(event);
	}
}
