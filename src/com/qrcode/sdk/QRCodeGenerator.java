package com.qrcode.sdk;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.text.TextUtils;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import com.qrcode.sdk.QRCodeOptions.ComposeType;
import com.qrcode.sdk.QRCodeOptions.GradientType;
import com.qrcode.sdk.QRCodeOptions.Shape;

public class QRCodeGenerator {
	private String mContent;

	/**
	 * Cache qrcode alternative to 'mContent'. use {@link #reset()} to clear.
	 */
	private QRCode mQRCode;

	public QRCodeGenerator(String content) {
		mContent = content;
	}

	public boolean reset() {
		mQRCode = null;
		return mQRCode == null;
	}

	private boolean isSet(ByteMatrix matrix, int row, int column) {
		// if ((matrix == null) || (row < 0 || row > matrix.getWidth() - 1)
		// || (column < 0 || column > matrix.getHeight() - 1)) {
		// return false;
		// }
		//
		// return matrix.get(row, column) == 1;

		if (matrix == null) {
			return false;
		}

		if (row == -1 || row == matrix.getWidth() || column == -1
				|| column == matrix.getHeight()) {
			return false;
		}

		int x, y = 0;
		if (row < 0 || row > matrix.getWidth() - 1 || column < 0
				|| column > matrix.getHeight() - 1) {
			x = (row + matrix.getWidth()) % matrix.getWidth();
			y = (column + matrix.getHeight()) % matrix.getHeight();
		} else {
			x = row % matrix.getWidth();
			y = column % matrix.getHeight();
		}

		return matrix.get(x, y) == 1;
	}

	private boolean isFinderPatterns(ByteMatrix matrix, int row, int col) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();

