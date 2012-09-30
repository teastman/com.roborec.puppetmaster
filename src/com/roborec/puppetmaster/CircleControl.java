package com.roborec.puppetmaster;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

public class CircleControl {
	
	private PointF offset;
	private PointF position;
	private PointF pivot;
	
	private int yawServoId;
	private int pitchServoId;
	
	private float scale;

	private float defaultDistance;
	private float minDistance;
	private float maxDistance;
	
	private double defaultAngle;
	private double leftAngle;
	private double rightAngle;
	
	private float radius;
	private float activeRadius;
	
	private Paint paint;
	private Paint activePaint;
	private Paint arcPaint;
	
	private WalkerView callbackView;
	
	private boolean active;
	
	private int activePointerId = -1;
	
	public CircleControl(WalkerView callbackView, int yawServoId, int pitchServoId, PointF pivot, PointF offset, float defaultDistance, float minDistance, float maxDistance,
			double defaultAngle, double leftAngle, double rightAngle,
			float radius, float activeRadius, Paint paint, Paint activePaint, Paint arcPaint) {
		super();
		this.callbackView = callbackView;
		this.yawServoId = yawServoId;
		this.pitchServoId = pitchServoId;
		this.pivot = pivot;
		this.offset = offset;
		this.defaultDistance = defaultDistance;
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		this.defaultAngle = defaultAngle;
		this.leftAngle = leftAngle;
		this.rightAngle = rightAngle;
		this.radius = radius;
		this.activeRadius = activeRadius;
		this.paint = paint;
		this.activePaint = activePaint;
		this.arcPaint = arcPaint;
		this.scale = 1;
		arcPaint.setStrokeCap(Cap.BUTT);
		arcPaint.setAntiAlias(true);
		arcPaint.setStyle(Paint.Style.STROKE);
		
		this.active = false;
		this.position = new PointF();
		resetPosition();
	}
	
	private PointF getPointOnLine(double distance, double angle)
	{
		if(angle >= 0)
		{
			if(angle <= (Math.PI/2))
			{
				return new PointF((float)(Math.cos(angle) * distance), (float)(Math.sin(angle) * distance));
			}
			else
			{
				angle = angle - Math.PI/2;
				return new PointF((float)(Math.sin(angle) * distance * -1), (float)(Math.cos(angle) * distance));
			}
		}
		else
		{
			if(angle >= -1*(Math.PI/2))
			{
				return new PointF((float)(Math.cos(angle) * distance), (float)(Math.sin(angle) * distance));
			}
			else
			{
				angle = angle + Math.PI/2;
				return new PointF((float)(Math.sin(angle) * distance), (float)(Math.cos(angle) * distance *-1));
			}
		}
	}
	
	private void resetPosition()
	{
		PointF relPos = getPointOnLine(defaultDistance*scale, defaultAngle);
		position.set(relPos.x + pivot.x*scale + offset.x, -relPos.y + pivot.y*scale + offset.y);
	}

	public boolean checkCollision(PointF point)
	{
		float xdiff = point.x - position.x;
		float ydiff = point.y - position.y;
		if(xdiff*xdiff+ydiff*ydiff<radius*scale*radius*scale)
			return true;
		return false;
	}
	
	
	public double rotate(double angle, double amount)
	{
		angle += amount;
		angle = angle % (2 * Math.PI);
		if(angle > Math.PI)
			angle = -2 * Math.PI + angle;
		else if(angle < -Math.PI)
			angle = 2 * Math.PI + angle;
		
		return angle;
	}
	
