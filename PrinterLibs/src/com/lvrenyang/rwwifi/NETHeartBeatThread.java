package com.lvrenyang.rwwifi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.lvrenyang.utils.Constant;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 网络心跳数据包线程 不断发送10 04 01 读取状态
 * 
 * @author Administrator
 * 
 */
public class NETHeartBeatThread extends Thread {

	private static volatile NETHeartBeatThread netHeartBeatThread = null;

	private static final int NETHEARTBEATHANDLER_STARTUP = 1000;

	private static Handler targetHandler = null;
	private static Handler netHeartBeatHandler = null;
	private static Looper mLooper = null;
	private static boolean threadInitOK = false;

	//private static final Object RWLOCK = new Object();
	private static boolean pause = false;
	private static final Lock heartLock = new ReentrantLock();
	
	private static int netStatusOK = 0;
	private static int netStatus = 0;

	private NETHeartBeatThread(Handler mHandler) {
		targetHandler = mHandler;
	}

	public static NETHeartBeatThread InitInstant(Handler mHandler) {
		if (netHeartBeatThread == null) {
			synchronized (NETHeartBeatThread.class) {
				if (netHeartBeatThread == null) {
					netHeartBeatThread = new NETHeartBeatThread(mHandler);
				}
			}
		}
		return netHeartBeatThread;
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
		netHeartBeatHandler = new NETHeartBeatHandler();
		threadInitOK = true;
		Looper.loop();
	}

	private static class NETHeartBeatHandler extends Handler {

		private void SendOutStatus() {
			Message smsg = targetHandler
					.obtainMessage(Constant.MSG_NETHEARTBEATTHREAD_UPDATESTATUS);
			smsg.arg1 = netStatusOK;
			smsg.arg2 = netStatus;
			targetHandler.sendMessage(smsg);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/**
			 * TCP 做心跳数据包用10 04 01命令
			 */
			case NETHEARTBEATHANDLER_STARTUP: {
				byte[] buffer = { 0x10, 0x04, 0x01 };
				int buffersize = 3;
				int sendlen = 0;

				byte[] rec = { 0x00 };
				int reclen = 0;
				int timeout = 2000;

				int intertimeout = 2000;
				final int threshold = 3;
				int curUnrespond = 0;
				while (true) {
					try {
						Thread.sleep(intertimeout);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (pause)
						continue;

					try {
						heartLock.lock();
						NETRWThread.ClrRec();// 只调用clearrec也需要这个锁，调用read也需要这个锁
						sendlen = NETRWThread.Write(buffer, 0, buffersize);
						reclen = NETRWThread.Read(rec, 0, 1, timeout);
					} finally {
						heartLock.unlock();
					}

					if (sendlen != buffersize) // 写出错，流的问题。直接break
					{
						netStatusOK = 0;
						netStatus = 0;
						SendOutStatus();
						break;
					}

					if (reclen == 1) {
						curUnrespond = 0;
						netStatusOK = 1;
						netStatus = rec[0];
					} else {
						curUnrespond++;
						netStatusOK = 0;
						netStatus = 0;
					}

					SendOutStatus();

					if (curUnrespond >= threshold)
						break;
				}

				break;
			}
			}
		}
	}

	public static void BeginHeartBeat() {
		Message msg = netHeartBeatHandler
				.obtainMessage(NETHEARTBEATHANDLER_STARTUP);
		netHeartBeatHandler.sendMessage(msg);
	}

	public static void PauseHeartBeat() {
		pause = true;
		try {
			heartLock.lock();
		} finally {
			heartLock.unlock();
		}
	}

	public static void ResumeHeartBeat() {
		pause = false;
	}

	public static void Quit() {
		try {
			if (null != mLooper) {
				mLooper.quit();
				mLooper = null;
			}
			netHeartBeatThread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
