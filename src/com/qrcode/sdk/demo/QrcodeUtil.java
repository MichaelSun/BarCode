package com.qrcode.sdk.demo;

import java.util.HashMap;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import com.qrcode.sdk.QRBorder;
import com.qrcode.sdk.QRCircleBorder;
import com.qrcode.sdk.QRRhombusBorder;

public class QrcodeUtil {

	public static enum Shape {
		NORMAL, ROUND, WATER
	}

	public static enum GRADIENT_TYPE {
		ROUND, SLASH, BACKSLASH, HORIZONTAL, VERTICAL
	}

	public static enum BORDER_TYPE {
		NONE, CIRCLE, RHOMBUS
	}

	public static enum FINDER_TYPE {
		NONE, RIGHT_ANGLE, ROUND_CORNER, SUYA
	}

	private static final int QUIET_ZONE_SIZE = 10;

	public static Bitmap encode(String contents, int width, int height,
			int padding, Shape shape, float radiusPercent,
			ErrorCorrectionLevel level, int foregroundColor,
			int backgroundColor, Bitmap backgroundBm, int finderColor,
			int finderBorderColor, FINDER_TYPE finderType, int gradientColor,
			GRADIENT_TYPE gradientType, BORDER_TYPE borderType)
			throws WriterException {
		if (TextUtils.isEmpty(contents)) {
			throw new IllegalArgumentException("Found empty contents");
		}

		if (width < 0 || height < 0) {
			throw new IllegalArgumentException(
					"Requested dimensions are too small: " + width + 'x'
							+ height);
		}

		Hashtable<EncodeHintType, Object> table = new Hashtable<EncodeHintType, Object>();
		// table.put(EncodeHintType.CHARACTER_SET, "UTF-8");

		QRCode code = Encoder.encode(contents, level, table);
		Bitmap bitmap = renderResult(code, width, height,
				padding < 0 ? QUIET_ZONE_SIZE : padding, shape, radiusPercent,
				foregroundColor, backgroundColor, backgroundBm, finderColor,
				finderBorderColor, finderType, gradientColor, gradientType,
				borderType);
		// boolean canDecode = DecodeUtils.checkDecode(bitmap, contents);
		return bitmap;
	}

	private static Bitmap renderResult(QRCode code, int width, int height,
			int quietZone, Shape shape, float radiusPercent,
			int foregroundColor, int backgroundColor, Bitmap backgroundBm,
			int finderColor, int findBorderColor, FINDER_TYPE finderType,
			int gradientColor, GRADIENT_TYPE gradientType,
			BORDER_TYPE borderType) {
		ByteMatrix input = code.getMatrix();
		if (input == null) {
			throw new IllegalStateException();
		}
		int inputWidth = input.getWidth();
		int inputHeight = input.getHeight();
		int outputWidth = Math.max(width, inputWidth);
		int outputHeight = Math.max(height, inputHeight);

		Bitmap bitmap = Bitmap.createBitmap(outputWidth, outputHeight,
				Config.ARGB_8888);
		bitmap.eraseColor(255);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL);

		int padding = quietZone;

		QRBorder border = null;
		if (borderType == BORDER_TYPE.RHOMBUS) {
			border = new QRRhombusBorder(outputWidth, outputHeight);
		} else if (borderType == BORDER_TYPE.CIRCLE) {
			border = new QRCircleBorder(outputWidth, outputHeight);
		}

		RectF insideRect = null;
		if (border != null) {
			Path path = border.getClipPath();
			canvas.clipPath(path);
			insideRect = border.getInsideArea();
		} else {
			insideRect = new RectF(padding, padding, outputWidth - padding,
					outputHeight - padding);
		}

		float multiple = Math.min(insideRect.width() / inputWidth,
				insideRect.height() / inputHeight);
		int roundRadius = (int) (multiple * radiusPercent);

		// draw background
		if (backgroundBm != null) {
			Rect src = new Rect(0, 0, backgroundBm.getWidth(),
					backgroundBm.getHeight());
			Rect dst = new Rect(0, 0, outputWidth, outputHeight);
			canvas.drawBitmap(backgroundBm, src, dst, paint);
		} else {
			canvas.drawColor(backgroundColor);
		}

