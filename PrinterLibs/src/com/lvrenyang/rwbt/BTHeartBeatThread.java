package com.lvrenyang.rwbt;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.lvrenyang.utils.Constant;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class BTHeartBeatThread extends Thread {

	private static volatile BTHeartBeatThread btHeartBeatThread = null;

	private static final int BTHEARTBEATHANDLER_STARTUP = 1000;

	private static Handler targetHandler = null;
	private static Handler btHeartBeatHandler = null;
	private static Looper mLooper = null;
	private static boolean threadInitOK = false;

	// private static final Object RWLOCK = new Object();
	private static boolean pause = false;
	private static final Lock heartLock = new ReentrantLock();

	private static int btStatusOK = 0;
	private static int btStatus = 0;

	public BTHeartBeatThread(Handler mHandler) {
		targetHandler = mHandler;
	}

	public static BTHeartBeatThread InitInstant(Handler mHandler) {
		if (btHeartBeatThread == null) {
			synchronized (BTHeartBeatThread.class) {
				if (btHeartBeatThread == null) {
					btHeartBeatThread = new BTHeartBeatThread(mHandler);
				}
			}
		}
		return btHeartBeatThread;
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
		btHeartBeatHandler = new BTHeartBeatHandler();
		threadInitOK = true;
		Looper.loop();
	}

	private static class BTHeartBeatHandler extends Handler {

		private void SendOutStatus() {
			Message smsg = targetHandler
					.obtainMessage(Constant.MSG_BTHEARTBEATTHREAD_UPDATESTATUS);
			smsg.arg1 = btStatusOK;
			smsg.arg2 = btStatus;
			targetHandler.sendMessage(smsg);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/**
			 * BT 做心跳数据包用10 04 01命令
			 */
			case BTHEARTBEATHANDLER_STARTUP: {
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
						BTRWThread.ClrRec();// 只调用clearrec也需要这个锁，调用read也需要这个锁
						sendlen = BTRWThread.Write(buffer, 0, buffersize);
						reclen = BTRWThread.Read(rec, 0, 1, timeout);
					} finally {
						heartLock.unlock();
					}

					if (sendlen != buffersize) // 写出错，流的问题。直接break
					{
						btStatusOK = 0;
						btStatus = 0;
						SendOutStatus();
						break;
					}

					if (reclen == 1) {
						curUnrespond = 0;
						btStatusOK = 1;
						btStatus = rec[0];
					} else {
						curUnrespond++;
						btStatusOK = 0;
						btStatus = 0;
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
		Message msg = btHeartBeatHandler
				.obtainMessage(BTHEARTBEATHANDLER_STARTUP);
		btHeartBeatHandler.sendMessage(msg);
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
			btHeartBeatThread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
