package com.lvrenyang.myprinter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.lvrenyang.pos.IO;

import java.io.UnsupportedEncodingException;

/**
 * Created by zombie on 10/16/14.
 */
public class TranspondActivity extends Activity {
    // Debugging
    private static final String TAG = "TranspondActivity";
    private static final boolean D = true;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private TextView mReadTxt;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transpond);
        mReadTxt = (TextView) findViewById(R.id.txt_read);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        if (mChatService == null) setupChat();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = null;
                    try {
                        readMessage = new String(readBuf, 0, msg.arg1, "GBK");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    System.out.print("接收的消息:\n" + readMessage);
                    mReadTxt.setText(getUnPosStr(readMessage));
                    String strQrcode = mReadTxt.getText().toString();
                    System.out.print("\n\n转发的内容:\n" + strQrcode);
                    if (strQrcode.length() == 0 || strQrcode.contains("null"))
                        return;
                    int nWidthX = 6;
                    int necl = 2;
                    String strCode = matchName(strQrcode) + "\n" + matchAddress(strQrcode) + "\n" + matchTel(strQrcode) + "\n" + matchPay(strQrcode);
                    if (DrawerService.workThread.isConnected()) {
                        Bundle data = new Bundle();
                        data.putString(Global.STRPARA1, strCode);
                        data.putInt(Global.INTPARA1, nWidthX);
                        data.putInt(Global.INTPARA2, necl);
                        DrawerService.workThread.handleCmd(Global.CMD_POS_SETQRCODE, data);
                    } else {
                        Toast.makeText(getApplication(), "请先连接打印机", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private String getUnPosStr(String a_pos) {
        byte[] ab_before = a_pos.getBytes();
        byte[] bs = new byte[ab_before.length];
        int len = 0;
        boolean isEscBefore = false;
        for (int i = 0; i<ab_before.length; i++) {
            byte b = ab_before[i];
            if (b == 1 || b == 0 || b == 16 || b == 4) {
                continue;
            }
            if (b == 27 || (b >= 17 && b <= 20 ) || b == 29) {
                //ESC DC1 分组符
                isEscBefore = true;
                continue;
            }
            if (isEscBefore && b == 'J') {
                continue;
            }
            if (isEscBefore) {
                isEscBefore = false;
                continue;
            }
            bs[len++] = b;
        }
        if (len == 0) {
            return "";
        }
        return new String(bs, 0, len);
    }

    private String matchTel(String content) {
        String[] parts = content.split("\n");
        StringBuilder sb = new StringBuilder("");
        for (String part : parts) {
            if (part.indexOf("电话:") >= 0) {
                if (part.indexOf("卖家电话") >= 0) {
                    continue;
                }
                sb.append(part);
            }
        }
        return sb.toString();
    }

    private String matchAddress(String content) {
        String[] parts = content.split("\n");
        StringBuilder sb = new StringBuilder("");
        for (String part : parts) {
            if (part.indexOf("地址:") >= 0) {
                if (part.indexOf("店铺地址") >= 0) {
                    continue;
                }
                sb.append(part);
            }
        }
        return sb.toString();
    }

    private String matchName(String content) {
        String[] parts = content.split("\n");
        StringBuilder sb = new StringBuilder("");
        for (String part : parts) {
            if (part.indexOf("买家:") >= 0 || part.indexOf("联系人:")  >= 0) {
                sb.append(part);
            }
        }
        return sb.toString();
    }

    private String matchPay(String content) {
        String[] parts = content.split("\n");
        StringBuilder sb = new StringBuilder("");
        for (String part : parts) {
            if (part.indexOf("实付:") >= 0 || part.indexOf("合计:") >= 0) {
                sb.append(part);
            }
        }
        return sb.toString();
    }
}
