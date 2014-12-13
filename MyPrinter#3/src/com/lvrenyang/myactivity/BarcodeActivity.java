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
import android.widget.EditText;
import android.widget.Toast;

import com.lvrenyang.myprinter.DrawerService;
import com.lvrenyang.myprinter.Global;
import com.lvrenyang.myprinter.R;
import com.lvrenyang.pos.Cmd;

public class BarcodeActivity extends Activity implements OnClickListener {

	private static Handler mHandler = null;
	private static String TAG = "BarcodeActivity";

	private Button buttonPrintBarcode;
	private EditText editTextBarcode;
	private Button buttonBarcodetype, buttonStartOrgx, buttonBarcodeWidth,
			buttonBarcodeHeight, buttonBarcodeFontType,
			buttonBarcodeFontPosition;

	private static int nBarcodetype, nStartOrgx, nBarcodeWidth = 1,
			nBarcodeHeight = 3, nBarcodeFontType, nBarcodeFontPosition = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode);

		buttonPrintBarcode = (Button) findViewById(R.id.buttonPrintBarcode);
		buttonPrintBarcode.setOnClickListener(this);

		editTextBarcode = (EditText) findViewById(R.id.editTextBarcode);

		buttonBarcodetype = (Button) findViewById(R.id.buttonBarcodetype);
		buttonBarcodetype.setOnClickListener(this);
		buttonStartOrgx = (Button) findViewById(R.id.buttonStartOrgx);
		buttonStartOrgx.setOnClickListener(this);
		buttonBarcodeWidth = (Button) findViewById(R.id.buttonBarcodeWidth);
		buttonBarcodeWidth.setOnClickListener(this);
		buttonBarcodeHeight = (Button) findViewById(R.id.buttonBarcodeHeight);
		buttonBarcodeHeight.setOnClickListener(this);
		buttonBarcodeFontType = (Button) findViewById(R.id.buttonBarcodeFontType);
		buttonBarcodeFontType.setOnClickListener(this);
		buttonBarcodeFontPosition = (Button) findViewById(R.id.buttonBarcodeFontPosition);
		buttonBarcodeFontPosition.setOnClickListener(this);

		mHandler = new MHandler(this);
		DrawerService.addHandler(mHandler);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DrawerService.delHandler(mHandler);
		mHandler = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateBarcodeUI();
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.buttonPrintBarcode: {
			String strBarcode = editTextBarcode.getText().toString();
			if (strBarcode.length() == 0)
				break;
			int nOrgx = nStartOrgx * 12;
			int nType = Cmd.Constant.BARCODE_TYPE_UPC_A + nBarcodetype;
			int nWidthX = nBarcodeWidth + 2;
			int nHeight = (nBarcodeHeight + 1) * 24;
			int nHriFontType = nBarcodeFontType;
			int nHriFontPosition = nBarcodeFontPosition;

			if (DrawerService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putString(Global.STRPARA1, strBarcode);
				data.putInt(Global.INTPARA1, nOrgx);
				data.putInt(Global.INTPARA2, nType);
				data.putInt(Global.INTPARA3, nWidthX);
				data.putInt(Global.INTPARA4, nHeight);
				data.putInt(Global.INTPARA5, nHriFontType);
				data.putInt(Global.INTPARA6, nHriFontPosition);
				DrawerService.workThread.handleCmd(Global.CMD_POS_SETBARCODE,
						data);
			} else {
				Toast.makeText(this, "请先连接打印机", Toast.LENGTH_SHORT).show();
			}
			break;
		}

		case R.id.buttonBarcodetype: {

			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.barcodetype)
					.setItems(R.array.barcodetype,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.barcodetype);
									String[] strs = getResources()
											.getStringArray(R.array.barcodestr);
									buttonBarcodetype.setText(items[which]);
									editTextBarcode.setText(strs[which]);
									nBarcodetype = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonStartOrgx: {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.startorgx)
					.setItems(R.array.barcodestartorgx,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.barcodestartorgx);
									buttonStartOrgx.setText(items[which]);
									nStartOrgx = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonBarcodeWidth: {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.barcodewidth)
					.setItems(R.array.barcodewidth,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.barcodewidth);
									buttonBarcodeWidth.setText(items[which]);
									nBarcodeWidth = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonBarcodeHeight: {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.barcodeheight)
					.setItems(R.array.barcodeheight,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.barcodeheight);
									buttonBarcodeHeight.setText(items[which]);
									nBarcodeHeight = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonBarcodeFontType: {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.barcodefonttype)
					.setItems(R.array.barcodefonttype,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.barcodefonttype);
									buttonBarcodeFontType.setText(items[which]);
									nBarcodeFontType = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonBarcodeFontPosition: {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.barcodefontposition)
					.setItems(R.array.barcodefontposition,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.barcodefontposition);
									buttonBarcodeFontPosition
											.setText(items[which]);
									nBarcodeFontPosition = which;
								}
							}).create();

			dialog.show();
			break;
		}
		}
	}

	private void updateBarcodeUI() {
		// Configue
		editTextBarcode.setText(getResources().getStringArray(
				R.array.barcodestr)[nBarcodetype]);
		buttonBarcodetype.setText(getResources().getStringArray(
				R.array.barcodetype)[nBarcodetype]);
		buttonStartOrgx.setText(getResources().getStringArray(
				R.array.barcodestartorgx)[nStartOrgx]);
		buttonBarcodeWidth.setText(getResources().getStringArray(
				R.array.barcodewidth)[nBarcodeWidth]);
		buttonBarcodeHeight.setText(getResources().getStringArray(
				R.array.barcodeheight)[nBarcodeHeight]);
		buttonBarcodeFontType.setText(getResources().getStringArray(
				R.array.barcodefonttype)[nBarcodeFontType]);
		buttonBarcodeFontPosition.setText(getResources().getStringArray(
				R.array.barcodefontposition)[nBarcodeFontPosition]);

	}

	static class MHandler extends Handler {

		WeakReference<BarcodeActivity> mActivity;

		MHandler(BarcodeActivity activity) {
			mActivity = new WeakReference<BarcodeActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BarcodeActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_POS_SETBARCODERESULT: {
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
