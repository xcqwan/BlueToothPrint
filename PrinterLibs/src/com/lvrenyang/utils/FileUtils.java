package com.lvrenyang.utils;

import java.io.File;
import java.io.RandomAccessFile;

public class FileUtils {
	/**
	 * 将数据存到文件
	 */
	public static void AddToFile(byte[] buffer, int byteOffset, int byteCount,
			String dumpfile) {
		if (null == dumpfile)
			return;
		if (null == buffer)
			return;
		if (byteOffset < 0 || byteCount <= 0)
			return;

		// 每次写入时，都换行写
		try {
			File file = new File(dumpfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(file.length());
			raf.write(buffer, byteOffset, byteCount);
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void AddToFile(String text, String dumpfile) {
		if (null == dumpfile)
			return;
		if (null == text)
			return;
		if ("".equals(text))
			return;

		try {
			File file = new File(dumpfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(file.length());
			raf.write(text.getBytes());
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
