package com.qrcode.sdk.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.qrcode.sdk.demo.QrcodeUtil.BORDER_TYPE;
import com.qrcode.sdk.demo.QrcodeUtil.GRADIENT_TYPE;
import com.qrcode.sdk.demo.QrcodeUtil.Shape;

@SuppressLint("DefaultLocale")
public class QrcodeActivity extends Activity implements
		OnSeekBarChangeListener, OnClickListener, OnCheckedChangeListener,
		ColorPickerDialog.OnColorChangedListener, View.OnLongClickListener {

	public static final String EXTRA_CONTENT = "extra_content";
	private String mContent = "";

	private static int SEEKBAR_MAX = 1000;

	private static final int COLOR_TYPE_FOREGROUND = 0x001;
	private static final int COLOR_TYPE_BACKGROUND = 0x002;
	private static final int COLOR_TYPE_GRADIENT = 0x003;
	private static final int COLOR_TYPE_FINDER = 0x004;

	private static final int REQUEST_LOAD_IMAGE = 1;

	ImageView mQrcodeImageView;

	View mShapeLayout;
	View mLevelLayout;
	View mColorLayout;
	View mGradientLayout;
	View mFinderColorLayout;
	View mBorderLayout;
	List<View> mMenuViews;

	SeekBar mShapeBar;
	Button mResetShapeBt;
	RadioGroup mEcLevelRg;
	Button mForegroundColorChooseBt;
	Button mBackgroundColorChooseBt;
	Button mResetColorBt;
	Button mBackgroundImageChooseBt;
	Button mGradientColorChooseBt;
	Button mResetGradientColorBt;
	Spinner mGradientTypeSpinner;
	Button mFinderColorChooseBt;
	Button mResetFinderColorBt;
	Spinner mBorderTypeSpinner;
	Button mResetBorderBt;

	Handler mHandler = new Handler();

	int width;
	int mForegroundColor = Color.BLACK;
	int mBackgroundColor = Color.WHITE;
	Bitmap mBackgroundBm = null;
	int mGradientColor = Color.BLACK;
	GRADIENT_TYPE mGadientType = GRADIENT_TYPE.ROUND;
	int mFinderColor = Color.BLACK;
	BORDER_TYPE mBorderType = BORDER_TYPE.NONE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrcode);

		if (getIntent() != null) {
			mContent = getIntent().getStringExtra(EXTRA_CONTENT);
		}

		width = getResources().getDisplayMetrics().widthPixels;

		mMenuViews = new ArrayList<View>();
		mShapeLayout = findViewById(R.id.shape_rl);
		mLevelLayout = findViewById(R.id.ec_level_rl);
		mColorLayout = findViewById(R.id.color_rl);
		mGradientLayout = findViewById(R.id.gradient_color_rl);
		mFinderColorLayout = findViewById(R.id.finder_color_rl);
		mBorderLayout = findViewById(R.id.border_rl);
		mMenuViews.add(mShapeLayout);
		mMenuViews.add(mLevelLayout);
		mMenuViews.add(mColorLayout);
		mMenuViews.add(mGradientLayout);
		mMenuViews.add(mFinderColorLayout);
		mMenuViews.add(mBorderLayout);

		mQrcodeImageView = (ImageView) findViewById(R.id.qrcode_img_iv);

		mShapeBar = (SeekBar) findViewById(R.id.shape_bar);
		mResetShapeBt = (Button) findViewById(R.id.shape_reset_bt);
		mEcLevelRg = (RadioGroup) findViewById(R.id.ec_level_rg);
		mForegroundColorChooseBt = (Button) findViewById(R.id.foreground_color_choose_bt);
		mBackgroundColorChooseBt = (Button) findViewById(R.id.background_color_choose_bt);
		mResetColorBt = (Button) findViewById(R.id.color_reset_bt);
		mBackgroundImageChooseBt = (Button) findViewById(R.id.background_image_choose_bt);
		mGradientColorChooseBt = (Button) findViewById(R.id.gradient_color_choose_bt);
		mResetGradientColorBt = (Button) findViewById(R.id.gradient_color_reset_bt);
		initGradientSpinner();
		mFinderColorChooseBt = (Button) findViewById(R.id.finder_color_choose_bt);
		mResetFinderColorBt = (Button) findViewById(R.id.finder_color_reset_bt);
		initBorderSpinner();
		mResetBorderBt = (Button) findViewById(R.id.border_reset_bt);

		mShapeBar.setOnSeekBarChangeListener(this);
		mResetShapeBt.setOnClickListener(this);
		mShapeBar.setMax(SEEKBAR_MAX);
		mShapeBar.setProgress(SEEKBAR_MAX / 2);
		mEcLevelRg.setOnCheckedChangeListener(this);
		mForegroundColorChooseBt.setOnClickListener(this);
		mBackgroundColorChooseBt.setOnClickListener(this);
		mQrcodeImageView.setClickable(true);
		mQrcodeImageView.setOnClickListener(this);
		mQrcodeImageView.setOnLongClickListener(this);
		mResetColorBt.setOnClickListener(this);
		mBackgroundImageChooseBt.setOnClickListener(this);
		mGradientColorChooseBt.setOnClickListener(this);
		mResetGradientColorBt.setOnClickListener(this);
		mFinderColorChooseBt.setOnClickListener(this);
		mResetFinderColorBt.setOnClickListener(this);
		mResetBorderBt.setOnClickListener(this);

		mForegroundColorChooseBt.setBackgroundColor(mForegroundColor);
		mBackgroundColorChooseBt.setBackgroundColor(mBackgroundColor);
		mGradientColorChooseBt.setBackgroundColor(mGradientColor);
		mFinderColorChooseBt.setBackgroundColor(mFinderColor);

		// ParsedResult result =
		// QrcodeUtil.decode(BitmapFactory.decodeFile("/sdcard/test/test.png"));
		// System.out.println(result == null);
		postChange();
	}

	private void initGradientSpinner() {
		mGradientTypeSpinner = (Spinner) findViewById(R.id.gradient_type_select_sp);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.gradient_type_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mGradientTypeSpinner.setAdapter(adapter);

		mGradientTypeSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						switch (arg2) {
						case 0:
							mGadientType = GRADIENT_TYPE.ROUND;
							break;
						case 1:
							mGadientType = GRADIENT_TYPE.SLASH;
							break;
						case 2:
							mGadientType = GRADIENT_TYPE.BACKSLASH;
							break;
						case 3:
							mGadientType = GRADIENT_TYPE.HORIZONTAL;
							break;
						case 4:
							mGadientType = GRADIENT_TYPE.VERTICAL;
							break;
						}
						postChange();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});
	}

	private void initBorderSpinner() {
		mBorderTypeSpinner = (Spinner) findViewById(R.id.border_type_select_sp);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.border_type_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mBorderTypeSpinner.setAdapter(adapter);

		mBorderTypeSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						switch (arg2) {
						case 0:
							mBorderType = BORDER_TYPE.NONE;
							break;
						case 1:
							mBorderType = BORDER_TYPE.CIRCLE;
							break;
						case 2:
							mBorderType = BORDER_TYPE.RHOMBUS;
							break;
						}
						postChange();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_shape:
			mShapeLayout.setVisibility(View.VISIBLE);
			mLevelLayout.setVisibility(View.GONE);
			mColorLayout.setVisibility(View.GONE);
			mGradientLayout.setVisibility(View.GONE);
			mFinderColorLayout.setVisibility(View.GONE);
			mBorderLayout.setVisibility(View.GONE);
			return true;
		case R.id.action_level:
			mShapeLayout.setVisibility(View.GONE);
			mLevelLayout.setVisibility(View.VISIBLE);
			mColorLayout.setVisibility(View.GONE);
			mGradientLayout.setVisibility(View.GONE);
			mFinderColorLayout.setVisibility(View.GONE);
			mBorderLayout.setVisibility(View.GONE);
			return true;
		case R.id.action_color:
			mShapeLayout.setVisibility(View.GONE);
			mLevelLayout.setVisibility(View.GONE);
			mColorLayout.setVisibility(View.VISIBLE);
			mGradientLayout.setVisibility(View.GONE);
			mFinderColorLayout.setVisibility(View.GONE);
			mBorderLayout.setVisibility(View.GONE);
			return true;
		case R.id.action_gradient:
			mShapeLayout.setVisibility(View.GONE);
			mLevelLayout.setVisibility(View.GONE);
			mColorLayout.setVisibility(View.GONE);
			mGradientLayout.setVisibility(View.VISIBLE);
			mFinderColorLayout.setVisibility(View.GONE);
			mBorderLayout.setVisibility(View.GONE);
			return true;
		case R.id.action_finder:
			mShapeLayout.setVisibility(View.GONE);
			mLevelLayout.setVisibility(View.GONE);
			mColorLayout.setVisibility(View.GONE);
			mGradientLayout.setVisibility(View.GONE);
			mFinderColorLayout.setVisibility(View.VISIBLE);
			mBorderLayout.setVisibility(View.GONE);
			return true;
		case R.id.action_broder:
			mShapeLayout.setVisibility(View.GONE);
			mLevelLayout.setVisibility(View.GONE);
			mColorLayout.setVisibility(View.GONE);
			mGradientLayout.setVisibility(View.GONE);
			mFinderColorLayout.setVisibility(View.GONE);
			mBorderLayout.setVisibility(View.VISIBLE);
			return true;
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
		case R.id.foreground_color_choose_bt:
			new ColorPickerDialog(this, this, mForegroundColor,
					COLOR_TYPE_FOREGROUND).show();
			break;
		case R.id.background_color_choose_bt:
			new ColorPickerDialog(this, this, mBackgroundColor,
					COLOR_TYPE_BACKGROUND).show();
			break;
		case R.id.color_reset_bt:
			mForegroundColor = Color.BLACK;
			mBackgroundColor = Color.WHITE;
			mForegroundColorChooseBt.setBackgroundColor(mForegroundColor);
			mBackgroundColorChooseBt.setBackgroundColor(mBackgroundColor);
			if (mBackgroundBm != null && !mBackgroundBm.isRecycled()) {
				mBackgroundBm.recycle();
			}
			mBackgroundBm = null;
			postChange();
			break;
		case R.id.background_image_choose_bt:
			selectPhoto();
			break;
		case R.id.gradient_color_choose_bt:
			new ColorPickerDialog(this, this, mGradientColor,
					COLOR_TYPE_GRADIENT).show();
			break;
		case R.id.gradient_color_reset_bt:
			mGradientColor = mForegroundColor;
			mGradientColorChooseBt.setBackgroundColor(mGradientColor);
			postChange();
			break;
		case R.id.finder_color_choose_bt:
			new ColorPickerDialog(this, this, mFinderColor, COLOR_TYPE_FINDER)
					.show();
			break;
		case R.id.finder_color_reset_bt:
			mFinderColor = mForegroundColor;
			mFinderColorChooseBt.setBackgroundColor(mFinderColor);
			postChange();
			break;
		case R.id.border_reset_bt:
			mBorderType = BORDER_TYPE.NONE;
			mBorderTypeSpinner.setSelection(0);
			postChange();
			break;
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
					Shape shape = Shape.NORMAL;
					ErrorCorrectionLevel level = QrcodeActivity.this
							.getEcLevel();
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
					Bitmap bitmap = QrcodeUtil.encode(mContent, width * 4 / 5,
							width * 4 / 5, -1, shape, radiusPercent, level,
							mForegroundColor, mBackgroundColor, mBackgroundBm,
							mFinderColor, mGradientColor, mGadientType,
							mBorderType);
					mQrcodeImageView.setImageBitmap(bitmap);
				} catch (WriterException e) {
					e.printStackTrace();
				}
			}
		}, 1000);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		postChange();
	}

	private ErrorCorrectionLevel getEcLevel() {
		ErrorCorrectionLevel level = ErrorCorrectionLevel.L;
		int checkedId = mEcLevelRg.getCheckedRadioButtonId();
		switch (checkedId) {
		case R.id.ec_level_2_rb:
			level = ErrorCorrectionLevel.M;
			break;
		case R.id.ec_level_3_rb:
			level = ErrorCorrectionLevel.Q;
			break;
		case R.id.ec_level_4_rb:
			level = ErrorCorrectionLevel.H;
			break;
		case R.id.ec_level_1_rb:
			break;
		default:
			break;
		}

		return level;
	}

	@Override
	public void colorChanged(int color, int type) {
		switch (type) {
		case COLOR_TYPE_FOREGROUND:
			mForegroundColor = color;
			mForegroundColorChooseBt.setBackgroundColor(color);
			break;
		case COLOR_TYPE_BACKGROUND:
			mBackgroundColor = color;
			mBackgroundColorChooseBt.setBackgroundColor(color);
			break;
		case COLOR_TYPE_GRADIENT:
			mGradientColor = color;
			mGradientColorChooseBt.setBackgroundColor(color);
			break;
		case COLOR_TYPE_FINDER:
			mFinderColor = color;
			mFinderColorChooseBt.setBackgroundColor(color);
			break;
		default:
			break;
		}

		postChange();
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.qrcode_img_iv:
			BitmapDrawable drawable = (BitmapDrawable) mQrcodeImageView
					.getDrawable();
			if (drawable != null) {
				Bitmap bitmap = drawable.getBitmap();
				if (bitmap != null) {
					Thread thread = new Thread(new SaveRunnable(bitmap));
					thread.setPriority(Thread.MAX_PRIORITY);
					thread.start();
				}
			}

			break;
		default:
			break;
		}
		return true;
	}

	private final class SaveRunnable implements Runnable {
		Bitmap mBitmap;

		public SaveRunnable(Bitmap bitmap) {
			mBitmap = bitmap;
		}

		@Override
		public void run() {
			if (mBitmap == null) {
				return;
			}

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());

			String pictureName = String.format(
					"Qrcode_%d%02d%02d_%02d%02d%02d",
					calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)
							+ (1 - Calendar.JANUARY),
					calendar.get(Calendar.DATE),
					calendar.get(Calendar.HOUR_OF_DAY),
					calendar.get(Calendar.MINUTE),
					calendar.get(Calendar.SECOND));

			File filePath = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			filePath = new File(filePath, "/Qrcode/");
			filePath.mkdirs();
			filePath = new File(filePath, pictureName + ".jpeg");

			try {
				filePath.createNewFile();
				FileOutputStream fos = new FileOutputStream(filePath);
				mBitmap.compress(CompressFormat.JPEG, 100, fos);
				fos.close();

				ExifInterface exif = new ExifInterface(
						filePath.getAbsolutePath());
				exif.setAttribute(ExifInterface.TAG_ORIENTATION,
						Integer.toString(ExifInterface.ORIENTATION_NORMAL));
				exif.saveAttributes();

				ContentValues v = new ContentValues();
				v.put(MediaColumns.TITLE, pictureName);
				v.put(MediaColumns.DISPLAY_NAME, pictureName);
				v.put(ImageColumns.DESCRIPTION, "Save as qrcode.");
				v.put(MediaColumns.DATE_ADDED, calendar.getTimeInMillis());
				v.put(ImageColumns.DATE_TAKEN, calendar.getTimeInMillis());
				v.put(MediaColumns.DATE_MODIFIED, calendar.getTimeInMillis());
				v.put(MediaColumns.MIME_TYPE, "image/jpeg");
				v.put(ImageColumns.ORIENTATION, 0);
				v.put(MediaColumns.DATA, filePath.getAbsolutePath());

				File parent = filePath.getParentFile();
				String path = parent.toString().toLowerCase(Locale.ENGLISH);
				String name = parent.getName().toLowerCase(Locale.ENGLISH);
				v.put(Images.ImageColumns.BUCKET_ID, path.hashCode());
				v.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
				v.put(MediaColumns.SIZE, filePath.length());

				ContentResolver c = getContentResolver();
				c.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(QrcodeActivity.this, "save success!",
							Toast.LENGTH_SHORT).show();
				}
			});
		}

	}

	private void selectPhoto() {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_LOAD_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_LOAD_IMAGE && resultCode == RESULT_OK
				&& data != null) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			if (mBackgroundBm != null && !mBackgroundBm.isRecycled()) {
				mBackgroundBm.recycle();
				mBackgroundBm = null;
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(picturePath, options);
			options.inSampleSize = computeSampleSize(options,
					mQrcodeImageView.getWidth());
			options.inJustDecodeBounds = false;
			mBackgroundBm = BitmapFactory.decodeFile(picturePath, options);
			postChange();
		}
	}

	private int computeSampleSize(BitmapFactory.Options options, int target) {
		int w = options.outWidth;
		int h = options.outHeight;
		int candidateW = w / target;
		int candidateH = h / target;
		int candidate = Math.max(candidateW, candidateH);
		if (candidate == 0)
			return 1;
		if (candidate > 1) {
			if ((w > target) && (w / candidate) < target)
				candidate -= 1;
		}
		if (candidate > 1) {
			if ((h > target) && (h / candidate) < target)
				candidate -= 1;
		}
		return candidate;
	}

}
