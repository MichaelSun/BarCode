package com.qrcode.sdk.demo;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
//import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class DecodeUtils {

	public static boolean checkDecode(Bitmap b, String content) {
//		if (b == null || content == null)
//			return false;
//
//		int oldW = b.getWidth();
//		int oldH = b.getHeight();
//		Matrix matrix = new Matrix();
//		if (oldW > 300 || oldH > 300) {
//			float scaleWidth = ((float) 300) / oldW;
//			float scaleHeight = ((float) 300) / oldH;
//			matrix.postScale(scaleWidth, scaleHeight);
//		}
//		Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, oldW, oldH, matrix, true);
//
//		int width = bitmap.getWidth();
//		int height = bitmap.getHeight();
//		int[] pixels = new int[width * height];
//		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//		LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
//		BinaryBitmap bm = new BinaryBitmap(new HybridBinarizer(source));
//
//		Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(
//				DecodeHintType.class);
//		Collection<BarcodeFormat> decodeFormats = EnumSet
//				.noneOf(BarcodeFormat.class);
//		decodeFormats.addAll(EnumSet.of(BarcodeFormat.QR_CODE));
//		hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
//		hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
//		Result result = null;
//		try {
//			result = new MultiFormatReader().decode(bm, hints);
//		} catch (NotFoundException e) {
//			e.printStackTrace();
//		}
//		if (result != null && result.getText() != null
//				&& result.getText().equals(content)) {
//			System.out.println(result.toString());
//			return true;
//		}
		return false;
	}

}
