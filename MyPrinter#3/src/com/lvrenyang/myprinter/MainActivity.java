package com.lvrenyang.myprinter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lvrenyang.myactivity.BarcodeActivity;
import com.lvrenyang.myactivity.ConnectBTMacActivity;
import com.lvrenyang.myactivity.ConnectBTPairedActivity;
import com.lvrenyang.myactivity.FormActivity;
import com.lvrenyang.myactivity.FormatTextActivity;
import com.lvrenyang.myactivity.PictureActivity;
import com.lvrenyang.myactivity.QrcodeActivity;
import com.lvrenyang.utils.DataUtils;

public class MainActivity extends Activity implements OnClickListener {

	private ViewPager viewPager;// 页卡内容
	private ImageView imageView;// 动画图片
	private TextView textView1, textView2;
	private List<View> views;// Tab页面列表
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private final int pageCount = 2;
	private View view1, view2;// 各个页卡
	private ProgressBar progressBar;
	private static Handler mHandler = null;
	private static String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myprinter_main);
		InitImageView();
		InitTextView();
		InitViewPager();

		progressBar = (ProgressBar) findViewById(R.id.progressBarStatus);
		progressBar.setMax(100);

		mHandler = new MHandler(this);
		DrawerService.addHandler(mHandler);

		Intent intent = new Intent(this, DrawerService.class);
		startService(intent);

		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DrawerService.delHandler(mHandler);
		mHandler = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if (DrawerService.workThread.isConnecting()) {
			Toast.makeText(this, "please waiting for connecting finished!",
					Toast.LENGTH_SHORT).show();
			return true;
		}

		int id = item.getItemId();
		if (id == R.id.menu_exit) {
			stopService(new Intent(this, DrawerService.class));
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void InitViewPager() {
		viewPager = (ViewPager) findViewById(R.id.vPager);
		views = new ArrayList<View>();
		LayoutInflater inflater = getLayoutInflater();
		view1 = inflater.inflate(R.layout.lay1, null);
        view2 = inflater.inflate(R.layout.lay4, null);
		view1.findViewById(R.id.btPicture).setOnClickListener(this);
		view1.findViewById(R.id.btFormatText).setOnClickListener(this);
		view1.findViewById(R.id.btForm).setOnClickListener(this);
		view1.findViewById(R.id.btBarcode).setOnClickListener(this);
		view1.findViewById(R.id.btQrcode).setOnClickListener(this);
        view2.findViewById(R.id.btConnectPrinterMac).setOnClickListener(this);
        view2.findViewById(R.id.btConnectPrinterPaired).setOnClickListener(this);
        view2.findViewById(R.id.btTranspond).setOnClickListener(this);
		views.add(view1);
		views.add(view2);
		viewPager.setAdapter(new MyViewPagerAdapter(views));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/**
	 * 初始化头标
	 */

	private void InitTextView() {
		textView1 = (TextView) findViewById(R.id.text1);
        textView2 = (TextView) findViewById(R.id.text4);
		textView1.setOnClickListener(new MyOnClickListener(0));
        textView2.setOnClickListener(new MyOnClickListener(1));

	}

	/***
	 * 初始化动画
	 */
	private void InitImageView() {
		imageView = (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.a)
				.getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / pageCount - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		imageView.setImageMatrix(matrix);// 设置动画初始位置
	}

	/***
	 * 头标点击监听
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyOnClickListener implements OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		public void onClick(View v) {
			viewPager.setCurrentItem(index);
		}

	}

	public class MyViewPagerAdapter extends PagerAdapter {
		private List<View> mListViews;

		public MyViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mListViews.get(position), 0);
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		public void onPageSelected(int arg0) {
			Animation animation = new TranslateAnimation(one * currIndex, one
					* arg0, 0, 0);
			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			imageView.startAnimation(animation);
			Log.v(TAG, "您选择了" + viewPager.getCurrentItem() + "页卡");
		}

	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onClick:" + arg0.toString());
		switch (arg0.getId()) {
            case R.id.btPicture:
                startActivity(new Intent(this, PictureActivity.class));
                break;
            case R.id.btFormatText:
                startActivity(new Intent(this, FormatTextActivity.class));
                break;
            case R.id.btForm:
                startActivity(new Intent(this, FormActivity.class));
                break;
            case R.id.btBarcode:
                startActivity(new Intent(this, BarcodeActivity.class));
                break;
            case R.id.btQrcode:
                startActivity(new Intent(this, QrcodeActivity.class));
                break;
            case R.id.btConnectPrinterMac:
                startActivity(new Intent(this, ConnectBTMacActivity.class));
                break;
            case R.id.btConnectPrinterPaired:
                startActivity(new Intent(this, ConnectBTPairedActivity.class));
                break;
            case R.id.btTranspond:
                //转发页面
                startActivity(new Intent(this, TranspondActivity.class));
                break;
        }
	}

	static class MHandler extends Handler {

		WeakReference<MainActivity> mActivity;

		MHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity theActivity = mActivity.get();
			switch (msg.what) {
			/**
			 * DrawerService 的 onStartCommand会发送这个消息
			 */
			case Global.MSG_ALLTHREAD_READY: {
				Log.v("MHandler", "MSG_ALLTHREAD_READY");
				if (DrawerService.workThread.isConnected()) {
					theActivity.progressBar.setIndeterminate(false);
					theActivity.progressBar.setProgress(100);
				} else {
					theActivity.progressBar.setIndeterminate(false);
					theActivity.progressBar.setProgress(0);
				}
				break;
			}

			case com.lvrenyang.utils.Constant.MSG_BTHEARTBEATTHREAD_UPDATESTATUS:
			case com.lvrenyang.utils.Constant.MSG_NETHEARTBEATTHREAD_UPDATESTATUS: {
				int statusOK = msg.arg1;
				int status = msg.arg2;
				Log.v(TAG,
						"statusOK: " + statusOK + " status: "
								+ DataUtils.byteToStr((byte) status));
				theActivity.progressBar.setIndeterminate(false);
				if (statusOK == 1)
					theActivity.progressBar.setProgress(100);
				else
					theActivity.progressBar.setProgress(0);

				break;
			}

			case Global.CMD_POS_PRINTPICTURERESULT: {
				int result = msg.arg1;
				Log.v(TAG, "Result: " + result);
				break;
			}
			}
		}
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				handleSendText(intent); // Handle text being sent
			} else if (type.startsWith("image/")) {
				handleSendImage(intent); // Handle single image being sent
			}
		}
	}

	private void handleSendText(Intent intent) {
		Uri textUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (textUri != null) {
			// Update UI to reflect text being shared
			try {
				if (DrawerService.workThread.isConnected()) {
					byte[] buffer = { 0x1b, 0x40, 0x1c, 0x26, 0x1b, 0x39, 0x01 }; // 设置中文，切换双字节编码。
					Bundle data = new Bundle();
					data.putByteArray(Global.BYTESPARA1, buffer);
					data.putInt(Global.INTPARA1, 0);
					data.putInt(Global.INTPARA2, buffer.length);
					DrawerService.workThread.handleCmd(Global.CMD_WRITE, data);
				}
				if (DrawerService.workThread.isConnected()) {
					FileInputStream fis = new FileInputStream(textUri.getPath());
					int length = fis.available();
					byte buffer[] = new byte[length];
					fis.read(buffer);
					Bundle data = new Bundle();
					data.putByteArray(Global.BYTESPARA1, buffer);
					data.putInt(Global.INTPARA1, 0);
					data.putInt(Global.INTPARA2, buffer.length);
					DrawerService.workThread.handleCmd(Global.CMD_WRITE, data);
					fis.close();
				} else {
					Toast.makeText(this, "请先连接打印机", Toast.LENGTH_SHORT).show();
				}

				finish();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			// Update UI to reflect image being shared
			Bitmap mBitmap = BitmapFactory
					.decodeFile(getRealPathFromURI(imageUri));
			if (mBitmap != null) {
				if (DrawerService.workThread.isConnected()) {
					Bundle data = new Bundle();
					data.putParcelable(Global.OBJECT1, mBitmap);
					data.putInt(Global.INTPARA1, 384);
					data.putInt(Global.INTPARA2, 0);
					DrawerService.workThread.handleCmd(
							Global.CMD_POS_PRINTPICTURE, data);
				} else {
					Toast.makeText(this, "请先连接打印机", Toast.LENGTH_SHORT).show();
				}
			}
			finish();
		}
	}

	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		CursorLoader loader = new CursorLoader(this, contentUri, proj, null,
				null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
}
