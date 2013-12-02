package com.qrcode.sdk.demo;

import java.util.Hashtable;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import android.graphics.Bitmap;

public class DecodeUtils {

	public static boolean checkDecode(Bitmap bitmap, String content) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[bitmap.getByteCount()];
		bitmap.getPixels(pixels, 0, 0, 0, 0, width, height);
		LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
		BinaryBitmap bm = new BinaryBitmap(new HybridBinarizer(source));
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		Result result = null;
//		try {
//			result = new MultiFormatReader().decode(bm, hints);
//		} catch (NotFoundException e) {
//			e.printStackTrace();
//		}
		System.out.println(result.toString());
		return false;
	}
}
