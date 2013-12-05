package com.qrcode.sdk;

import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;

public class QRCircleBorder extends QRBorder {

	private float mDiameter;

	private RectF mInsideRect;

	public QRCircleBorder(int width, int height) {
		super(width, height);
		mDiameter = width < height ? width : height;
		float insideW = (float) (mDiameter * Math.sqrt(2) / 2);
		mInsideRect = new RectF(mWidth / 2.0f - insideW / 2, mHeight / 2.0f
				- insideW / 2, mWidth / 2.0f + insideW / 2, mHeight / 2.0f
				+ insideW / 2);
	}

	@Override
	protected RectF getInsideArea() {
		return mInsideRect;
	}

	@Override
	protected Path getClipPath() {
		Path path = new Path();
		path.addCircle(mWidth / 2.0f, mHeight / 2.0f, mDiameter / 2,
				Direction.CCW);
		return path;
	}

}