	// Returns true if the destination was a valid position,
	// Otherwise moves closest it can and returns false;
	public boolean move(PointF destination)
	{
		PointF relativeDestination = new PointF(destination.x - (pivot.x*scale + offset.x),  -(destination.y - (pivot.y*scale + offset.y)));
		
		float relativeMagnitude = (float)Math.sqrt((double)(relativeDestination.x*relativeDestination.x+relativeDestination.y*relativeDestination.y));
		double relativeAngle = 0.0;
		
		relativeAngle = Math.atan2(relativeDestination.y, relativeDestination.x);

		if(relativeMagnitude > maxDistance*scale)
			relativeMagnitude = maxDistance*scale;
		else if(relativeMagnitude < minDistance*scale)
			relativeMagnitude = minDistance*scale;
		
		if(relativeAngle < leftAngle && relativeAngle > rightAngle)
		{
		}
		else if(leftAngle < 0 && rightAngle > 0 && (relativeAngle < leftAngle || relativeAngle > rightAngle))
		{
			
		}
		else
		{
			// Outside
			/*double tempLeft = leftAngle;
			double tempRight = rightAngle;
			
			if(tempLeft < 0)
				tempLeft = 2 * Math.PI + tempLeft;
				//tempLeft = Math.abs(tempLeft) + Math.PI;
			if(tempRight < 0)
				tempRight = 2 * Math.PI + tempRight;
				//tempRight = Math.abs(tempRight) + Math.PI;
			if(tempRight > tempLeft)
				tempLeft += (2*Math.PI);
			
			double midAngle = ((tempLeft - tempRight) / 2 + tempRight) % (Math.PI * 2);
			
			if(midAngle > Math.PI)
				midAngle = -2 * Math.PI + midAngle;
			double testAngle = rotate(relativeAngle, -midAngle);*/
			
			double testAngle = findZeroCenteredAngle(relativeAngle);
			
			if(testAngle > Math.PI/2 || testAngle < -Math.PI/2)
				relativeMagnitude = minDistance*scale;
				
			if(testAngle > 0)
				relativeAngle = leftAngle;
			else
				relativeAngle = rightAngle;
			
		}

		relativeDestination = getPointOnLine(relativeMagnitude, relativeAngle);
		
		float newX = relativeDestination.x + (pivot.x*scale + offset.x);
		float newY = -relativeDestination.y + (pivot.y*scale + offset.y);
		
		if((int)position.y != (int)newY || (int)position.x != (int)newX)
		{
			position.set(newX, newY);

			double testAngle = findZeroCenteredAngle(relativeAngle);
			
			callbackView.sendServoCommand(yawServoId, (float)Math.toDegrees(rotate(testAngle, Math.PI/2)));
			
			float normalizedDistance = 0;
			if(relativeMagnitude > defaultDistance*scale)
			{
				normalizedDistance = 90 / (maxDistance*scale - defaultDistance*scale) * (relativeMagnitude - defaultDistance*scale) + 90;
			}
			else
			{
				normalizedDistance = 90 / (defaultDistance*scale - minDistance*scale) * (relativeMagnitude- minDistance*scale);
			}
			
			callbackView.sendServoCommand(pitchServoId, normalizedDistance);
		}
		
		return true;
	}
	
	public double findZeroCenteredAngle(double relativeAngle)
	{
		double tempLeft = leftAngle;
		double tempRight = rightAngle;
		
		if(tempLeft < 0)
			tempLeft = 2 * Math.PI + tempLeft;
			//tempLeft = Math.abs(tempLeft) + Math.PI;
		if(tempRight < 0)
			tempRight = 2 * Math.PI + tempRight;
			//tempRight = Math.abs(tempRight) + Math.PI;
		if(tempRight > tempLeft)
			tempLeft += (2*Math.PI);
		
		double midAngle = ((tempLeft - tempRight) / 2 + tempRight) % (Math.PI * 2);
		
		if(midAngle > Math.PI)
			midAngle = -2 * Math.PI + midAngle;
		
		double testAngle = rotate(relativeAngle, -midAngle);
		
		return testAngle;
	}
	
	public void touch(MotionEvent event)
	{
	    final int action = event.getAction();
	    
	    switch (action & MotionEvent.ACTION_MASK) {
		    case MotionEvent.ACTION_DOWN: {
		    	if(checkCollision(new PointF(event.getX(), event.getY())))
				{
					activePointerId = event.getPointerId(0);
					active = true;
				}
				break;
		    }
		    
		    case MotionEvent.ACTION_POINTER_DOWN: {
				int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				int pointerId = event.getPointerId(pointerIndex);
				if(checkCollision(new PointF(event.getX(pointerIndex), event.getY(pointerIndex))))
				{
					activePointerId = pointerId;
					active = true;
				}
				break;
		    }
		    
		    case MotionEvent.ACTION_UP : {
				activePointerId = -1;
				active = false;
		    	break;
		    }
	
		    case MotionEvent.ACTION_POINTER_UP : {
		        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		        int pointerId = event.getPointerId(pointerIndex);
		        if (pointerId == activePointerId) {
		        	active = false;
					activePointerId = -1;
		        }
		    	break;
		    }
	
		    case MotionEvent.ACTION_MOVE : {
		    	if(active)
		    	{
		            int pointerIndex = event.findPointerIndex(activePointerId);
		            move(new PointF(event.getX(pointerIndex), event.getY(pointerIndex)));
		    	}
		    	break;
		    }
	    }
	}
	
