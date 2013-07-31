package com.cfm.pullrefresh.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.cfm.pullrefresh.R;
import com.cfm.pullrefresh.widget.PullWidget.OnStateChangeListener;

public class PullRefreshWidget extends LinearLayout implements OnStateChangeListener{
	
	private static final String TAG = "PullRefreshWidget";
	
	private static final int MODE_REFRESHING = 0;
	private static final int MODE_IDLE = 1;
	private static final int MODE_DRAGGING = 2;
	private static final int MODE_REFRESH_START = 3;
	
	private int mode = MODE_IDLE;
	
	private View mHeadView;
	private AdapterView<?> mAdapterView;
	private ScrollView mScrollView;
	private View mNormalView;
	private PullWidget mPullWidget;
	
	private float dx,dy;
	private int moveDistance;
	private int headOriginalHeight;
	
	private Handler handler;
	private MoveRunnable moveRunnable;
	private HeadMoveRunnable headMoveRunnable;
	
	private OnRefreshListener onRefreshListener;
	
	private class MoveRunnable implements Runnable{
		int startY;
		
		public MoveRunnable(int startY){
			stopMovement();
			this.startY = startY;
		}
		
		@Override
		public void run(){
			startY += (headOriginalHeight - startY) * 0.5F;
			
			mPullWidget.setHeight(startY - headOriginalHeight);
			setChildHeight(mHeadView, startY);
			
			if(startY != headOriginalHeight){
				handler.postDelayed(moveRunnable, 20);
			}else{
				switch(mode){
				case MODE_REFRESHING:
				case MODE_REFRESH_START:
					mPullWidget.circling();
					break;
				case MODE_DRAGGING:
					smoothHideHeadView();
					break;
				}
			}
		}
	}
	
	public class HeadMoveRunnable implements Runnable{
		LayoutParams params;
		
		public HeadMoveRunnable(){
			params = (LayoutParams) mHeadView.getLayoutParams();
		}
		
		@Override
		public void run(){
			params.topMargin += (-headOriginalHeight - params.topMargin) * 0.5F;
			
			mHeadView.setLayoutParams(params);
			
			if(params.topMargin != -headOriginalHeight){
				handler.postDelayed(headMoveRunnable, 20);
			}else{
				mode = MODE_IDLE;
			}
		}
	}
	
	public interface OnRefreshListener{
		public void onRefresh();
	}

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
		mPullWidget.setOnStateChangeListener(this);
		
		headOriginalHeight = mHeadView.getMeasuredHeight();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, headOriginalHeight);
		params.topMargin = -headOriginalHeight;
		addView(mHeadView, params);
		
		handler = new Handler();
	}
	
	@Override
	public void onFinishInflate(){
		super.onFinishInflate();
		
		initContent();
	}
	
	private void initContent(){
		int count = getChildCount();
		if(count != 2) throw new ArrayIndexOutOfBoundsException("this widget contain one child view at most and must contain one.");
		
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
	
	private void setChildHeight(View child, int height){
		ViewGroup.LayoutParams params = child.getLayoutParams();
		params.height = height;
		child.setLayoutParams(params);
	}
	
	private void setHeadMargin(int height){
		LayoutParams params = (LayoutParams) mHeadView.getLayoutParams();
		params.topMargin = height - headOriginalHeight;
		
		mHeadView.setLayoutParams(params);
	}
	
	private boolean shouldRefreshStart(){
		if(null != mNormalView) return true;
		if(null != mScrollView){
			if(mScrollView.getChildAt(0).getScrollY() == 0) return true;
		}
		if(null != mAdapterView){
			int top = mAdapterView.getChildAt(0).getTop();
			int padding = mAdapterView.getPaddingTop();
			if(mAdapterView.getFirstVisiblePosition() == 0){
				if(top == 0 || Math.abs(top - padding) <= 8){
					Log.d(TAG, "top-padding:" + Math.abs(top - padding));
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void smoothToOriginalSpot(int y){
		moveRunnable = new MoveRunnable(y);
		
		handler.post(moveRunnable);
	}
	
	private void stopMovement(){
		handler.removeCallbacks(moveRunnable);
		handler.removeCallbacks(headMoveRunnable);
	}
	
	private void smoothHideHeadView(){
		headMoveRunnable = new HeadMoveRunnable();
		
		handler.post(headMoveRunnable);
	}
	
	public void setOnRefreshListener(OnRefreshListener onRefreshListener){
		this.onRefreshListener = onRefreshListener;
	}
	
	public OnRefreshListener getOnRefreshListener(){
		return onRefreshListener;
	}
	
	public void onRefreshComplete(){
		mPullWidget.stopCirclingAndReturn();
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event){
		dx = event.getRawX();
		dy = event.getRawY();
		switch(event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			moveDistance = (int) dy;
			break;
		case MotionEvent.ACTION_MOVE:
			moveDistance = (int) (event.getRawY() - moveDistance);
			if(Math.abs(event.getRawX() - dx) < Math.abs(moveDistance)){
				if(shouldRefreshStart() && moveDistance > 0){
					Log.d(TAG, "shouldRefreshStart");
					return true;
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			break;
		}
		
		return super.onInterceptTouchEvent(event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		Log.d(TAG, "MODE:" + mode);
		Log.d(TAG, "TOUCH:" + event.toString());
		switch(event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			stopMovement();
			moveDistance = 0;
			dx = event.getX();
			dy = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			moveDistance = (int) (event.getY() - dy);
			switch(mode){
			case MODE_IDLE:
				Log.d(TAG, "IDLE");
				if(shouldRefreshStart() && mode != MODE_REFRESHING && moveDistance > 0) mode = MODE_DRAGGING;
			    break;
			case MODE_DRAGGING:
				if(moveDistance < 0){
					moveDistance = 0;
				}
				Log.d(TAG, "DRAGGING");
				if(moveDistance <= headOriginalHeight){
					setHeadMargin((int) moveDistance);
					mPullWidget.setHeight(0);
					setChildHeight(mHeadView, headOriginalHeight);
				}else{
					setHeadMargin(headOriginalHeight);
					mPullWidget.setHeight((int) moveDistance - headOriginalHeight);
					setChildHeight(mHeadView, (int) moveDistance);
					if(mPullWidget.isExceedMaximumHeight()){
						mode = MODE_REFRESH_START;
					}
				}
				break;
			case MODE_REFRESH_START:
				smoothToOriginalSpot(moveDistance);
				mode = MODE_REFRESHING;
				break;
			case MODE_REFRESHING:
				if(null != onRefreshListener) onRefreshListener.onRefresh();
				break;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(mode == MODE_DRAGGING) {
				if(moveDistance > headOriginalHeight){
					smoothToOriginalSpot(moveDistance);
				}else{
					smoothHideHeadView();
				}
			}
			break;
		}
		return true;
	}

	@Override
	public void onCirclingFullyStop() {
		smoothHideHeadView();
	}

	@Override
	public void onPullFullyStop() {
		
	}
}
