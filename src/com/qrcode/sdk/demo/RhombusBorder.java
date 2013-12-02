package com.qrcode.sdk.demo;

import android.graphics.Path;
import android.graphics.RectF;

public class RhombusBorder extends Border {

	private RectF mInsideRect;

	private float mInsideW = 0;

	public RhombusBorder(int width, int height, int padding) {
		super(width, height, padding);
		mInsideW = (width < height ? width - 2 * padding : height - 2 * padding) / 2.0f;
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
		path.moveTo(mPadding, mHeight / 2.0f);
		path.lineTo(mWidth / 2.0f, mPadding);
		path.lineTo(mWidth - mPadding, mHeight / 2.0f);
		path.lineTo(mWidth / 2.0f, mHeight - mPadding);
		path.lineTo(mPadding, mHeight / 2.0f);
		return path;
	}

}