	private double angleDiff(double left, double right)
	{
		if(left < 0)
			left = 2 * Math.PI + left;
		if(right < 0)
			right = 2 * Math.PI + right;
		if(right > left)
			left += (2*Math.PI);
		
		return (left - right);
	}
	
	public void draw(Canvas canvas) {
		arcPaint.setStrokeWidth(maxDistance*scale - minDistance*scale);
		float strokeWidthAdjustment = (maxDistance*scale - minDistance*scale) / 2 + minDistance*scale;
		if(active)
		canvas.drawArc(new RectF(pivot.x*scale + offset.x - strokeWidthAdjustment, pivot.y*scale + offset.y - strokeWidthAdjustment, pivot.x*scale + offset.x + strokeWidthAdjustment, pivot.y*scale + offset.y + strokeWidthAdjustment), -(float)Math.toDegrees(leftAngle), (float)Math.toDegrees(angleDiff(leftAngle, rightAngle)), false, arcPaint);
		canvas.drawCircle((float)position.x, (float)position.y, active?activeRadius*scale:radius*scale, active?activePaint:paint);
		canvas.drawLine(pivot.x*scale + offset.x, pivot.y*scale + offset.y, position.x, position.y, paint);
	}
	
	public int getYawServoId() {
		return yawServoId;
	}

	public void setYawServoId(int yawServoId) {
		this.yawServoId = yawServoId;
	}

	public int getPitchServoId() {
		return pitchServoId;
	}

	public void setPitchServoId(int pitchServoId) {
		this.pitchServoId = pitchServoId;
	}

	public PointF getOffset() {
		return offset;
	}

	public void setOffset(PointF offset) {
		if(this.offset.x != offset.x || this.offset.y != offset.y)
		{
			this.offset = offset;
			resetPosition();
		}
	}
	
	public double getAngle()
	{
		return Math.atan2((double)(-(position.y - (pivot.y*scale + offset.y))), (double)(position.x - (pivot.x*scale + offset.x)));
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		if(this.scale != scale)
		{
			this.scale = scale;
			resetPosition();
		}
	}

	public PointF getPosition() {
		return position;
	}

	public void setPosition(PointF position) {
		this.position = position;
	}

	public PointF getPivot() {
		return pivot;
	}

	public void setPivot(PointF pivot) {
		this.pivot = pivot;
		resetPosition();
	}
	
	public float getDefaultDistance() {
		return defaultDistance;
	}

	public void setDefaultDistance(float defaultDistance) {
		this.defaultDistance = defaultDistance;
	}

	public float getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(float minDistance) {
		this.minDistance = minDistance;
	}

	public float getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(float maxDistance) {
		this.maxDistance = maxDistance;
	}

	public double getDefaultAngle() {
		return defaultAngle;
	}

	public void setDefaultAngle(double defaultAngle) {
		this.defaultAngle = defaultAngle;
	}

	public double getLeftAngle() {
		return leftAngle;
	}

	public void setLeftAngle(double leftAngle) {
		this.leftAngle = leftAngle;
	}

	public double getRightAngle() {
		return rightAngle;
	}

	public void setRightAngle(double rightAngle) {
		this.rightAngle = rightAngle;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public float getActiveRadius() {
		return activeRadius;
	}

	public void setActiveRadius(float activeRadius) {
		this.activeRadius = activeRadius;
	}

	public Paint getPaint() {
		return paint;
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}
	
	public Paint getArcPaint() {
		return arcPaint;
	}

	public void setArcPaint(Paint arcPaint) {
		this.arcPaint = arcPaint;
	}

	public Paint getActivePaint() {
		return activePaint;
	}

	public void setActivePaint(Paint activePaint) {
		this.activePaint = activePaint;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
}
