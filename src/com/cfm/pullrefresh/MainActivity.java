package com.cfm.pullrefresh;

import com.cfm.pullrefresh.widget.PullWidget;

import android.os.Bundle;
import android.app.Activity;
import android.view.MotionEvent;

public class MainActivity extends Activity {
	
	private PullWidget pullWidget;
	
	private float dx,dy;
	String string;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pullWidget = (PullWidget) findViewById(R.id.pull_widget);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		
		switch(event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			dx = event.getX();
			dy = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			pullWidget.setHeight((int) (event.getY() - dy));
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			pullWidget.smoothToOriginalSpot();
			break;
		}
		
		return true;
	}
}
