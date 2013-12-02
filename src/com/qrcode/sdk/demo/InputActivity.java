package com.qrcode.sdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class InputActivity extends Activity implements OnClickListener {

	private EditText mInputEt;

	private Button mGenerateBtn;
	private Button mClearBtn;
	private Button mDefaultBtn;

//	private static String MECARD_SAMPLE = "MECARD:N:Ting Sun;EMAIL:ting.sun@dajie-inc.com;ADR:Beijing Chaoyang;TEL:18612560621;;";
	private static String VCARD_SAMPLE = "BEGIN:VCARD\nVERSION:3.0\nFN:Ting\nPHOTO;VALUE=uri:http://tp3.sinaimg.cn/1668659954/180/5679291057/1\nTEL;CELL;VOICE:18612560521\nURL:http://lzem.me\nEND:VCARD";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input);

		mInputEt = (EditText) findViewById(R.id.input_et);
		mInputEt.setText(VCARD_SAMPLE);

		mGenerateBtn = (Button) findViewById(R.id.generate_bt);
		mDefaultBtn = (Button) findViewById(R.id.default_bt);
		mClearBtn = (Button) findViewById(R.id.clear_bt);

		mGenerateBtn.setOnClickListener(this);
		mDefaultBtn.setOnClickListener(this);
		mClearBtn.setOnClickListener(this);

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.generate_bt:
			String content = mInputEt.getText().toString();
			if (!TextUtils.isEmpty(content)) {
				Intent intent = new Intent(this, QrcodeActivity.class);
				intent.putExtra(QrcodeActivity.EXTRA_CONTENT, content);
				startActivity(intent);
			}
			break;
		case R.id.clear_bt:
			mInputEt.setText("");
			break;
		case R.id.default_bt:
			mInputEt.setText(VCARD_SAMPLE);
			break;
		}
	}

}
