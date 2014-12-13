package com.lvrenyang.myprinter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class AppStart extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_private);

		new Handler().postDelayed(new Runnable() {

			public void run() {
				/* 启动蓝牙 */
				BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
				if (null != adapter) {
					if (!adapter.isEnabled()) {
						if (adapter.enable()) {
							// while(!adapter.isEnabled());
							Log.v("SearchBTAndConnectActivity",
									"Enable BluetoothAdapter");
						} else {
							finish();
							return;
						}
					}
				}

				/* 启动WIFI */
				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				switch (wifiManager.getWifiState()) {
				case WifiManager.WIFI_STATE_DISABLED:
					wifiManager.setWifiEnabled(true);
					break;
				default:
					break;
				}

				Intent intent = new Intent(AppStart.this, MainActivity.class);
				startActivity(intent);
				AppStart.this.finish();
			}

		}, 1000);
	}

}
