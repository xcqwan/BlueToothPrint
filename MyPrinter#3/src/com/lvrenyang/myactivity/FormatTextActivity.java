package com.lvrenyang.myactivity;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.lvrenyang.myprinter.DrawerService;
import com.lvrenyang.myprinter.Global;
import com.lvrenyang.myprinter.R;
import com.lvrenyang.pos.Cmd;
import com.lvrenyang.utils.DataUtils;

public class FormatTextActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnSeekBarChangeListener {

	private Button btPrintEnglish, btPrintChinese, btPrintEwen, btFontSize,
			btTextAlign, btScaleTimesWidth, btScaleTimesHeight;
	private CheckBox cbBlackWhiteReverse, cbBold, cbUpsideDown, cbTurnRight90,
			cbUnderLine1, cbUnderLine2;
	private SeekBar sbLineHeight, sbRightSpace;
	private TextView tvLineHeight, tvRightSpace;

	private static int nFontSize, nTextAlign, nScaleTimesWidth,
			nScaleTimesHeight, nFontStyle, nLineHeight = 32, nRightSpace;

	private static Handler mHandler = null;
	private static String TAG = "FormatTextActivity";

	String strEnglish = "~!@#$%^&*()_+`[]{}\\|;',./:\"<>?1234567890-=abcdefghijklmnopqrstuvwxyz\n";
	String strChinese = "待到山花烂漫时，她在丛中笑。\n";
	String strEwen = "ЗАО \"НАЗВАНИЕ\r\n  ВАШЕЙ КОМПАНИИ\"\r\nНаша компания\r\n";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_formattext);

		btPrintEnglish = (Button) findViewById(R.id.btPrintEnglish);
		btPrintChinese = (Button) findViewById(R.id.btPrintChinese);
		btPrintEwen = (Button) findViewById(R.id.btPrintEwen);
		btFontSize = (Button) findViewById(R.id.btFontSize);
		btTextAlign = (Button) findViewById(R.id.btTextAlign);
		btScaleTimesWidth = (Button) findViewById(R.id.btScaleTimesWidth);
		btScaleTimesHeight = (Button) findViewById(R.id.btScaleTimesHeight);

		btPrintEnglish.setOnClickListener(this);
		btPrintChinese.setOnClickListener(this);
		btPrintEwen.setOnClickListener(this);
		btFontSize.setOnClickListener(this);
		btTextAlign.setOnClickListener(this);
		btScaleTimesWidth.setOnClickListener(this);
		btScaleTimesHeight.setOnClickListener(this);

		cbBlackWhiteReverse = (CheckBox) findViewById(R.id.cbBlackWhiteReverse);
		cbBold = (CheckBox) findViewById(R.id.cbBold);
		cbUpsideDown = (CheckBox) findViewById(R.id.cbUpsideDown);
		cbTurnRight90 = (CheckBox) findViewById(R.id.cbTurnRight90);
		cbUnderLine1 = (CheckBox) findViewById(R.id.cbUnderLine1);
		cbUnderLine2 = (CheckBox) findViewById(R.id.cbUnderLine2);

		cbBlackWhiteReverse.setOnClickListener(this);
		cbBold.setOnClickListener(this);
		cbUpsideDown.setOnClickListener(this);
		cbTurnRight90.setOnClickListener(this);
		cbUnderLine1.setOnClickListener(this);
		cbUnderLine2.setOnClickListener(this);

		sbLineHeight = (SeekBar) findViewById(R.id.sbLineHeight);
		sbLineHeight.setMax(255);
		sbLineHeight.setOnSeekBarChangeListener(this);
		sbRightSpace = (SeekBar) findViewById(R.id.sbRightSpace);
		sbRightSpace.setMax(255);
		sbRightSpace.setOnSeekBarChangeListener(this);

		tvLineHeight = (TextView) findViewById(R.id.tvLineHeight);
		tvRightSpace = (TextView) findViewById(R.id.tvRightSpace);

		mHandler = new MHandler(this);
		DrawerService.addHandler(mHandler);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateSetAndShowUI();
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
		case R.id.btPrintEnglish:
		case R.id.btPrintChinese:
		case R.id.btPrintEwen: {
			// 不要直接和Pos打交道，要通过workThread来交流
			Map<String, Charset> charsetMap = Charset.availableCharsets();
			Collection<Charset> charsetColl = charsetMap.values();
			Iterator<Charset> iter = charsetColl.iterator();
			for (int i = 0; i < charsetColl.size(); i++) {
				Log.v(TAG, iter.next().displayName());
			}
			if (DrawerService.workThread.isConnected()) {
				int charset = 0, codepage = 0;
				String text = "";
				String encoding = "";
				byte[] addBytes = new byte[0];
				if (arg0.getId() == R.id.btPrintEnglish) {
					text = strEnglish;
					encoding = "US-ASCII";
					charset = 0;
					codepage = 0;
				} else if (arg0.getId() == R.id.btPrintChinese) {
					text = strEnglish + strChinese;
					encoding = "GBK";
					charset = 15;
					codepage = 255;
				} else if (arg0.getId() == R.id.btPrintEwen) {
					charset = 0;
					codepage = 6;
					addBytes = DataUtils.byteArraysToBytes(new byte[][] {
							{ 0x1c, 0x2e }, EncodeToCP866(strEwen),{ 0x1b, 0x40} });
				}

				Bundle dataCP = new Bundle();
				Bundle dataAlign = new Bundle();
				Bundle dataRightSpace = new Bundle();
				Bundle dataLineHeight = new Bundle();
				Bundle dataTextOut = new Bundle();
				Bundle dataWrite = new Bundle();
				dataCP.putInt(Global.INTPARA1, charset);
				dataCP.putInt(Global.INTPARA2, codepage);
				dataAlign.putInt(Global.INTPARA1, nTextAlign);
				dataRightSpace.putInt(Global.INTPARA1, nRightSpace);
				dataLineHeight.putInt(Global.INTPARA1, nLineHeight);
				dataTextOut.putString(Global.STRPARA1, text);
				dataTextOut.putString(Global.STRPARA2, encoding);
				dataTextOut.putInt(Global.INTPARA1, 0);
				dataTextOut.putInt(Global.INTPARA2, nScaleTimesWidth);
				dataTextOut.putInt(Global.INTPARA3, nScaleTimesHeight);
				dataTextOut.putInt(Global.INTPARA4, nFontSize);
				dataTextOut.putInt(Global.INTPARA5, nFontStyle);
				dataWrite.putByteArray(Global.BYTESPARA1, addBytes);
				dataWrite.putInt(Global.INTPARA1, 0);
				dataWrite.putInt(Global.INTPARA2, addBytes.length);

				DrawerService.workThread.handleCmd(
						Global.CMD_POS_SETCHARSETANDCODEPAGE, dataCP);
				DrawerService.workThread.handleCmd(Global.CMD_POS_SALIGN,
						dataAlign);
				DrawerService.workThread.handleCmd(
						Global.CMD_POS_SETRIGHTSPACE, dataRightSpace);
				DrawerService.workThread.handleCmd(
						Global.CMD_POS_SETLINEHEIGHT, dataLineHeight);
				DrawerService.workThread.handleCmd(Global.CMD_POS_STEXTOUT,
						dataTextOut);
				DrawerService.workThread.handleCmd(Global.CMD_POS_WRITE,
						dataWrite);
				

			} else {
				Toast.makeText(this, "请先连接打印机", Toast.LENGTH_SHORT).show();
			}
			break;
		}

		case R.id.btFontSize: {

			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.fontsize)
					.setItems(R.array.fontsize,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.fontsize);
									btFontSize.setText(items[which]);
									nFontSize = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.btTextAlign: {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.textalign)
					.setItems(R.array.textalign,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.textalign);
									btTextAlign.setText(items[which]);
									nTextAlign = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.btScaleTimesWidth: {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.width)
					.setItems(R.array.scaletimes_width_max2,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.scaletimes_width_max2);
									btScaleTimesWidth.setText(items[which]);
									nScaleTimesWidth = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.btScaleTimesHeight: {

			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.height)
					.setItems(R.array.scaletimes_height_max2,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.scaletimes_height_max2);
									btScaleTimesHeight.setText(items[which]);
									nScaleTimesHeight = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.cbBlackWhiteReverse:
		case R.id.cbBold:
		case R.id.cbUpsideDown:
		case R.id.cbTurnRight90:
		case R.id.cbUnderLine1:
		case R.id.cbUnderLine2: {
			updateFontStyle();
			break;
		}

		}
	}

	private void updateFontStyle() {
		nFontStyle = Cmd.Constant.FONTSTYLE_NORMAL;
		if (cbBlackWhiteReverse.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_BLACKWHITEREVERSE;
		if (cbBold.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_BOLD;
		if (cbUpsideDown.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_UPSIDEDOWN;
		if (cbTurnRight90.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_TURNRIGHT90;
		if (cbUnderLine1.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_UNDERLINE1;
		if (cbUnderLine2.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_UNDERLINE2;

	}

	static class MHandler extends Handler {

		WeakReference<FormatTextActivity> mActivity;

		MHandler(FormatTextActivity activity) {
			mActivity = new WeakReference<FormatTextActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			FormatTextActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_POS_STEXTOUTRESULT:
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

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
		case R.id.sbLineHeight: {
			nLineHeight = progress;
			tvLineHeight.setText(getString(R.string.lineheight) + "\n"
					+ nLineHeight);
			break;
		}

		case R.id.sbRightSpace: {
			nRightSpace = progress;
			tvRightSpace.setText(getString(R.string.rightspace) + "\n"
					+ nRightSpace);
			break;
		}
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	public boolean onLongClick(View arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	private void updateSetAndShowUI() {
		// Configue

		btFontSize
				.setText(getResources().getStringArray(R.array.fontsize)[nFontSize]);
		btTextAlign
				.setText(getResources().getStringArray(R.array.textalign)[nTextAlign]);
		btScaleTimesWidth.setText(getResources().getStringArray(
				R.array.scaletimes_width_max2)[nScaleTimesWidth]);
		btScaleTimesHeight.setText(getResources().getStringArray(
				R.array.scaletimes_height_max2)[nScaleTimesHeight]);

		cbBlackWhiteReverse
				.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_BLACKWHITEREVERSE) != 0);
		cbBold.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_BOLD) != 0);
		cbUpsideDown
				.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_UPSIDEDOWN) != 0);
		cbTurnRight90
				.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_TURNRIGHT90) != 0);
		cbUnderLine1
				.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_UNDERLINE1) != 0);
		cbUnderLine2
				.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_UNDERLINE2) != 0);

		tvLineHeight.setText("行高\n" + nLineHeight);
		sbLineHeight.setProgress(nLineHeight);
		tvRightSpace.setText("右间距\n" + nRightSpace);
		sbRightSpace.setProgress(nRightSpace);
	}

	static byte[] EncodeToCP866(String data) {
		char[] alphaChars = data.toCharArray();
		byte[] retval = new byte[alphaChars.length];

		for (int i = 0; i < alphaChars.length; i++) {
			char chr = alphaChars[i];
			int ch = chr;

			if (ch >= 0x0410 && ch <= 0x043f) // OK
				ch = ch - 0x0410 + 0x80;
			else if (ch >= 0x0440 && ch <= 0x044f)
				ch = ch - 0x0440 + 0xe0;
			else if (ch == 0x0401) // Ё
				ch = 0xf0;
			else if (ch == 0x0451) // ё
				ch = 0xf1;
			else if (ch == 0x0407) // Й
				ch = 0xf4;
			else if (ch == 0x0457) // й
				ch = 0xf5;
			else if (ch == 0x040e) // Ю
				ch = 0xf6;
			else if (ch == 0x045e) // ю
				ch = 0xf7;

			retval[i] = (byte) ch;
		}
		return retval;
	}

}
