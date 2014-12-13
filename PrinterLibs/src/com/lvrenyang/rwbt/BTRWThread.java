package com.lvrenyang.rwbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import com.lvrenyang.callback.RecvCallBack;
import com.lvrenyang.rwbuf.RxBuffer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class BTRWThread extends Thread {
	private static final String TAG = "BTRWThread";
	//private static final Object SLOCK = new Object(); // ���ж����Ĳ���������Ҫ����
	private static volatile BTRWThread btRWThread = null;

	private static final int BTRWHANDLER_READ = 1000;
	private static final UUID uuid = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private static Handler btrwHandler = null;
	private static Looper mLooper = null;
	private static boolean threadInitOK = false;

	private static BluetoothSocket s = null;
	private static DataInputStream is = null;
	private static DataOutputStream os = null;
	private static boolean isOpened = false;

	private static RecvCallBack callBack = null;
	private static final Object NULLLOCK = new Object();
	private static RxBuffer BTRXBuffer = new RxBuffer(0x1000);

	private BTRWThread() {
		threadInitOK = false;
	}

	public static BTRWThread InitInstant() {
		if (btRWThread == null) {
			synchronized (BTRWThread.class) {
				if (btRWThread == null) {
					btRWThread = new BTRWThread();
				}
			}
		}
		return btRWThread;
	}

	@Override
	public void start() {
		super.start();
		while (!threadInitOK)
			;
	}

	@Override
	public void run() {
		Looper.prepare();
		mLooper = Looper.myLooper();
		btrwHandler = new BTRWHandler();
		threadInitOK = true;
		Looper.loop();
	}

	private static class BTRWHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BTRWHANDLER_READ: {
				byte[] buffer = new byte[0x1000];
				int rec = 0;
				Log.i(TAG, "start read");
				while (true) {

					try {
						rec = ReadIsAvaliable(buffer, buffer.length);
						if (rec > 0) {
							for (int i = 0; i < rec; i++)
								BTRXBuffer.PutByte(buffer[i]);
							OnRecv(buffer, 0, rec);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}

					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
				}

				Close();
				break;
			}
			}
		}
	}

	public static boolean Open(String BTAddress) {
		boolean result = false;
		/*synchronized (SLOCK)*/ {
			result = _Open(BTAddress);
		}
		return result;
	}

	private static boolean _Open(String BTAddress) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter == null)
			return false;

		boolean valid = false;

		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTAddress);
