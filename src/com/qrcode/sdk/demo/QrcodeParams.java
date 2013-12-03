package com.qrcode.sdk.demo;

import android.graphics.Bitmap;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.qrcode.sdk.demo.QrcodeUtil.BORDER_TYPE;
import com.qrcode.sdk.demo.QrcodeUtil.FINDER_TYPE;
import com.qrcode.sdk.demo.QrcodeUtil.GRADIENT_TYPE;
import com.qrcode.sdk.demo.QrcodeUtil.Shape;

public class QrcodeParams {

	public String contents;
	public int width;
	public int height;
	public int padding;
	public Shape shape;
	public float radiusPercent;
	public ErrorCorrectionLevel level;
	public int foregroundColor;
	public int backgroundColor;
	public Bitmap backgroundBm;
	public int finderColor;
	public int finderBorderColor;
	public FINDER_TYPE finderType;
	public int gradientColor;
	public GRADIENT_TYPE gradientType;
	public BORDER_TYPE borderType;

	public QrcodeParams() {

	}

	public QrcodeParams(String contents, int width, int height, int padding,
			Shape shape, float radiusPercent, ErrorCorrectionLevel level,
			int foregroundColor, int backgroundColor, Bitmap backgroundBm,
			int finderColor, int finderBorderColor, FINDER_TYPE finderType,
			int gradientColor, GRADIENT_TYPE gradientType,
			BORDER_TYPE borderType) {
		this.contents = contents;
		this.width = width;
		this.height = height;
		this.padding = padding;
		this.shape = shape;
		this.radiusPercent = radiusPercent;
		this.level = level;
		this.foregroundColor = foregroundColor;
		this.backgroundColor = backgroundColor;
		this.backgroundBm = backgroundBm;
		this.finderColor = finderColor;
		this.finderBorderColor = finderBorderColor;
		this.finderType = finderType;
		this.gradientColor = gradientColor;
		this.gradientType = gradientType;
		this.borderType = borderType;
	}

}
