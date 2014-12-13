package com.lvrenyang.myactivity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.lvrenyang.myprinter.DrawerService;
import com.lvrenyang.myprinter.Global;
import com.lvrenyang.myprinter.R;

public class PictureActivity extends Activity implements OnClickListener {

	private static final int RESULT_LOAD_IMAGE = 1;
	private Button btPrintPicture;
	private ImageView imageViewPicture;
	private RadioButton rbPaperWidth2,rbPaperWidth3,rbPaperWidth4;

	private static Handler mHandler = null;
	private static String TAG = "CheckKeyActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);

		btPrintPicture = (Button) findViewById(R.id.buttonPrintPicture);
		btPrintPicture.setOnClickListener(this);
		imageViewPicture = (ImageView) findViewById(R.id.imageViewPicture);
		imageViewPicture.setOnClickListener(this);
		rbPaperWidth2 = (RadioButton) findViewById(R.id.radioButton2Inch);
		rbPaperWidth3 = (RadioButton) findViewById(R.id.radioButton3Inch);
		rbPaperWidth4 = (RadioButton) findViewById(R.id.radioButton4Inch);

		Bitmap bm = getImageFromAssetsFile("yellowmen.png");
		if (null != bm) {
			imageViewPicture.setImageBitmap(bm);
		}

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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaColumns.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(picturePath, opts);
			opts.inJustDecodeBounds = false;
			if (opts.outWidth > 1200) {
				opts.inSampleSize = opts.outWidth / 1200;
			}
			Bitmap bitmap = BitmapFactory.decodeFile(picturePath, opts);
			if (null != bitmap) {
				imageViewPicture.setImageBitmap(bitmap);
			}

		}

	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.buttonPrintPicture: {

			Bitmap mBitmap = ((BitmapDrawable) imageViewPicture.getDrawable())
					.getBitmap();
			
			int nPaperWidth = 384;
			if(rbPaperWidth2.isChecked())
				nPaperWidth = 384;
			else if (rbPaperWidth3.isChecked())
				nPaperWidth = 576;
			else if (rbPaperWidth4.isChecked())
				nPaperWidth = 832;
			
			if (mBitmap != null) {
				if (DrawerService.workThread.isConnected()) {
					Bundle data = new Bundle();
					//data.putParcelable(Global.OBJECT1, mBitmap);
					data.putParcelable(Global.PARCE1, mBitmap);
					data.putInt(Global.INTPARA1, nPaperWidth);
					data.putInt(Global.INTPARA2, 0);
					DrawerService.workThread.handleCmd(
							Global.CMD_POS_PRINTPICTURE, data);
				} else {
					Toast.makeText(this, "请先连接打印机", Toast.LENGTH_SHORT).show();
				}
			}

			break;
		}

		case R.id.imageViewPicture: {
			Intent i = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, RESULT_LOAD_IMAGE);
			break;
		}
		}
	}

	static class MHandler extends Handler {

		WeakReference<PictureActivity> mActivity;

		MHandler(PictureActivity activity) {
			mActivity = new WeakReference<PictureActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			PictureActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_POS_PRINTPICTURERESULT: {
				int result = msg.arg1;
				Toast.makeText(theActivity, (result == 1) ? "成功" : "失败",
						Toast.LENGTH_SHORT).show();
				Log.v(TAG, "Result: " + result);
				break;
			}

			}
		}
	}

	/**
	 * 从Assets中读取图片
	 */
	private Bitmap getImageFromAssetsFile(String fileName) {
		Bitmap image = null;
		AssetManager am = getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return image;

	}

}
