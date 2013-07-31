package com.cfm.pullrefresh.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.cfm.pullrefresh.R;

public class PullWidget extends View{
	
	private static final String TAG = "PullWidget";
	
	private int width,height;
	private RectF topBound;
	private RectF bottomBound;
	private Rect iconBound;
	private Point centerPoint;
	
	private Paint paint;
	private Path path;
	
	private Drawable iconDrawable;
	
	private float minimumHeight;
	private float maximumHeight;
	private float minimumContentHeight;
	private float minimumBottomHeight;
	
	private Handler handler;
	private PullRunnable pullRunnable;
	private CirclingRunnable circlingRunnable;
	private BackRunnable backRunnable;
	
	private OnStateChangeListener onStateChangeListener;
	
	private boolean isInRotateMode;
	private float degree;
	
	public interface OnStateChangeListener{
		public void onCirclingFullyStop();
		public void onPullFullyStop();
	}
	
	private class PullRunnable implements Runnable{
		private int movingHeight;
		
		public PullRunnable(int movingHeight){
			this.movingHeight = movingHeight;
		}
		
		@Override
		public void run(){
			movingHeight += -movingHeight * 0.5F;
			setHeight(movingHeight);
			
			if(movingHeight > 0){
				handler.postDelayed(pullRunnable, 20);
			}else{
				if(null != onStateChangeListener) onStateChangeListener.onPullFullyStop();
			}
		}
	}
	
	private class CirclingRunnable implements Runnable{
		
		public CirclingRunnable(){
			isInRotateMode = true;
			handler.removeCallbacks(pullRunnable);
			handler.removeCallbacks(backRunnable);
			setHeight(0);
		}
		
		@Override
		public void run(){
			if(degree < 360){
				degree += 20;
			}else{
				degree = 0;
			}
			
			invalidate();
			
			handler.postDelayed(circlingRunnable, 20);
		}
	}
	
	private class BackRunnable implements Runnable{
		
		public BackRunnable(){
			isInRotateMode = true;
			handler.removeCallbacks(pullRunnable);
			handler.removeCallbacks(circlingRunnable);
			setHeight(0);
		}
		
		@Override
		public void run(){
			degree += (359 - degree) * 0.5F;
			if(degree < 359){
				handler.postDelayed(backRunnable, 20);
			}else{
				degree = 0;
				isInRotateMode = false;
				if(null != onStateChangeListener) onStateChangeListener.onCirclingFullyStop();
			}
			
			invalidate();
		}
	}

	public PullWidget(Context context){
		this(context, null);
	}
	
	public PullWidget(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public PullWidget(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		
		topBound = new RectF();
		bottomBound = new RectF();
		iconBound = new Rect();
		centerPoint = new Point();
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0xFF999999);
		path = new Path();
		
		iconDrawable = context.getResources().getDrawable(R.drawable.icon_pullwidget);
		
		minimumHeight = context.getResources().getDimensionPixelOffset(R.dimen.minimum_height);
		maximumHeight = minimumHeight * 3;
		minimumContentHeight = context.getResources().getDimensionPixelOffset(R.dimen.minimum_content_height);
		minimumBottomHeight = minimumContentHeight;
		
		isInRotateMode = false;
		degree = 0;
		handler = new Handler();
	}
	
	public void setHeight(int height){
		height += minimumHeight;
		if(height > maximumHeight){
			height = (int) maximumHeight;
		}
		if(height < minimumHeight){
			height = (int) minimumHeight;
		}

		this.height = height;
		LayoutParams param = getLayoutParams();
		param.height = height;
		setLayoutParams(param);
		invalidate();
	}
	
	public boolean isExceedMaximumHeight(){
		return height >= maximumHeight;
	}
	
	public void smoothToOriginalSpot(){
		pullRunnable = new PullRunnable(height);
		handler.post(pullRunnable);
	}
	
	public void circling(){
		circlingRunnable = new CirclingRunnable();
		handler.post(circlingRunnable);
		Log.d(TAG, "circling");
	}
	
	public void stopCirclingAndReturn(){
		backRunnable = new BackRunnable();
		handler.post(backRunnable);
	}
	
	public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener){
		this.onStateChangeListener = onStateChangeListener;
	}
	
	public OnStateChangeListener getOnStateChangeListener(){
		return onStateChangeListener;
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if(heightMeasureSpec < minimumHeight){
			height = (int) minimumHeight;
			setMeasuredDimension(width, height);
		}
		
		if(widthMeasureSpec < minimumHeight){
			width = (int) minimumHeight;
			setMeasuredDimension(width, height);
		}
		
		width = getMeasuredWidth();
		height = getMeasuredHeight();
		
		measureDraw(width, height);
	}
	
	private  void measureDraw(int width, int height){
		centerPoint.x = width / 2;
		centerPoint.y = height / 2;
		
		topBound.left = centerPoint.x - minimumContentHeight / 2;
		topBound.top = (minimumHeight - minimumContentHeight) / 2;
		topBound.right = topBound.left + minimumContentHeight;
		topBound.bottom = topBound.top + minimumContentHeight;
		
		minimumBottomHeight = minimumContentHeight * ((maximumHeight - (height - minimumHeight)) / maximumHeight);
		
		bottomBound.left = centerPoint.x - minimumBottomHeight / 2;
		bottomBound.top = height - minimumBottomHeight - (minimumHeight - minimumBottomHeight) / 2;
		bottomBound.right = bottomBound.left + minimumBottomHeight;
		bottomBound.bottom = bottomBound.top + minimumBottomHeight;
	}
	
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		path.reset();
		path.moveTo(topBound.left, minimumHeight / 2);
		path.arcTo(topBound, 180, 180);
		path.quadTo(bottomBound.right, centerPoint.y, bottomBound.right, height - minimumHeight / 2);
		path.arcTo(bottomBound, 0, 180);
		path.quadTo(bottomBound.left, centerPoint.y, topBound.left, minimumHeight / 2);
		canvas.drawPath(path, paint);
		
		topBound.round(iconBound);
		iconDrawable.setBounds(iconBound);
		if(isInRotateMode){
			canvas.save();
			canvas.rotate(degree, iconDrawable.getBounds().centerX(), iconDrawable.getBounds().centerY());
			iconDrawable.draw(canvas);
			canvas.restore();
		}else{
			iconDrawable.draw(canvas);
		}
	}
}
