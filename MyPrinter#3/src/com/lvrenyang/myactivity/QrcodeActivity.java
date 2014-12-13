package com.lvrenyang.myactivity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.lvrenyang.myprinter.DrawerService;
import com.lvrenyang.myprinter.Global;
import com.lvrenyang.myprinter.R;

public class QrcodeActivity extends Activity implements OnClickListener {

	private static Handler mHandler = null;
	private static String TAG = "QrcodeActivity";

	private Button buttonPrintQRCode;
	private EditText editTextQrcode;
	private Button buttonQrcodetype, buttonQrcodeWidth,
			buttonErrorCorrectionLevel;

	private CheckBox useEpsonQRCmd;
	public static int nQrcodetype = 0, nQrcodeWidth = 4,
			nErrorCorrectionLevel = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrcode);

		buttonPrintQRCode = (Button) findViewById(R.id.buttonPrintQRCode);
		buttonPrintQRCode.setOnClickListener(this);

		editTextQrcode = (EditText) findViewById(R.id.editTextQrcode);

		buttonQrcodetype = (Button) findViewById(R.id.buttonQrcodetype);
		buttonQrcodetype.setOnClickListener(this);
		buttonQrcodeWidth = (Button) findViewById(R.id.buttonQrcodeWidth);
		buttonQrcodeWidth.setOnClickListener(this);
		buttonErrorCorrectionLevel = (Button) findViewById(R.id.buttonErrorCorrectionLevel);
		buttonErrorCorrectionLevel.setOnClickListener(this);

		useEpsonQRCmd = (CheckBox) findViewById(R.id.checkBoxUseEpsonQRCmd);
		mHandler = new MHandler(this);
		DrawerService.addHandler(mHandler);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateBarcodeUI();
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
		case R.id.buttonPrintQRCode: {
			String strQrcode = editTextQrcode.getText().toString();
			if (strQrcode.length() == 0)
				return;
			int nWidthX = nQrcodeWidth + 2;
			int necl = nErrorCorrectionLevel + 1;
			if (DrawerService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putString(Global.STRPARA1, strQrcode);
				data.putInt(Global.INTPARA1, nWidthX);
				data.putInt(Global.INTPARA2, necl);
				if (useEpsonQRCmd.isChecked())
					DrawerService.workThread.handleCmd(
							Global.CMD_EPSON_SETQRCODE, data);
				else
					DrawerService.workThread.handleCmd(
							Global.CMD_POS_SETQRCODE, data);
			} else {
				Toast.makeText(this, "请先连接打印机", Toast.LENGTH_SHORT).show();
			}
			break;
		}

		case R.id.buttonQrcodetype: {

			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.qrcodetype)
					.setItems(R.array.qrcodetype,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.qrcodetype);
									buttonQrcodetype.setText(items[which]);
									nQrcodetype = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonQrcodeWidth: {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.qrcodewidth)
					.setItems(R.array.qrcodewidth,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.qrcodewidth);
									buttonQrcodeWidth.setText(items[which]);
									nQrcodeWidth = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonErrorCorrectionLevel: {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.errorcorrectionlevel)
					.setItems(R.array.errorcorrectionlevel,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.errorcorrectionlevel);
									buttonErrorCorrectionLevel
											.setText(items[which]);
									nErrorCorrectionLevel = which;
								}
							}).create();

			dialog.show();
			break;
		}
		}
	}

	static class MHandler extends Handler {

		WeakReference<QrcodeActivity> mActivity;

		MHandler(QrcodeActivity activity) {
			mActivity = new WeakReference<QrcodeActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			QrcodeActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_EPSON_SETQRCODERESULT:
			case Global.CMD_POS_SETQRCODERESULT: {
				int result = msg.arg1;
				Toast.makeText(theActivity, (result == 1) ? "成功" : "失败",
						Toast.LENGTH_SHORT).show();
				Log.v(TAG, "Result: " + result);
				break;
			}

			}
		}
	}

	private void updateBarcodeUI() {
		// Configue

		buttonQrcodetype.setText(getResources().getStringArray(
				R.array.qrcodetype)[nQrcodetype]);
		buttonQrcodeWidth.setText(getResources().getStringArray(
				R.array.qrcodewidth)[nQrcodeWidth]);
		buttonErrorCorrectionLevel.setText(getResources().getStringArray(
				R.array.errorcorrectionlevel)[nErrorCorrectionLevel]);

	}
}
