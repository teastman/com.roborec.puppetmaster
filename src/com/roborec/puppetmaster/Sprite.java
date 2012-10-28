package com.roborec.puppetmaster;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Sprite {
	
	private Bitmap image;
	private Rect sourceRect;
	private int currentFrame;
	
	private int horizontalFrameCount;
	private int verticalFrameCount;
	
	private int x;
	private int y;
	
	public Sprite(Bitmap image, int horizontalFrameCount, int verticalFrameCount, int x, int y)
	{
		super();
		this.image = image;
		this.currentFrame = 0;
		this.horizontalFrameCount = horizontalFrameCount;
		this.verticalFrameCount = verticalFrameCount;
		
		int width = (int)(image.getWidth() / horizontalFrameCount);
		int height = (int)(image.getHeight() / verticalFrameCount);
		
		this.sourceRect = new Rect(0, 0, width, height);
		this.x = x;
		this.y = y;
	}
	
	public int getHorizontalFrameCount() {
		return horizontalFrameCount;
	}

	public void setHorizontalFrameCount(int horizontalFrameCount) {
		this.horizontalFrameCount = horizontalFrameCount;
	}

	public int getVerticalFrameCount() {
		return verticalFrameCount;
	}

	public void setVerticalFrameCount(int verticalFrameCount) {
		this.verticalFrameCount = verticalFrameCount;
	}

	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	public Rect getSourceRect() {
		return sourceRect;
	}
	public void setSourceRect(Rect sourceRect) {
		this.sourceRect = sourceRect;
	}
	public int getCurrentFrame() {
		return currentFrame;
	}
	public void setCurrentFrame(int currentFrame) {
		this.currentFrame = currentFrame;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	
}
