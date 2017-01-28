package scu.cd.sc;
/**
 * 主界面
 * author:    杨智                                                    张健  
 * e-mail:  youngtze_2014@163.com  876424571@qq.com
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class MainActivity extends Activity {

	private boolean mConnected = false;
	private GameView gameView = null;
	private static final int MAIN_GROUP = 1;
	private static final int ADD_PLAYER = 11;

	// Debugging
	private static final String TAG = "BluetoothChat";
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

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 17;
	private static final int REQUEST_ENABLE_BT = 2;
	
	
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothService mChatService = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		// Get local Bluetooth adapter
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 全屏显示
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 获取屏幕宽高
		Display display = getWindowManager().getDefaultDisplay();
		// 现实GameView
		GameView.init(this, display.getWidth(), display.getHeight());
		gameView = GameView.getInstance();
		setContentView(gameView);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	public boolean getConnectState() {
		return mConnected;
	}

	public void onReturn() {
		new AlertDialog.Builder(this)
				.setTitle("退出游戏")
				.setMessage("确定退出游戏？")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						BluetoothAdapter ba_0 = BluetoothAdapter
								.getDefaultAdapter();
						if (ba_0 != null) {
							if (ba_0.isEnabled()) // 如果蓝牙是打开的，则关闭
								ba_0.disable();
						}
						System.exit(0);
					}
				})
				.setNegativeButton("否", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						return;
					}
				}).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			onReturn();
			break;
		case KeyEvent.KEYCODE_MENU:
			super.openOptionsMenu();
			break;
		}
		// return super.onKeyDown(keyCode, event);
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothService(this, mHandler);
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	public void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// mChatService.write(send);
			byte[] send = message.getBytes();
			mChatService.writeString(message.getBytes());
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					mConnected = true;
					Toast.makeText(getApplicationContext(), "已经连接成功！",
							Toast.LENGTH_SHORT).show();

					break;
				case BluetoothService.STATE_CONNECTING:
					// mTitle.setText(R.string.title_connecting);
					Toast.makeText(getApplicationContext(), "正在连接....",
							Toast.LENGTH_SHORT).show();
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				String writeMessage = new String(writeBuf);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, readBuf.length);
				if (readMessage != null) {
					String[] strArr = readMessage.split(",");
					if (strArr[0].equals("下棋")) {
						gameView.setPlay(true);
						int x = Integer.parseInt(strArr[2]);
						int y = Integer.parseInt(strArr[3]);
						gameView.mGameMap[y][x] = Integer.parseInt(strArr[1]);
						if (Integer.parseInt(strArr[1]) == GameView.CAMP_BLACK)
							gameView.mCampTurn = GameView.CAMP_WHITE;
						else if (Integer.parseInt(strArr[1]) == GameView.CAMP_WHITE)
							gameView.mCampTurn = GameView.CAMP_BLACK;
//						Toast.makeText(getApplicationContext(), readMessage,
//								Toast.LENGTH_SHORT).show();
					} else if (strArr[0].equals("悔棋请求")) {
						final int x = Integer.parseInt(strArr[1]);
						final int y = Integer.parseInt(strArr[2]);
						new AlertDialog.Builder(MainActivity.this)
								.setTitle(strArr[0])
								.setMessage("允许对方悔棋吗？")
								.setPositiveButton("允许",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub
												MainActivity.this
														.sendMessage("悔棋回复"
																+ "," + "允许悔棋");
												gameView.mGameMap[y][x] = GameView.CAMP_DEFAULT;
												gameView.setPlay(false);
											}
										})
								.setNegativeButton("不允许",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub
												MainActivity.this
														.sendMessage("悔棋回复"
																+ "," + "不允许悔棋");
											}
										}).show();
					} else if (strArr[0].equals("悔棋回复")) {
						if (strArr[1].equals("允许悔棋")) {
							gameView.setPlay(true);
							Toast.makeText(MainActivity.this, "对方允许悔棋，悔棋成功",
									Toast.LENGTH_SHORT).show(); // 悔棋成功
							gameView.mGameMap[gameView.getLastY()][gameView
									.getLastX()] = GameView.CAMP_DEFAULT;
						} else
							Toast.makeText(MainActivity.this, "对方不允许悔棋，悔棋失败",
									Toast.LENGTH_SHORT).show(); // 悔棋成功
					} else if (strArr[0].equals("禁止悔棋")) {
						gameView.setUndo(false);
					} else if (strArr[0].equals("请求重玩")) {
						new AlertDialog.Builder(MainActivity.this)
								.setTitle(strArr[0])
								.setMessage("接受对方重玩请求吗？")
								.setPositiveButton("接受",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub
												gameView.onRePlay();
												MainActivity.this
														.sendMessage("重玩回复"
																+ "," + "同意重玩");
											}
										})
								.setNegativeButton("不接受",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												MainActivity.this
														.sendMessage("重玩回复"
																+ "," + "不同意重玩");
											}
										}).show();
					} else if (strArr[0].equals("重玩回复")) {
						if (strArr[1].equals("同意重玩")) {
							gameView.onRePlay();
							Toast.makeText(MainActivity.this, "对方同意重玩，重新开始",
									Toast.LENGTH_SHORT).show();

						} else
							Toast.makeText(MainActivity.this, "对方不同意重玩，游戏继续",
									Toast.LENGTH_SHORT).show();
					} else if (strArr[0].equals("本局结束")) {
						if (strArr[1].equals("黑方胜")) {
							gameView.setBlackWin();
							gameView.mCampWinner = R.string.Role_black;

							new AlertDialog.Builder(MainActivity.this)
									.setTitle("继续游戏")
									.setMessage(
											GameView.sResources
													.getString(gameView.mCampWinner)
													+ "胜利" + "," + "是否继续游戏？")
									.setPositiveButton(
											"是",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													// TODO Auto-generated
													// method stub
													gameView.onRePlay();
													gameView.setPlay(false);
						/////////////////////////////////////////////////////////////////////							
												}
											})
									.setNegativeButton(
											"否",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													System.exit(0);
												}
											}).show();

						} else if (strArr[1].equals("白方胜")) {
							gameView.setWhiteWin();
							gameView.mCampWinner = R.string.Role_white;
							
							new AlertDialog.Builder(MainActivity.this)
									.setTitle("继续游戏")
									.setMessage(
											GameView.sResources
													.getString(gameView.mCampWinner)
													+ "胜利" + "," + "是否继续游戏？")
									.setPositiveButton(
											"是",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													// TODO Auto-generated
													// method stub
													gameView.onRePlay();
													gameView.setPlay(false);
			////////////////////////////////////////////////////////////////////////////////////
												}
											})
									.setNegativeButton(
											"否",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													System.exit(0);
												}
											}).show();

						}
					} else if (strArr[1].equals("平局")) {
						gameView.setTie();
						gameView.mCampWinner = R.string.Role_tie;
						new AlertDialog.Builder(MainActivity.this)
								.setTitle("继续游戏")
								.setMessage("平局" + "," + "是否继续游戏？")
								.setPositiveButton("是",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub
												gameView.onRePlay();
											}
										})
								.setNegativeButton("否",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												System.exit(0);
											}
										}).show();
					}
				}
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnected = true;
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
			gameView.invalidate();
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(MAIN_GROUP, ADD_PLAYER, 0, "寻找玩家");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case ADD_PLAYER: // 导找玩家
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}