		for (float outputY = padding; outputY < outputHeight - padding; outputY += multiple) {
			for (float outputX = padding; outputX < outputWidth - padding; outputX += multiple) {
				int inputX = Math.round((outputX - insideRect.left) / multiple);
				int inputY = Math.round((outputY - insideRect.top) / multiple);

				// FinderPatterns
				if (inFinderPattern(input, inputX, inputY)) {
					if (isFinderPoint(input, inputX, inputY)) {
						paint.setColor(finderColor);
					} else {
						paint.setColor(findBorderColor);
					}
				} else {
					roundRadius = (int) (radiusPercent * multiple);
					if (gradientColor != foregroundColor) {
						// 渐变色
						float radio = 0f;
						if (gradientType == GRADIENT_TYPE.HORIZONTAL) {
							radio = outputX * 1.0f / outputWidth;
						} else if (gradientType == GRADIENT_TYPE.VERTICAL) {
							radio = outputY * 1.0f / outputHeight;
						} else if (gradientType == GRADIENT_TYPE.SLASH) {
							radio = (float) (Math.hypot(outputX - outputWidth,
									outputHeight - outputY) / Math.hypot(
									outputWidth, outputHeight));
						} else if (gradientType == GRADIENT_TYPE.BACKSLASH) {
							radio = (float) (Math.hypot(outputX, outputHeight
									- outputX) / Math.hypot(outputWidth,
									outputHeight));
						} else {
							radio = (float) (Math.hypot(outputWidth / 2.0
									- outputX, outputHeight / 2.0 - outputY) / (Math
									.min(outputWidth, outputHeight) / 2.0));
						}
						int color = getGradientColor(gradientColor,
								foregroundColor, radio);
						paint.setColor(color);
					} else {
						paint.setColor(foregroundColor);
					}
				}

				if (isSet(input, inputX, inputY)) {
					if (shape == Shape.ROUND) {
						// 圆角
						canvas.drawRoundRect(new RectF(outputX, outputY,
								outputX + multiple, outputY + multiple),
								roundRadius, roundRadius, paint);
					} else if (shape == Shape.WATER) {
						// 液态
						drawRoundRect(
								canvas,
								new RectF(outputX, outputY, outputX + multiple,
										outputY + multiple),
								paint,
								roundRadius,
								isSet(input, inputX - 1, inputY - 1)
										|| isSet(input, inputX, inputY - 1)
										|| isSet(input, inputX - 1, inputY),
								isSet(input, inputX, inputY - 1)
										|| isSet(input, inputX + 1, inputY - 1)
										|| isSet(input, inputX + 1, inputY),
								isSet(input, inputX, inputY + 1)
										|| isSet(input, inputX - 1, inputY + 1)
										|| isSet(input, inputX - 1, inputY),
								isSet(input, inputX + 1, inputY)
										|| isSet(input, inputX + 1, inputY + 1)
										|| isSet(input, inputX, inputY + 1));
					} else {
						// 正常
						canvas.drawRect(outputX, outputY, outputX + multiple,
								outputY + multiple, paint);
					}
				} else {
					if (shape == Shape.WATER) {
						RectF rect = new RectF(outputX, outputY, outputX
								+ multiple, outputY + multiple);
						if (isSet(input, inputX, inputY - 1)
								&& isSet(input, inputX - 1, inputY)) {
							drawAntiRoundRect(canvas, paint, roundRadius, rect,
									1);
						}

						if (isSet(input, inputX, inputY - 1)
								&& isSet(input, inputX + 1, inputY)) {
							drawAntiRoundRect(canvas, paint, roundRadius, rect,
									2);
						}

						if (isSet(input, inputX, inputY + 1)
								&& isSet(input, inputX + 1, inputY)) {
							drawAntiRoundRect(canvas, paint, roundRadius, rect,
									3);
						}

						if (isSet(input, inputX - 1, inputY)
								&& isSet(input, inputX, inputY + 1)) {
							drawAntiRoundRect(canvas, paint, roundRadius, rect,
									4);
						}

					}
				}
			}
		}

