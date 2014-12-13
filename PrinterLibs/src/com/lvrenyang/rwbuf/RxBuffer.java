package com.lvrenyang.rwbuf;

/**
 * 为了防止在读取或写入的时候，调用了清除，所以需要锁
 * 
 * @author Administrator
 * 
 */
public class RxBuffer {

	private final Object BUFLOCK = new Object();
	int RxSize;
	int Read, Write;
	byte Buffer[];

	public RxBuffer(int RX_SIZE) {
		Read = Write = 0;
		RxSize = RX_SIZE;
		Buffer = new byte[RxSize];
	}

	public byte GetByte() {
		byte ch;
		synchronized (BUFLOCK) {
			ch = _GetByte();
		}
		return ch;
	}

	public byte _GetByte() {
		byte ch;
		ch = Buffer[Read++];
		if (Read > (RxSize - 1))
			Read = 0;
		return (ch);
	}

	public void PutByte(byte ch) {
		synchronized (BUFLOCK) {
			_PutByte(ch);
		}
	}

	public void _PutByte(byte ch) {
		Buffer[Write++] = ch;
		if (Write > RxSize - 1)
			Write = 0;
	}

	public void ClrRec() {
		synchronized (BUFLOCK) {
			_ClrRec();
		}
	}

	public void _ClrRec() {
		Write = Read = 0;
	}

	public boolean IsEmpty() {
		return (Read == Write);
	}
}
