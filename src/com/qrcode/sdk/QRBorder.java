package com.qrcode.sdk;

import android.graphics.Path;
import android.graphics.RectF;

public abstract class QRBorder {

	protected int mWidth = 0;

	protected int mHeight = 0;

	public QRBorder(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public abstract RectF getInsideArea();

	public abstract Path getClipPath();

}
