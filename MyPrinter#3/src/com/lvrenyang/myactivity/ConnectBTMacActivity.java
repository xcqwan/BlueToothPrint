package com.lvrenyang.myactivity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.lvrenyang.myprinter.DrawerService;
import com.lvrenyang.myprinter.Global;
import com.lvrenyang.myprinter.R;

public class ConnectBTMacActivity extends Activity implements OnClickListener {

	private ProgressDialog dialog;
	private EditText editText;

	private static Handler mHandler = null;
	private static String TAG = "ConnectBTMacActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connectbtmac);

		editText = (EditText) findViewById(R.id.editTextInputMacAddress);
		findViewById(R.id.buttonConnectMacAddress).setOnClickListener(this);
		dialog = new ProgressDialog(this);

		SharedPreferences settings = getSharedPreferences(
				Global.PREFERENCES_FILENAME, 0);
		editText.setText(settings.getString(Global.PREFERENCES_BTADDRESS, ""));

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
		case R.id.buttonConnectMacAddress: {
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (null == adapter) {
				finish();
				break;
			}

			if (!adapter.isEnabled()) {
				if (adapter.enable()) {
					while (!adapter.isEnabled())
						;
					Log.v(TAG, "Enable BluetoothAdapter");
				} else {
					finish();
					break;
				}
			}

			String address = editText.getText().toString().trim();
			if (!BluetoothAdapter.checkBluetoothAddress(address)) {
				Toast.makeText(this, "蓝牙地址不合法。形如01:23:45:67:89:AB的才是合法地址。",
						Toast.LENGTH_LONG).show();
				break;
			}

			SharedPreferences settings = getSharedPreferences(
					Global.PREFERENCES_FILENAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(Global.PREFERENCES_BTADDRESS, address);
			editor.commit();
			dialog.setMessage("正在连接 " + address);
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
			DrawerService.workThread.connectBt(address);

			break;
		}
		}
	}

	static class MHandler extends Handler {

		WeakReference<ConnectBTMacActivity> mActivity;

		MHandler(ConnectBTMacActivity activity) {
			mActivity = new WeakReference<ConnectBTMacActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ConnectBTMacActivity theActivity = mActivity.get();
			switch (msg.what) {
			/**
			 * DrawerService 的 onStartCommand会发送这个消息
			 */

			case Global.MSG_WORKTHREAD_SEND_CONNECTBTRESULT: {
				int result = msg.arg1;
				Toast.makeText(theActivity, (result == 1) ? "连接成功" : "连接失败",
						Toast.LENGTH_SHORT).show();
				Log.v(TAG, "Connect Result: " + result);
				theActivity.dialog.cancel();
				break;
			}

			}
		}
	}

}
