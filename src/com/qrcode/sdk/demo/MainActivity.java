package com.qrcode.sdk.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.zxing.WriterException;
import com.qrcode.sdk.demo.QrcodeUtil.Shape;

public class MainActivity extends Activity implements OnSeekBarChangeListener,
		OnClickListener {
	private static String CONTENT = "MECARD:N:Ting Sun;Email:ting.sun@dajie-inc.com;Address:Beijing Chaoyang;Phone:18612560621;;";
	private static int SEEKBAR_MAX = 1000;

	ImageView mQrcodeImageView;
	RelativeLayout mSettingPanel;
	SeekBar mShapeBar;
	Button mResetShapeBt;
	EditText mContentEt;
	Button mGenerateBT;
	Button mClearContentBt;

	Handler mHandler = new Handler();

	int width;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		width = getResources().getDisplayMetrics().widthPixels;

		mSettingPanel = (RelativeLayout) findViewById(R.id.setting_rl);
		mQrcodeImageView = (ImageView) findViewById(R.id.qrcode_img_iv);
		mShapeBar = (SeekBar) findViewById(R.id.shape_bar);
		mResetShapeBt = (Button) findViewById(R.id.shape_reset_bt);
		mContentEt = (EditText) findViewById(R.id.content_et);
		mGenerateBT = (Button) findViewById(R.id.generate_bt);
		mClearContentBt = (Button) findViewById(R.id.clear_content_bt);

		mShapeBar.setOnSeekBarChangeListener(this);
		mResetShapeBt.setOnClickListener(this);
		mShapeBar.setMax(SEEKBAR_MAX);
		mShapeBar.setProgress(SEEKBAR_MAX / 2);
		mClearContentBt.setOnClickListener(this);
		mGenerateBT.setOnClickListener(this);

		mContentEt.setText(CONTENT);

		postChange();
		// ParsedResult result =
		// QrcodeUtil.decode(BitmapFactory.decodeFile("/sdcard/test/1.jpg"));
		// System.out.println(result == null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_settings) {
			mSettingPanel
					.setVisibility(mSettingPanel.getVisibility() == View.GONE ? View.VISIBLE
							: View.GONE);
		}

		return super.onOptionsItemSelected(item);
	}

	public boolean saveBitmapToFile(String savePath, Bitmap bitmap) {
		if (savePath == null || bitmap == null) {
			return false;
		}

		try {
			OutputStream os = new FileOutputStream(new File(savePath));
			bitmap.compress(CompressFormat.PNG, 100, os);
			os.flush();
			os.close();

			return true;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		return false;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		postChange();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.shape_reset_bt:
			mShapeBar.setProgress(SEEKBAR_MAX / 2);
			break;
		case R.id.generate_bt:
			postChange();
			break;
		case R.id.clear_content_bt:
			mContentEt.setText("");
		default:
			break;
		}
	}

	private void postChange() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					int progress = mShapeBar.getProgress();
					String content = mContentEt.getText().toString();
					Shape shape = Shape.NORMAL;
					if (progress < SEEKBAR_MAX / 2) {
						shape = Shape.WATER;
					} else if (progress > SEEKBAR_MAX / 2) {
						shape = Shape.ROUND;
					}
					float radiusPercent = Math.abs(SEEKBAR_MAX - progress * 2)
							/ (float) SEEKBAR_MAX;
					if (shape == Shape.WATER) {
						// 液化半径上限为0.7
						radiusPercent *= 0.7;
					}
					Bitmap bitmap = QrcodeUtil.encode(
							TextUtils.isEmpty(content) ? CONTENT : content,
							width, width, -1, shape, radiusPercent);
					mQrcodeImageView.setImageBitmap(bitmap);
				} catch (WriterException e) {
					e.printStackTrace();
				}
			}
		}, 1000);
	}

}
