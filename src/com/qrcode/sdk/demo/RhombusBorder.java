package com.qrcode.sdk.demo;

import android.graphics.Path;
import android.graphics.RectF;

public class RhombusBorder extends Border {

	private RectF mInsideRect;

	private float mInsideW = 0;

	public RhombusBorder(int width, int height) {
		super(width, height);
		mInsideW = (width < height ? width : height) / 2.0f;
		mInsideRect = new RectF((mWidth - mInsideW) / 2,
				(mHeight - mInsideW) / 2, (mWidth + mInsideW) / 2,
				(mHeight + mInsideW) / 2);
	}

	@Override
	protected RectF getInsideArea() {
		return mInsideRect;
	}

	@Override
	protected Path getClipPath() {
		Path path = new Path();
		path.moveTo(0, mHeight >> 1);
		path.lineTo(mWidth >> 1, 0);
		path.lineTo(mWidth, mHeight >> 1);
		path.lineTo(mWidth >> 1, mHeight);
		path.lineTo(0, mHeight >> 1);
		return path;
	}

}