		return bitmap;
	}

	public static Bitmap encodeAngryBird(String contents, int width,
			int height, int padding, ErrorCorrectionLevel level, Bitmap bar1,
			Bitmap hbar2, Bitmap vbar2, Bitmap bird, Bitmap finder,
			Bitmap background) throws WriterException {
		if (TextUtils.isEmpty(contents)) {
			throw new IllegalArgumentException("Found empty contents");
		}

		if (width < 0 || height < 0) {
			throw new IllegalArgumentException(
					"Requested dimensions are too small: " + width + 'x'
							+ height);
		}

		Hashtable<EncodeHintType, Object> table = new Hashtable<EncodeHintType, Object>();
		// table.put(EncodeHintType.CHARACTER_SET, "UTF-8");

		QRCode code = Encoder.encode(contents, level, table);
		ByteMatrix input = code.getMatrix();
		if (input == null) {
			throw new IllegalStateException();
		}

		int inputWidth = input.getWidth();
		int inputHeight = input.getHeight();
		int outputWidth = Math.max(width, inputWidth);
		int outputHeight = Math.max(height, inputHeight);

		Bitmap bitmap = Bitmap.createBitmap(outputWidth, outputHeight,
				Config.ARGB_8888);
		bitmap.eraseColor(255);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL);

		float multiple = Math.min((float) (outputWidth - 2 * padding)
				/ inputWidth, (float) (outputHeight - 2 * padding)
				/ inputHeight);

		// draw background
		if (background != null) {
			Rect src = new Rect(0, 0, background.getWidth(),
					background.getHeight());
			Rect dst = new Rect(0, 0, outputWidth, outputHeight);
			canvas.drawBitmap(background, src, dst, paint);
		} else {
			canvas.drawColor(Color.WHITE);
		}

		// draw finder
		if (finder != null) {
			Rect src = new Rect(0, 0, finder.getWidth(), finder.getHeight());
			RectF dst = new RectF(padding, padding, padding + 7 * multiple,
					padding + 7 * multiple);
			canvas.drawBitmap(finder, src, dst, paint);
			dst.set(padding + multiple * (inputWidth - 7), padding, padding
					+ multiple * inputWidth, padding + multiple * 7);
			canvas.drawBitmap(finder, src, dst, paint);
			dst.set(padding, padding + multiple * (inputHeight - 7), padding
					+ multiple * 7, padding + multiple * inputHeight);
			canvas.drawBitmap(finder, src, dst, paint);
		}

		// draw bar
		boolean[][] drawn = new boolean[inputWidth][inputHeight];
		Rect src = new Rect();
		RectF dst = new RectF();
		for (int inputY = 0; inputY < inputHeight; inputY++) {
			for (int inputX = 0; inputX < inputWidth; inputX++) {
				float outputX = padding + inputX * multiple;
				float outputY = padding + inputY * multiple;

				if (inFinderPattern(input, inputX, inputY)) {
					continue;
				} else {
					if (isSet(input, inputX, inputY)) {
						if (drawn[inputX][inputY]) {
							continue;
						} else {
							if (inputX + 1 < inputWidth
									&& inputY + 1 < inputHeight
									&& isSet(input, inputX + 1, inputY)
									&& isSet(input, inputX, inputY + 1)
									&& isSet(input, inputX + 1, inputY + 1)
									&& !drawn[inputX + 1][inputY]) {
								src.set(0, 0, bird.getWidth(), bird.getHeight());
								dst.set(outputX, outputY, outputX + 2
										* multiple, outputY + 2 * multiple);
								canvas.drawBitmap(bird, src, dst, paint);
								drawn[inputX][inputY] = true;
								drawn[inputX + 1][inputY] = true;
								drawn[inputX][inputY + 1] = true;
								drawn[inputX + 1][inputY + 1] = true;
							} else if (inputX + 1 < inputWidth
									&& isSet(input, inputX + 1, inputY)
									&& !drawn[inputX + 1][inputY]) {
								drawn[inputX + 1][inputY] = true;
								src.set(0, 0, hbar2.getWidth(),
										hbar2.getHeight());
								dst.set(outputX, outputY, outputX + 2
										* multiple, outputY + multiple);
								canvas.drawBitmap(hbar2, src, dst, paint);
							} else if (inputY + 1 < inputHeight
									&& isSet(input, inputX, inputY + 1)
									&& !drawn[inputX][inputY + 1]) {
								drawn[inputX][inputY + 1] = true;
								src.set(0, 0, vbar2.getWidth(),
										vbar2.getHeight());
								dst.set(outputX, outputY, outputX + multiple,
										outputY + 2 * multiple);
								canvas.drawBitmap(vbar2, src, dst, paint);
							} else {
								src.set(0, 0, bar1.getWidth(), bar1.getHeight());
								dst.set(outputX, outputY, outputX + multiple,
										outputY + multiple);
								canvas.drawBitmap(bar1, src, dst, paint);
							}
							drawn[inputX][inputY] = true;
						}
					}
				}
			}
		}

		return bitmap;
	}

	private static void drawRoundRect(Canvas canvas, RectF rect, Paint paint,
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

	private static void drawAntiRoundRect(Canvas canvas, Paint paint,
			int radius, RectF rect, int direction) {
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

	// private static boolean isSet(ByteMatrix matrix, int row, int column) {
	// if (matrix == null) {
	// return false;
	// }
	//
	// if (row < 0 || row > matrix.getWidth() - 1) {
	// return false;
	// }
	//
	// if (column < 0 || column > matrix.getHeight() - 1) {
	// return false;
	// }
	//
	// return matrix.get(row, column) == 1;
	// }

	private static boolean isSet(ByteMatrix matrix, int row, int column) {
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

	public static ParsedResult decode(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int pixels[] = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(),
				bitmap.getHeight(), pixels);
		BinaryBitmap binaryBitmap = new BinaryBitmap(
				new HybridBinarizer(source));
		try {
			HashMap<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
			hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
			Result result = new QRCodeReader().decode(binaryBitmap, hints);
			ParsedResult parsedResult = ResultParser.parseResult(result);
			return parsedResult;
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (ChecksumException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static int getGradientColor(int startColor, int endColor,
			float radio) {
		if (radio <= 0.000001)
			return startColor;
		if (radio >= 1.0)
			return endColor;

		int a1 = Color.alpha(startColor);
		int r1 = Color.red(startColor);
		int g1 = Color.green(startColor);
		int b1 = Color.blue(startColor);

		int a2 = Color.alpha(endColor);
		int r2 = Color.red(endColor);
		int g2 = Color.green(endColor);
		int b2 = Color.blue(endColor);

		int a3 = (int) (a1 + (a2 - a1) * radio);
		int r3 = (int) (r1 + (r2 - r1) * radio);
		int g3 = (int) (g1 + (g2 - g1) * radio);
		int b3 = (int) (b1 + (b2 - b1) * radio);

		return Color.argb(a3, r3, g3, b3);
	}

	private static boolean isFinderPoint(ByteMatrix matrix, int row, int column) {
		if (row >= 2 && row <= 4 && column >= 2 && column <= 4)
			return true;
		if (row >= 2 && row <= 4 && column >= matrix.getHeight() - 5
				&& column <= matrix.getHeight() - 3)
			return true;
		if (column >= 2 && column <= 4 && row >= matrix.getWidth() - 5
				&& row <= matrix.getWidth() - 3)
			return true;
		return false;
	}

	private static boolean inFinderPattern(ByteMatrix matrix, int row,
			int column) {
		if (row >= 0 && row <= 6 && column >= 0 && column <= 6)
			return true;
		if (row >= 0 && row <= 6 && column >= matrix.getHeight() - 7
				&& column <= matrix.getHeight() - 1)
			return true;
		if (row >= matrix.getWidth() - 7 && row <= matrix.getWidth() - 1
				&& column >= 0 && column <= 6)
			return true;
		return false;
	}
}