//        try {
//            s = device.createRfcommSocketToServiceRecord(uuid);
//            s.connect();
//            os = new DataOutputStream(s.getOutputStream());
//            is = new DataInputStream(s.getInputStream());
//            valid = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
		Method m;
		try {
			m = device.getClass().getMethod("createRfcommSocket",
					new Class[] { int.class });
			try {
				s = (BluetoothSocket) m.invoke(device, 1);
				bluetoothAdapter.cancelDiscovery();
				try {
					s.connect();
					os = new DataOutputStream(s.getOutputStream());
					is = new DataInputStream(s.getInputStream());
					valid = true;
					Log.v("BTRWThread Open", "Connected to " + BTAddress);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// ���ɹ��ˣ����������
		if (valid) {
			isOpened = true;
			Message msg = btrwHandler.obtainMessage(BTRWHANDLER_READ);
			btrwHandler.sendMessage(msg);
		} else {
			_Close();
		}

		return valid;
	}

	public static boolean OpenOfficial(String BTAddress) {
		boolean result = false;
		/*synchronized (SLOCK)*/ {
			result = _OpenOfficial(BTAddress);
		}
		return result;
	}

	private static boolean _OpenOfficial(String BTAddress) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter == null)
			return false;

		boolean valid = false;

		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTAddress);
		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			// MY_UUID is the app's UUID string, also used by the server code
			s = device.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e) {
		}

		bluetoothAdapter.cancelDiscovery();

		try {
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			s.connect();
			os = new DataOutputStream(s.getOutputStream());
			is = new DataInputStream(s.getInputStream());
			valid = true;
			Log.v("BTRWThread OpenOfficial", "Connected to " + BTAddress);
		} catch (IOException connectException) {
			// Unable to connect; close the socket and get out
			try {
				s.close();
			} catch (IOException closeException) {
			}
		}

		// ���ɹ��ˣ����������
		if (valid) {
			isOpened = true;
			Message msg = btrwHandler.obtainMessage(BTRWHANDLER_READ);
			btrwHandler.sendMessage(msg);
		} else {
			isOpened = false;
			s = null;
		}

		return valid;
	}

	public static void Close() {
		/*synchronized (SLOCK)*/ {
			_Close();
		}
	}

	private static void _Close() {
		try {
			if (is != null) {
				is.close();
				is = null;
			}
			if (os != null) {
				os.close();
				os = null;
			}
			if (null != s) {
				s.close();
				s = null;
			}
			Log.v("BTRWThread Close", "Close BluetoothSocket");
		} catch (Exception e) {
			e.printStackTrace();
		}
		isOpened = false;
	}

	public static boolean IsOpened() {
		boolean ret = false;
		/*synchronized (SLOCK)*/ {
			ret = _IsOpened();
		}
		return ret;
	}

	private static boolean _IsOpened() {
		return isOpened;
	}

	public static int Write(byte[] buffer, int offset, int count) {
		int ret = 0;
		/*synchronized (SLOCK)*/ {
			ret = _Write(buffer, offset, count);
		}
		return ret;
	}

	private static int _Write(byte[] buffer, int offset, int count) {
		int cnt = 0;
		if (null != os) {
			try {
				os.write(buffer, offset, count);
				os.flush();
				cnt = count;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				_Close();
			}
		}

		return cnt;
	}

	/**
	 * 
	 * @param buffer
	 * @param byteOffset
	 * @param byteCount
	 * @param timeout
	 * @return ����ʵ�ʶ�ȡ���ֽ���
	 */
	public static synchronized int Read(byte[] buffer, int byteOffset,
			int byteCount, int timeout) {
		int index = 0;
		long time = System.currentTimeMillis();
		while ((System.currentTimeMillis() - time) < timeout) {
			if (!IsEmpty()) {
				buffer[index++] = GetByte();
			}

			if (index == byteCount)
				break;
		}

		return index;
	}

	private static int ReadIsAvaliable(byte[] buffer, int maxCount)
			throws IOException {
		int ret = 0;
		/*synchronized (SLOCK)*/ {
			ret = _ReadIsAvaliable(buffer, maxCount);
			if (ret > 0)
				OnRecv(buffer, 0, ret);
		}
		return ret;
	}

	private static int _ReadIsAvaliable(byte[] buffer, int maxCount)
			throws IOException {
		int available = 0;
		int rec = -1;

		if (null != is) {
			available = is.available();
			if (available > 0) {
				rec = is.read(buffer, 0, available > maxCount ? maxCount
						: available);
			}
		}
		return rec;
	}

	private static void OnRecv(byte[] buffer, int byteOffset, int byteCount) {
		synchronized (NULLLOCK) {
			if (null != callBack)
				callBack.onRecv(buffer, byteOffset, byteCount);
		}
	}

	public static void SetOnRecvCallBack(RecvCallBack callback) {
		synchronized (NULLLOCK) {
			callBack = callback;
		}
	}

	public static boolean Request(byte sendbuf[], int sendlen, int requestlen,
			byte recbuf[], Integer reclen, int timeout) {
		int Retry = 3;

		while ((Retry--) > 0) {
			ClrRec();
			Write(sendbuf, 0, sendlen);
			reclen = Read(recbuf, 0, requestlen, timeout);
			if (requestlen == reclen)
				return true;
		}
		return false;
	}

	public static void ClrRec() {
		BTRXBuffer.ClrRec();
	}

	public static boolean IsEmpty() {
		return BTRXBuffer.IsEmpty();
	}

	public static byte GetByte() {
		return BTRXBuffer.GetByte();
	}

	public static synchronized void Quit() {
		try {
			if (null != mLooper) {
				mLooper.quit();
				mLooper = null;
			}
			btRWThread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
