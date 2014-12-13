package com.lvrenyang.myactivity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.lvrenyang.myprinter.DrawerService;
import com.lvrenyang.myprinter.Global;
import com.lvrenyang.myprinter.R;
import com.lvrenyang.utils.DataUtils;

public class FormActivity extends Activity implements OnClickListener {

	private static Handler mHandler = null;
	private static String TAG = "FormActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_form);

		findViewById(R.id.buttonPrintForm).setOnClickListener(this);

		mHandler = new MHandler(this);
		DrawerService.addHandler(mHandler);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DrawerService.delHandler(mHandler);
		mHandler = null;
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.buttonPrintForm: {
			byte[] setHT = {0x1b,0x44,0x18,0x00};
			byte[] HT = {0x09};
			byte[] LF = {0x0d,0x0a};
			byte[][] allbuf = new byte[][]{
					setHT,"FOOD".getBytes(),HT,"PRICE".getBytes(),LF,LF,
					setHT,"DECAF16".getBytes(),HT,"30".getBytes(),LF,
					setHT,"ISLAND BLEND".getBytes(),HT,"180".getBytes(),LF,
					setHT,"FLAVOR SMALL".getBytes(),HT,"30".getBytes(),LF,
					setHT,"Kenya AA".getBytes(),HT,"90".getBytes(),LF,
					setHT,"CHAI".getBytes(),HT,"15.5".getBytes(),LF,
					setHT,"MOCHA".getBytes(),HT,"20".getBytes(),LF,
					setHT,"BREVE".getBytes(),HT,"1000".getBytes(),LF,LF,LF
					};
			byte[] buf = DataUtils.byteArraysToBytes(allbuf);
			if (DrawerService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buf);
				data.putInt(Global.INTPARA1, 0);
				data.putInt(Global.INTPARA2, buf.length);
				DrawerService.workThread.handleCmd(Global.CMD_POS_WRITE, data);
			} else {
				Toast.makeText(this, "请先连接打印机", Toast.LENGTH_SHORT).show();
			}
			break;
		}
		}
	}

	static class MHandler extends Handler {

		WeakReference<FormActivity> mActivity;

		MHandler(FormActivity activity) {
			mActivity = new WeakReference<FormActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			FormActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_POS_WRITERESULT: {
				int result = msg.arg1;
				Toast.makeText(theActivity, (result == 1) ? "成功" : "失败",
						Toast.LENGTH_SHORT).show();
				Log.v(TAG, "Result: " + result);
				break;
			}

			}
		}
	}

}
