package com.qrcode.sdk;

import android.graphics.Path;
import android.graphics.RectF;

public class QRRhombusBorder extends QRBorder {

	private RectF mInsideRect;

	private float mInsideW = 0;

	public QRRhombusBorder(int width, int height) {
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
		path.moveTo(0, mHeight / 2.0f);
		path.lineTo(mWidth / 2.0f, 0);
		path.lineTo(mWidth, mHeight / 2.0f);
		path.lineTo(mWidth / 2.0f, mHeight);
		path.lineTo(0, mHeight / 2.0f);
		return path;
	}

}
