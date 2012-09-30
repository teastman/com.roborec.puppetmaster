package com.roborec.puppetmaster;

import com.roborec.puppetmaster.R;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class WalkerView extends View{
	
	private int width = 0;
	private int height = 0;
	
	private final double BUFFER_SPACE = 0.1;
	private double scaleRatio = 0.0;
	
	private Bitmap walker_base;
	private Bitmap walker_top;
	
	private Paint arcPaint;
	
	private CircleControl headControl;
	
	private CircleControl fl_leg;
	private CircleControl fr_leg;

	private CircleControl ml_leg;
	private CircleControl mr_leg;
	
	private CircleControl bl_leg;
	private CircleControl br_leg;
	
	private int base_width;
	private int base_height;
	
	private int head_width;
	private int head_height;
	
	private int body_x_offset;
	private int body_y_offset;
	
	private int head_x_offset;
	private int head_y_offset;
	
	private PuppetMasterActivity walkerActivity;
	
	public WalkerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public WalkerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public WalkerView(Context context) {
		super(context);
		init();
	}
	
	private void init()
	{
		walker_base = BitmapFactory.decodeResource(getResources(), R.drawable.fullbase);
		walker_top = BitmapFactory.decodeResource(getResources(), R.drawable.fulltop);
		
		arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		arcPaint.setColor(0x66FFFFFF);
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0x66DDDDFF);
		Paint activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		activePaint.setColor(0x99EEEEFF);
		
		int radius = 50;
		int activeRadius = 75;
		
		headControl = new CircleControl(this, 1, 2, new PointF(334, 308), new PointF(0, 0), 290, 225, 355, Math.PI/2, Math.PI, 0, radius, activeRadius, paint, activePaint, new Paint(arcPaint));
		
		fl_leg = new CircleControl(this, 1, 2, new PointF(234, 292), new PointF(0, 0), 250, 150, 350, (Math.PI - 0.29*Math.PI), Math.PI, Math.PI/2, radius, activeRadius, paint, activePaint, arcPaint);
		fr_leg = new CircleControl(this, 1, 2, new PointF(438, 292), new PointF(0, 0), 250, 150, 350, (0.29*Math.PI), Math.PI/2, 0, radius, activeRadius, paint, activePaint, arcPaint);

		ml_leg = new CircleControl(this, 1, 2, new PointF(265, 390), new PointF(0, 0), 175, 115, 250, (Math.PI - 0.03*Math.PI), -3*Math.PI/4, 3*Math.PI/4, radius, activeRadius, paint, activePaint, arcPaint);
		mr_leg = new CircleControl(this, 1, 2, new PointF(405, 390), new PointF(0, 0), 175, 115, 250, (0.03*Math.PI), Math.PI/4, -Math.PI/4, radius, activeRadius, paint, activePaint, arcPaint);
		
		bl_leg = new CircleControl(this, 1, 2, new PointF(265, 475), new PointF(0, 0), 210, 150, 300, (-Math.PI + 0.24*Math.PI), -Math.PI/2, -Math.PI, radius, activeRadius, paint, activePaint, arcPaint);
		br_leg = new CircleControl(this, 7, 8, new PointF(405, 475), new PointF(0, 0), 210, 150, 300, (-0.24*Math.PI), 0, -Math.PI/2, radius, activeRadius, paint, activePaint, arcPaint);
	}

	public void sendServoCommand(int servoId, float angle)
	{
		String servoIdString = Integer.toString(servoId);
		if(servoIdString.length() < 2)
			servoIdString = "0" + servoIdString;
		else if(servoIdString.length() > 2)
			servoIdString = servoIdString.substring(0, 2);
		
		String message = "servo:" + servoIdString + ":" + (int)angle + "\r\n";
		
		walkerActivity.sendMessage(message);
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawBitmap(	walker_base, 
							new Rect(0,0,walker_base.getWidth(), walker_base.getHeight()), 
							new Rect(body_x_offset, body_y_offset, base_width + body_x_offset, base_height + body_y_offset),
							null);
		
		Matrix matrix = new Matrix();
		float angle = (float)Math.toDegrees(headControl.getAngle());
		/*matrix.postScale((float)scaleRatio, (float)scaleRatio);
		matrix.postTranslate(head_x_offset, head_y_offset);*/
		matrix.setRectToRect(	new RectF(0f,0f,walker_top.getWidth(), walker_top.getHeight()), 
								new RectF(head_x_offset, head_y_offset, head_x_offset + head_width, head_y_offset + head_height), 
								Matrix.ScaleToFit.CENTER);
		matrix.postRotate(-angle+90, head_x_offset+155f*(float)scaleRatio, head_y_offset+193f*(float)scaleRatio);
		canvas.drawBitmap(walker_top, matrix, new Paint());
		
/*
		canvas.drawBitmap(	walker_top.getImage(), 
							walker_top.getSourceRect(), 
							new Rect(head_x_offset, head_y_offset, head_width + head_x_offset, head_height + head_y_offset), 
							null);
*/
				
		headControl.draw(canvas);
		fl_leg.draw(canvas);
		fr_leg.draw(canvas);
		ml_leg.draw(canvas);
		mr_leg.draw(canvas);
		bl_leg.draw(canvas);
		br_leg.draw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST || MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY)
		{
			width = MeasureSpec.getSize(widthMeasureSpec);
		}
		else
		{
			width = 670;
		}
		
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST || MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY)
		{
			height = MeasureSpec.getSize(heightMeasureSpec);
		}
		else
		{
			height = 683;
		}
		this.setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		if((double)width / (double)height > 670.0 / 683.0)
		{	
			scaleRatio = ((double)height - 2.0 * BUFFER_SPACE * (double)height) / 683.0;
		}
		else
		{
			scaleRatio = ((double)width - 2.0 * BUFFER_SPACE * (double)width) / 670.0;
		}
		
		base_width = (int)(670.0 * scaleRatio);
		base_height = (int)(683.0 * scaleRatio);

		head_width = (int)(328.0 * scaleRatio);
		head_height = (int)(297.0 * scaleRatio);
		
		body_x_offset = (int)(((double)width - (double)base_width) / 2.0);
		body_y_offset = (int)(((double)height - (double)base_height) / 2.0);
	
		head_x_offset = (int)((double)body_x_offset + (335.0 * scaleRatio) - (155.0 * scaleRatio));
		head_y_offset = (int)((double)body_y_offset + (310.0 * scaleRatio) - (193.0 * scaleRatio));
		
		headControl.setScale((float)scaleRatio);
		fl_leg.setScale((float)scaleRatio);
		fr_leg.setScale((float)scaleRatio);
		ml_leg.setScale((float)scaleRatio);
		mr_leg.setScale((float)scaleRatio);
		bl_leg.setScale((float)scaleRatio);
		br_leg.setScale((float)scaleRatio);
		
		PointF offset = new PointF((float)body_x_offset, (float)body_y_offset);
		headControl.setOffset(offset);
		fl_leg.setOffset(offset);
		fr_leg.setOffset(offset);
		ml_leg.setOffset(offset);
		mr_leg.setOffset(offset);
		bl_leg.setOffset(offset);
		br_leg.setOffset(offset);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		headControl.touch(event);
		fl_leg.touch(event);
		fr_leg.touch(event);
		ml_leg.touch(event);
		mr_leg.touch(event);
		bl_leg.touch(event);
		br_leg.touch(event);
		
		invalidate();
		
		return true;//super.onTouchEvent(event);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    //super.onConfigurationChanged(newConfig);
	    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	public void setWalkerActivity(PuppetMasterActivity walkerActivity) {
		this.walkerActivity = walkerActivity;
	}
	
}
