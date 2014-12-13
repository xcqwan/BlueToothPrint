package com.lvrenyang.rwusb;

import java.io.IOException;

import com.lvrenyang.callback.RecvCallBack;
import com.lvrenyang.rwbuf.RxBuffer;
import com.lvrenyang.rwusb.PL2303Driver.TTYTermios;
import com.lvrenyang.rwusb.USBDriver.RTNCode;
import com.lvrenyang.rwusb.USBDriver.USBPort;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * 这只是单纯处理读数据的线程，如果需要执行工作，还需要另外的线程
 * 这里面启动心跳线程，构造函数的时候new，start的时候也start子线程，quit的时候也
 * 
 * @author Administrator
 * 
 */
public class USBRWThread extends Thread {

	private static final Object SLOCK = new Object(); // 读写互斥锁，内部使用。
	private static volatile USBRWThread rwThread = null;

	private static final int RWHANDLER_READ = 1000;

	private static Handler rwHandler = null;
	private static Looper mLooper = null;
	private static boolean threadInitOK = false;

	private static PL2303Driver pl2303 = new PL2303Driver();
	private static USBPort port = null;
	private static TTYTermios serial = null;
	private static boolean isOpened = false;

	private static RecvCallBack callBack = null;
	private static final Object NULLLOCK = new Object();
	public static RxBuffer RXBuffer = new RxBuffer(0x1000);

	private USBRWThread() {
		threadInitOK = false;
	}

	public static USBRWThread InitInstant() {
		if (rwThread == null) {
			synchronized (USBRWThread.class) {
				if (rwThread == null) {
					rwThread = new USBRWThread();
				}
			}
		}
		return rwThread;
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
		rwHandler = new RWHandler();
		threadInitOK = true;
		Looper.loop();
	}

	private static class RWHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RWHANDLER_READ: {
				byte[] buffer = new byte[0x40];
				int rec = 0;

				while (true) {

					try {
						rec = ReadIsAvaliable(buffer, buffer.length);
						if (rec > 0) {
							for (int i = 0; i < rec; i++)
								RXBuffer.PutByte(buffer[i]);
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

					/* USB没有avaliable，所以这里通过这种方法来模拟出这种情况 */
					if (rec > 0)
						continue;

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

	public static boolean Open(USBPort port, TTYTermios serial) {
		boolean result = false;
		synchronized (SLOCK) {
			result = _Open(port, serial);
		}
		return result;
	}

	private static boolean _Open(USBPort port, TTYTermios serial) {
		boolean valid = false;

		try {
			if (pl2303.probe(port, PL2303Driver.id) == RTNCode.OK) {
				if (pl2303.attach(port) == RTNCode.OK) {
					if (pl2303.open(port, serial) == RTNCode.OK) {
						USBRWThread.port = port;
						USBRWThread.serial = serial;
						valid = true;
					}
				}
			}
			valid = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			valid = false;
		}

		// 如果成功了，则发起读命令

		if (valid) {
			isOpened = true;
			Message msg = rwHandler.obtainMessage(RWHANDLER_READ);
			rwHandler.sendMessage(msg);
		} else {
			isOpened = false;
		}

		return valid;
	}

	public static void Close() {
		synchronized (SLOCK) {
			_Close();
		}
	}

	private static void _Close() {
		try {
			pl2303.close(port, serial);
			pl2303.release(port);
			pl2303.disconnect(port);
			port = null;
			serial = null;
			Log.v("NETRWThread Close", "Close Socket");
		} catch (Exception e) {
			e.printStackTrace();
		}
		isOpened = false;
	}

	public static boolean IsOpened() {
		boolean ret = false;
		synchronized (SLOCK) {
			ret = _IsOpened();
		}
		return ret;
	}

	private static boolean _IsOpened() {
		return isOpened;
	}

	public static int Write(byte[] buffer, int offset, int count) {
		int ret = 0;
		synchronized (SLOCK) {
			ret = _Write(buffer, offset, count);
		}
		return ret;
	}

	private static int _Write(byte[] buffer, int offset, int count) {
		int cnt = 0;
		try {
			cnt = pl2303.write(port, buffer, offset, count, 2000);
			if (cnt < 0) {
				cnt = 0;
				throw new Exception("write error");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			_Close();
		}

		return cnt;
	}

	/**
	 * 
	 * @param buffer
	 * @param byteOffset
	 * @param byteCount
	 * @param timeout
	 * @return 返回实际读取的字节数
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
		synchronized (SLOCK) {
			ret = _ReadIsAvaliable(buffer, maxCount);
		}
		return ret;
	}

	private static int _ReadIsAvaliable(byte[] buffer, int maxCount)
			throws IOException {
		int rec;

		rec = pl2303.read(port, buffer, 0, maxCount, 1);
		if (rec < 0)
			rec = 0;

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
		RXBuffer.ClrRec();
	}

	public static boolean IsEmpty() {
		return RXBuffer.IsEmpty();
	}

	public static byte GetByte() {
		return RXBuffer.GetByte();
	}

	public static synchronized void Quit() {
		try {
			if (null != mLooper) {
				mLooper.quit();
				mLooper = null;
			}
			rwThread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