		if (row >= 0 && row <= 6 && col >= 0 && col <= 6) {
			return true;
		}
		if (row >= 0 && row <= 6 && col >= width - 7 && col <= width - 1) {
			return true;
		}
		if (col >= 0 && col <= 6 && row >= height - 7 && row <= height - 1) {
			return true;
		}
		return false;
	}

	private boolean isFinderPoint(ByteMatrix matrix, int row, int col) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();

		if (row >= 2 && row <= 4 && col >= 2 && col <= 4) {
			return true;
		}
		if (row >= 2 && row <= 4 && col >= height - 5 && col <= height - 3) {
			return true;
		}
		if (col >= 2 && col <= 4 && row >= width - 5 && row <= width - 3) {
			return true;
		}
		return false;
	}

	private void drawRoundRect(Canvas canvas, RectF rect, Paint paint,
			int radius, boolean leftTop, boolean rightTop, boolean leftBottom,
			boolean rightBottom) {
		float roundRadius[] = new float[8];
		roundRadius[0] = leftTop ? 0 : radius;
		roundRadius[1] = leftTop ? 0 : radius;
		roundRadius[2] = rightTop ? 0 : radius;
		roundRadius[3] = rightTop ? 0 : radius;
		roundRadius[4] = rightBottom ? 0 : radius;
		roundRadius[5] = rightBottom ? 0 : radius;
		roundRadius[6] = leftBottom ? 0 : radius;
		roundRadius[7] = leftBottom ? 0 : radius;

		Path path = new Path();
		path.addRoundRect(rect, roundRadius, Direction.CCW);
		canvas.drawPath(path, paint);
	}

	private void drawAntiRoundRect(Canvas canvas, Paint paint, int radius,
			RectF rect, int direction) {
		if (direction == 1) {
			Path path = new Path();
			path.moveTo(rect.left, rect.top);
			path.lineTo(rect.left, rect.top + radius);
			path.addArc(new RectF(rect.left, rect.top, rect.left + radius * 2,
					rect.top + radius * 2), 180, 90);
			path.lineTo(rect.left, rect.top);
			path.close();
			canvas.drawPath(path, paint);
		} else if (direction == 2) {
			Path path = new Path();
			path.moveTo(rect.right, rect.top);
			path.lineTo(rect.right - radius, rect.top);
			path.addArc(new RectF(rect.right - 2 * radius, rect.top,
					rect.right, rect.top + radius * 2), 270, 90);
			path.lineTo(rect.right, rect.top);
			path.close();
			canvas.drawPath(path, paint);
		} else if (direction == 3) {
			Path path = new Path();
			path.moveTo(rect.right, rect.bottom);
			path.lineTo(rect.right, rect.bottom - radius);
			path.addArc(new RectF(rect.right - 2 * radius, rect.bottom - 2
					* radius, rect.right, rect.bottom), 0, 90);
			path.lineTo(rect.right, rect.bottom);
			path.close();
			canvas.drawPath(path, paint);
		} else if (direction == 4) {
			Path path = new Path();
			path.moveTo(rect.left, rect.bottom);
			path.lineTo(rect.left + radius, rect.bottom);
			path.addArc(new RectF(rect.left, rect.bottom - 2 * radius,
					rect.left + 2 * radius, rect.bottom), 90, 90);
			path.lineTo(rect.left, rect.bottom);
			path.close();
			canvas.drawPath(path, paint);
		}
	}

	private int getGradientColor(int startColor, int endColor, float ratio) {
		if (ratio <= 0.000001) {
			return startColor;
		}
		if (ratio >= 1.0) {
			return endColor;
		}

		int a1 = Color.alpha(startColor);
		int r1 = Color.red(startColor);
		int g1 = Color.green(startColor);
		int b1 = Color.blue(startColor);

		int a2 = Color.alpha(endColor);
		int r2 = Color.red(endColor);
		int g2 = Color.green(endColor);
		int b2 = Color.blue(endColor);

		int a3 = (int) (a1 + (a2 - a1) * ratio);
		int r3 = (int) (r1 + (r2 - r1) * ratio);
		int g3 = (int) (g1 + (g2 - g1) * ratio);
		int b3 = (int) (b1 + (b2 - b1) * ratio);

		return Color.argb(a3, r3, g3, b3);
	}

	private Bitmap composeBitmap(Bitmap topBitmap, Bitmap bottomBitmap,
			ComposeType composeType, int leftPadding, int topPadding,
			int backgroundColor) {
		if (composeType == null || bottomBitmap == null || topBitmap == null) {
			return topBitmap;
		}

		int width = topBitmap.getWidth();
		int height = topBitmap.getHeight();
		int scaledWidth = width - (leftPadding << 1);
		int scaledHeight = height - (topPadding << 1);
		Bitmap scaledBottomBitmap = Bitmap.createScaledBitmap(bottomBitmap,
				scaledWidth, scaledHeight, false);

		float blackCoefficient = 0.8f; // 将背景图色值变暗，以增加识别率
		float composeCoefficient = 0.3f;
		int topColor, bottomColor;
		int ta, tr, tg, tb, ba, br, bg, bb;

		switch (composeType) {
		case SIMPLE:
			for (int i = 0; i < height; i++) {
				if (i < topPadding || i > height - 1 - topPadding) {
					continue;
				}
				for (int j = 0; j < width; j++) {
					if (j < leftPadding || j > width - 1 - leftPadding) {
						continue;
					}

					topColor = topBitmap.getPixel(j, i);
					bottomColor = scaledBottomBitmap.getPixel(j - leftPadding,
							i - topPadding);

					ta = Color.alpha(topColor);
					tr = Color.red(topColor);
					tg = Color.green(topColor);
					tb = Color.blue(topColor);
					ba = Color.alpha(bottomColor);
					br = Color.red(bottomColor);
					bg = Color.green(bottomColor);
					bb = Color.blue(bottomColor);

					if (topColor == backgroundColor) {
						ta = (int) (ta * (1 - composeCoefficient) + ba
								* composeCoefficient);
						tr = (int) (tr * (1 - composeCoefficient) + br
								* composeCoefficient);
						tg = (int) (tg * (1 - composeCoefficient) + bg
								* composeCoefficient);
						tb = (int) (tb * (1 - composeCoefficient) + bb
								* composeCoefficient);
					} else {
						// Black Point
						ta = (int) (ba * blackCoefficient);
						tr = (int) (br * blackCoefficient);
						tg = (int) (bg * blackCoefficient);
						tb = (int) (bb * blackCoefficient);
					}

					topBitmap.setPixel(j, i, Color.argb(ta, tr, tg, tb));
				}
			}
			break;
		case ALTERNATIVE:
			for (int i = 0; i < height; i++) {
				if (i < topPadding || i > height - 1 - topPadding) {
					continue;
				}
				for (int j = 0; j < width; j++) {
					if (j < leftPadding || j > width - 1 - leftPadding) {
						continue;
					}

					topColor = topBitmap.getPixel(j, i);
					bottomColor = scaledBottomBitmap.getPixel(j - leftPadding,
							i - topPadding);

					if (Color.alpha(topColor) == 0) {
						topBitmap.setPixel(j, i, bottomColor);
					} else {
						topBitmap.setPixel(j, i, topColor);
					}
				}
			}
			break;
		default:
			break;
		}

		scaledBottomBitmap.recycle();
		scaledBottomBitmap = null;

		return topBitmap;
	}

	public Bitmap generate(QRCodeOptions options) throws WriterException {
		if (options == null) {
			options = new QRCodeOptions();
		}

		if (mQRCode == null) {
			if (TextUtils.isEmpty(mContent)) {
				throw new WriterException("Content is empty.");
			}

			mQRCode = Encoder.encode(mContent, options.outErrorCorrectionLevel);
			if (mQRCode == null) {
				return null;
			}
		}

		int width = options.outWidth;
		int height = options.outHeight;

		ByteMatrix input = mQRCode.getMatrix();
		if (input == null) {
			throw new IllegalStateException();
		}
		int inputWidth = input.getWidth();
		int inputHeight = input.getHeight();
		int qrWidth = options.outBorderType != null ? inputWidth : inputWidth
				+ (options.outPadding << 1);
		int qrHeight = options.outBorderType != null ? inputHeight
				: inputHeight + (options.outPadding << 1);

		int outputWidth = Math.max(width, qrWidth);
		int outputHeight = Math.max(height, qrHeight);

		float multiple = Math.min(outputWidth / (float) qrWidth, outputHeight
				/ (float) qrHeight);
		float leftPadding = (outputWidth - inputWidth * multiple) / 2;
		float topPadding = (outputHeight - inputHeight * multiple) / 2;

		Bitmap bitmap = Bitmap.createBitmap(outputWidth, outputHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL);

		if (options.outComposeType != ComposeType.ALTERNATIVE) {
			canvas.drawColor(options.outBackgroundColor);
		}

		QRBorder border = null;
		RectF insideRect = null;

		if (options.outBorderType != null) {
			switch (options.outBorderType) {
			case ROUND:
				border = new QRCircleBorder(outputWidth, outputHeight);
				break;
			case RHOMBUS:
				border = new QRRhombusBorder(outputWidth, outputHeight);
				break;
			default:
				break;
			}
		}

		if (border != null) {
			canvas.clipPath(border.getClipPath());
			insideRect = border.getInsideArea();
			multiple = Math.min((insideRect.right - insideRect.left)
					/ inputWidth, (insideRect.bottom - insideRect.top)
					/ inputHeight);
			leftPadding = 0;
			topPadding = 0;
		} else {
			insideRect = new RectF(leftPadding, topPadding, outputWidth
					- leftPadding, outputHeight - topPadding);
		}

		int roundRectRadius = (int) (multiple * options.outRadiuspercent);
		if (options.outShape == Shape.WATER) {
			roundRectRadius = (int) (roundRectRadius * 0.7f); // 避免液态太过出现毛刺
		}

		int finderPatternColor = options.outFinderPatternColor;
		if (finderPatternColor == -1) {
			finderPatternColor = options.outForegroundColor;
		}
		int foregroundColor = options.outForegroundColor;
		int gradientColor = options.outGradientColor;

		int inputX, inputY;
		for (float outputY = topPadding; outputY < outputHeight - topPadding; outputY += multiple) {
			inputY = Math.round((outputY - insideRect.top) / multiple);
			for (float outputX = leftPadding; outputX < outputWidth
					- leftPadding; outputX += multiple) {
				inputX = Math.round((outputX - insideRect.left) / multiple);

				if (options.outBackgroundImage != null
						&& options.outComposeType == ComposeType.ALTERNATIVE) {
					if (isSet(input, inputX, inputY)) {
						int offsetX = 0, offsetY = 0;
						int boxWidth, boxHeight;
						for (int m = 0; m < multiple / 2 + 1; m++) {
							boxHeight = (m == 0 || (multiple % 2 == 0 && m == multiple / 2)) ? 1
									: 2;
							for (int n = 0; n < multiple / 2 + 1; n++) {
								boxWidth = (n == 0 || (multiple % 2 == 0 && n == multiple / 2)) ? 1
										: 2;

								if (m % 2 == n % 2) {
									paint.setColor(Color.argb(0xFF, 0, 0, 0));
								} else {
									paint.setColor(Color.argb(0, 0, 0, 0));
								}
								canvas.drawRect(new RectF(outputX + offsetX,
										outputY + offsetY, outputX + offsetX
												+ boxWidth, outputY + offsetY
												+ boxHeight), paint);

								offsetX += boxWidth;
							}
							offsetX = 0;
							offsetY += boxHeight;
						}
					} else {
						int offsetX = 0, offsetY = 0;
						int boxWidth, boxHeight;
						for (int m = 0; m < multiple / 2 + 1; m++) {
							boxHeight = (m == 0 || (multiple % 2 == 0 && m == multiple / 2)) ? 1
									: 2;
							for (int n = 0; n < multiple / 2 + 1; n++) {
								boxWidth = (n == 0 || (multiple % 2 == 0 && n == multiple / 2)) ? 1
										: 2;

								if (m % 2 == n % 2) {
									paint.setColor(Color.argb(0xFF, 0xFF, 0xFF,
											0xFF));
								} else {
									paint.setColor(Color.argb(0, 0, 0, 0));
								}
								canvas.drawRect(new RectF(outputX + offsetX,
										outputY + offsetY, outputX + offsetX
												+ boxWidth, outputY + offsetY
												+ boxHeight), paint);

								offsetX += boxWidth;
							}
							offsetX = 0;
							offsetY += boxHeight;
						}
					}
				} else {
					if (isFinderPatterns(input, inputX, inputY)) {
						paint.setColor(finderPatternColor);
						if (isFinderPoint(input, inputX, inputY)) {
							if (options.outFinderPointColor == QRCodeOptions.COLOR_UNSET) {
								paint.setColor(finderPatternColor);
							} else {
								paint.setColor(options.outFinderPointColor);
							}
						} else {
							if (options.outFinderBorderColor == QRCodeOptions.COLOR_UNSET) {
								paint.setColor(finderPatternColor);
							} else {
								paint.setColor(options.outFinderBorderColor);
							}
						}
					} else if (options.outGradientType == GradientType.NORMAL
							|| gradientColor == foregroundColor) {
						paint.setColor(foregroundColor);
					} else {
						float ratio;
						switch (options.outGradientType) {
						case HORIZONTAL:
							ratio = inputX / (float) inputWidth;
							break;
						case VERTICAL:
							ratio = inputY / (float) inputHeight;
							break;
						case SLASH:
							ratio = (float) (Math.hypot(inputWidth - inputX,
									inputHeight - inputY) / Math.hypot(
									inputWidth, inputHeight));
							break;
						case BACKSLASH:
							ratio = (float) (Math.hypot(inputX, inputHeight
									- inputY) / Math.hypot(inputWidth,
									inputHeight));
							break;
						case ROUND:
							ratio = (float) (Math.hypot(inputWidth / 2.0
									- inputX, inputHeight / 2.0 - inputY) / (Math
									.min(inputWidth, inputHeight) / 2.0));
							break;
						default:
							ratio = 0.0f;
							break;
						}

						paint.setColor(getGradientColor(gradientColor,
								foregroundColor, ratio));
					}

					Shape shape = options.outShape;
					if (isSet(input, inputX, inputY)) {
						if (shape == Shape.ROUND) {
							// 圆角
							canvas.drawRoundRect(new RectF(outputX, outputY,
									outputX + multiple, outputY + multiple),
									roundRectRadius, roundRectRadius, paint);
						} else if (shape == Shape.WATER) {
							// 液态
							drawRoundRect(
									canvas,
									new RectF(outputX, outputY, outputX
											+ multiple, outputY + multiple),
									paint,
									roundRectRadius,
									isSet(input, inputX - 1, inputY - 1)
											|| isSet(input, inputX, inputY - 1)
											|| isSet(input, inputX - 1, inputY),
									isSet(input, inputX, inputY - 1)
											|| isSet(input, inputX + 1,
													inputY - 1)
											|| isSet(input, inputX + 1, inputY),
									isSet(input, inputX, inputY + 1)
											|| isSet(input, inputX - 1,
													inputY + 1)
											|| isSet(input, inputX - 1, inputY),
									isSet(input, inputX + 1, inputY)
											|| isSet(input, inputX + 1,
													inputY + 1)
											|| isSet(input, inputX, inputY + 1));
						} else {
							// 正常
							canvas.drawRect(outputX, outputY, outputX
									+ multiple, outputY + multiple, paint);
						}
					} else {
						if (shape == Shape.WATER) {
							RectF rect = new RectF(outputX, outputY, outputX
									+ multiple, outputY + multiple);
							if (isSet(input, inputX, inputY - 1)
									&& isSet(input, inputX - 1, inputY)) {
								drawAntiRoundRect(canvas, paint,
										roundRectRadius, rect, 1);
							}

							if (isSet(input, inputX, inputY - 1)
									&& isSet(input, inputX + 1, inputY)) {
								drawAntiRoundRect(canvas, paint,
										roundRectRadius, rect, 2);
							}

							if (isSet(input, inputX, inputY + 1)
									&& isSet(input, inputX + 1, inputY)) {
								drawAntiRoundRect(canvas, paint,
										roundRectRadius, rect, 3);
							}

							if (isSet(input, inputX - 1, inputY)
									&& isSet(input, inputX, inputY + 1)) {
								drawAntiRoundRect(canvas, paint,
										roundRectRadius, rect, 4);
							}
						}
					}
				}

			}
		}

		return composeBitmap(bitmap, options.outBackgroundImage,
				options.outComposeType, (int) leftPadding, (int) topPadding,
				options.outBackgroundColor);
	}
}
