package com.lvrenyang.utils;

import java.io.File;

import android.os.Environment;

/**
 * 外部和库通讯需要使用的消息都在这里定义。
 * 
 * @author Administrator
 * 
 */
public class Constant {

	public static final int MSG_NETHEARTBEATTHREAD_UPDATESTATUS = 100100;
	public static final int MSG_BTHEARTBEATTHREAD_UPDATESTATUS = 100200;
	public static final int MSG_USBHEARTBEATTHREAD_UPDATESTATUS = 100300;

	public static final String dumpfile = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "dump.txt";
}
