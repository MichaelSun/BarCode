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

public class QrcodeUtil {

	public static enum Shape {
		NORMAL, ROUND, WATER
	}

	private static final int QUIET_ZONE_SIZE = 4;

	public static Bitmap encode(String contents, int width, int height,
			int padding, Shape shape, float radiusPercent,
			ErrorCorrectionLevel level, int foregroundColor, int backgroundColor)
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
		return renderResult(code, width, height, padding < 0 ? QUIET_ZONE_SIZE
				: padding, shape, radiusPercent, foregroundColor,
				backgroundColor);
	}

	private static Bitmap renderResult(QRCode code, int width, int height,
			int quietZone, Shape shape, float radiusPercent,
			int foregroundColor, int backgroundColor) {
		ByteMatrix input = code.getMatrix();
		if (input == null) {
			throw new IllegalStateException();
		}
		int inputWidth = input.getWidth();
		int inputHeight = input.getHeight();
		int qrWidth = inputWidth + (quietZone << 1);
		int qrHeight = inputHeight + (quietZone << 1);
		int outputWidth = Math.max(width, qrWidth);
		int outputHeight = Math.max(height, qrHeight);

		int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
		int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
		int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

		Bitmap bitmap = Bitmap.createBitmap(outputWidth, outputHeight,
				Config.ARGB_8888);
		bitmap.eraseColor(255);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(backgroundColor);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(foregroundColor);
		paint.setStyle(Style.FILL);

		int roundRadius = (int) (multiple * radiusPercent);

		for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
			for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
				if (input.get(inputX, inputY) == 1) {
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

	private static boolean isSet(ByteMatrix matrix, int row, int column) {
		if (matrix == null) {
			return false;
		}

		if (row < 0 || row > matrix.getWidth() - 1) {
			return false;
		}

		if (column < 0 || column > matrix.getHeight() - 1) {
			return false;
		}

		return matrix.get(row, column) == 1;
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
}
