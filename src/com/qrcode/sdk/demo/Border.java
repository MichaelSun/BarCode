package com.qrcode.sdk.demo;

import android.graphics.Path;
import android.graphics.RectF;

public abstract class Border {

	protected int mWidth = 0;

	protected int mHeight = 0;
	
	protected int mPadding = 0;

	public Border(int width, int height, int padding) {
		mWidth = width;
		mHeight = height;
		mPadding = padding;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	protected abstract RectF getInsideArea();

	protected abstract Path getClipPath();

}
