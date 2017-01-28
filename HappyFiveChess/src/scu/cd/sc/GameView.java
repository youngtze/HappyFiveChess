package scu.cd.sc;
/**
 * 游戏视图类
 * 
 */
import scu.cd.sc.R;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

public class GameView extends View  {

	// 定义常量
	public static final int ALIGN_TOP = 1;
	public static final int ALIGN_VCENTER = ALIGN_TOP << 1;
	public static final int ALIGN_LEFT = ALIGN_TOP << 2;
	public static final int ALIGN_RIGHT = ALIGN_TOP << 3;
	public static final int ALIGN_HCENTER = ALIGN_TOP << 4;
	public static final int ALIGN_BOTTOM = ALIGN_TOP << 5;
	public static final int GS_WAIT = 0;
	public static final int GS_INVITING = 1;
	public static final int GS_COMFIRE = 2;
	public static final int GS_DECLINE = 3;
	public static final int GS_GAME = 4;
	public static final int GS_END = 5;
	public static final int GS_AWAY = 6;
	public static final int GS_ERROR = 7;
	public static final int MAP_SPACE = 15;
	public static final int TILE_WIDTH = 24;
	public static final int TILE_HEIGHT = 25;
	public static final int CHESS_WIDTH = 9;
	public static final int CHESS_HEIGHT = 9;
	public static final int RADIUS_SPACE = TILE_WIDTH >> 1;
	public static final int CAMP_DEFAULT = 0;
	public static final int CAMP_BLACK = 1;
	public static final int CAMP_WHITE = 2;
	public static final int CALU_ALL_COUNT = 10;
	public static final int CALU_SINGLE_COUNT = 5;

	public static final int BOARDWIDTH = 320;
	public static final int BOARDHEIGHT = 480;
	public static final int LEFTPADDING = 20;
	public static final int RIGHTPADDING = 20;
	public static final int TOPPADDING = 20;
	public static final int BOTTOMPADDING = 20;
	public static final int CTRLHEIGHT = 160;

	// 最近一次的棋盘坐标
	private int mLastX = 0;
	private int mLastY = 0;

	// 悔棋标志
	private boolean mUndo = true;
	// 下棋标志
	private boolean mPlay = true;

	static GameView sInstance = null;

	public static void init(MainActivity mActivity, int screenWidth,
			int screenHeight) {
		sInstance = new GameView(mActivity, screenWidth, screenHeight);
	}

	public static GameView getInstance() {
		return sInstance;
	}

	boolean mbLoop = false;
	// 定义SurfaceHolder对象
	SurfaceHolder mSurfaceHolder = null;
	public static Paint sPaint = null;
	public static Canvas sCanvas = null;
	public static Resources sResources = null;
	private int mGameState = 0;
	private int mScreenWidth = 0;
	private int mScreenHeight = 0;
	public int[][] mGameMap = null;
	private int mMapHeightLengh = 0;
	private int mMapWidthLengh = 0;
	private int mMapIndexX = 0;
	private int mMapIndexY = 0;
	public int mCampTurn = 0;
	public int mCampWinner = 0;
	private float mTitleSpace_X = 0;
	private float mTitleSpace_Y = 0;
	private int mTitleHeight = 0;
	private float mTitleIndex_x = 0;
	private float mTitleIndex_y = 0;
	// 控制区域坐标
	private float mReplayX1 = 0;
	private float mReplayX2 = 0;
	private float mUndoX1 = 0;
	private float mUndoX2 = 0;
	private float mExitX1 = 0;
	private float mExitX2 = 0;

	private float mY1 = 0;
	private float mY2 = 0;

	// 数字坐标
	private float mWhiteNumLeftTopX = 80;
	private float mWhiteNumLeftTopY = 70;
	private float mWhiteNumGapX = 20;
	private float mWhiteNumGapY = 32;

	private float mBlackNumLeftTopX = 220;
	private float mBlackNumLeftTopY = 70;
	private float mBlackNumGapX = 20;
	private float mBlackNumGapY = 32;
	
	//黑白棋左上角位置
	private float mWhitePlayerLeftTopX = 10;
	private float mBlackPlayerLeftTopX = 290;
	private float mPlayerLeftTopY = 100;
	
	//黑白棋中心位置
	private float mWhitePlayerCenterX = 18;
	private float mBlackPlayerCenterX = 302;
	private float mPlayerCenterY = 110;
	
	

	private Bitmap bitmapBg = null;
	private Bitmap mBlack = null;
	private Bitmap mWhite = null;

	private Bitmap mBlank = null;

	private Bitmap mBlackShiNum = null;
	private Bitmap mBlackGeNum = null;
	private Bitmap mBlackZero = null;
	private Bitmap mBlackOne = null;
	private Bitmap mBlackTwo = null;
	private Bitmap mBlackThree = null;
	private Bitmap mBlackFour = null;
	private Bitmap mBlackFive = null;
	private Bitmap mBlackSix = null;
	private Bitmap mBlackSeven = null;
	private Bitmap mBlackEight = null;
	private Bitmap mBlackNine = null;

	private Bitmap mWhiteShiNum = null;
	private Bitmap mWhiteGeNum = null;
	private Bitmap mWhiteZero = null;
	private Bitmap mWhiteOne = null;
	private Bitmap mWhiteTwo = null;
	private Bitmap mWhiteThree = null;
	private Bitmap mWhiteFour = null;
	private Bitmap mWhiteFive = null;
	private Bitmap mWhiteSix = null;
	private Bitmap mWhiteSeven = null;
	private Bitmap mWhiteEight = null;
	private Bitmap mWhiteNine = null;

	// 记录当前的比赛情况
	private int mWhiteWin = 0; // 白方
	private int mWhiteTie = 0;
	private int mWhiteLose = 0;

	private int mBlackWin = 0; // 黑方
	private int mBlackTie = 0;
	private int mBlackLose = 0;

	MainActivity mActivity = null;

	private void getWhiteNum(int whiteNum) {
		int shi = whiteNum / 10;
		int ge = whiteNum % 10;
		switch (shi) {
		case 0:
			mWhiteShiNum = mBlank;
			break;
		case 1:
			mWhiteShiNum = mWhiteOne;
			break;
		case 2:
			mWhiteShiNum = mWhiteTwo;
			break;
		case 3:
			mWhiteShiNum = mWhiteThree;
			break;
		case 4:
			mWhiteShiNum = mWhiteFour;
			break;
		case 5:
			mWhiteShiNum = mWhiteFive;
			break;
		case 6:
			mWhiteShiNum = mWhiteSix;
			break;
		case 7:
			mWhiteShiNum = mWhiteSeven;
			break;
		case 8:
			mWhiteShiNum = mWhiteEight;
			break;
		case 9:
			mWhiteShiNum = mWhiteNine;
			break;
		}
		switch (ge) {
		case 0:
			mWhiteGeNum = mWhiteZero;
			break;
		case 1:
			mWhiteGeNum = mWhiteOne;
			break;
		case 2:
			mWhiteGeNum = mWhiteTwo;
			break;
		case 3:
			mWhiteGeNum = mWhiteThree;
			break;
		case 4:
			mWhiteGeNum = mWhiteFour;
			break;
		case 5:
			mWhiteGeNum = mWhiteFive;
			break;
		case 6:
			mWhiteGeNum = mWhiteSix;
			break;
		case 7:
			mWhiteGeNum = mWhiteSeven;
			break;
		case 8:
			mWhiteGeNum = mWhiteEight;
			break;
		case 9:
			mWhiteGeNum = mWhiteNine;
			break;
		}
	}

	private void getBlackNum(int blackNum) {
		int shi = blackNum / 10;
		int ge = blackNum % 10;
		switch (shi) {
		case 0:
			mBlackShiNum = mBlank;
			break;
		case 1:
			mBlackShiNum = mBlackOne;
			break;
		case 2:
			mBlackShiNum = mBlackTwo;
			break;
		case 3:
			mBlackShiNum = mBlackThree;
			break;
		case 4:
			mBlackShiNum = mBlackFour;
			break;
		case 5:
			mBlackShiNum = mBlackFive;
			break;
		case 6:
			mBlackShiNum = mBlackSix;
			break;
		case 7:
			mBlackShiNum = mBlackSeven;
			break;
		case 8:
			mBlackShiNum = mBlackEight;
			break;
		case 9:
			mBlackShiNum = mBlackNine;
			break;
		}
		switch (ge) {
		case 0:
			mBlackGeNum = mBlackZero;
			break;
		case 1:
			mBlackGeNum = mBlackOne;
			break;
		case 2:
			mBlackGeNum = mBlackTwo;
			break;
		case 3:
			mBlackGeNum = mBlackThree;
			break;
		case 4:
			mBlackGeNum = mBlackFour;
			break;
		case 5:
			mBlackGeNum = mBlackFive;
			break;
		case 6:
			mBlackGeNum = mBlackSix;
			break;
		case 7:
			mBlackGeNum = mBlackSeven;
			break;
		case 8:
			mBlackGeNum = mBlackEight;
			break;
		case 9:
			mBlackGeNum = mBlackNine;
			break;
		}
	}

	public GameView(MainActivity activity, int screenWidth, int screenHeight) {
		super(activity);
		sPaint = new Paint();
		sPaint.setAntiAlias(true);
		sResources = getResources();
		mActivity = activity;
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;

		mReplayX1 = (float) 20 / 320 * mScreenWidth;
		mReplayX2 = (float) 100 / 320 * mScreenWidth;
		mUndoX1 = (float) 120 / 320 * mScreenWidth;
		mUndoX2 = (float) 200 / 320 * mScreenWidth;
		mExitX1 = (float) 220 / 320 * mScreenWidth;
		mExitX2 = (float) 300 / 320 * mScreenWidth;

		mY1 = (float) 10 / 480 * mScreenHeight;
		mY2 = (float) 45 / 480 * mScreenHeight;

		mWhiteNumLeftTopX = (float) mWhiteNumLeftTopX / 320 * mScreenWidth; // 得到实际设备的娄字位置坐标
		mWhiteNumLeftTopY = (float) mWhiteNumLeftTopY / 480 * mScreenHeight;
		mWhiteNumGapX = (float) mWhiteNumGapX / 320 * mScreenWidth;
		mWhiteNumGapY = (float) mWhiteNumGapY / 480 * mScreenHeight;

		mBlackNumLeftTopX = (float) mBlackNumLeftTopX / 320 * mScreenWidth;
		mBlackNumLeftTopY = (float) mBlackNumLeftTopY / 480 * mScreenHeight;
		mBlackNumGapX = (float) mBlackNumGapX / 320 * mScreenWidth;
		mBlackNumGapY = (float) mBlackNumGapY / 480 * mScreenHeight;
		
		mWhitePlayerLeftTopX = mWhitePlayerLeftTopX/320*mScreenWidth;
		mBlackPlayerLeftTopX = mBlackPlayerLeftTopX/320*mScreenWidth;
		mPlayerLeftTopY = mPlayerLeftTopY/480*mScreenHeight;
		
		
		mWhitePlayerCenterX = mWhitePlayerCenterX/320*mScreenWidth;
		mBlackPlayerCenterX = mBlackPlayerCenterX/320*mScreenWidth;
		mPlayerCenterY = mPlayerCenterY/480*mScreenHeight;
		
		

		setFocusable(true);
		mbLoop = true;
		bitmapBg = CreatMatrixBitmap(R.drawable.chessboard, mScreenWidth,
				mScreenHeight);
		mBlack = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black);
		mWhite = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white);

		mBlank = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.blank);

		mBlackZero = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black_zero);
		mBlackOne = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black_one);
		mBlackTwo = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black_two);
		mBlackThree = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black_three);
		mBlackFour = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black_four);
		mBlackFive = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black_five);
		mBlackSix = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black_six);
		mBlackSeven = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black_seven);
		mBlackEight = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black_eight);
		mBlackNine = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.black_nine);

		mWhiteZero = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white_zero);
		mWhiteOne = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white_one);
		mWhiteTwo = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white_two);
		mWhiteThree = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white_three);
		mWhiteFour = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white_four);
		mWhiteFive = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white_five);
		mWhiteSix = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white_six);
		mWhiteSeven = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white_seven);
		mWhiteEight = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white_eight);
		mWhiteNine = BitmapFactory.decodeResource(GameView.sResources,
				R.drawable.white_nine);

		mTitleSpace_X = (float) mScreenWidth * (1 - (float) 40 / 320)
				/ (CHESS_WIDTH - 1); // x方向棋子与棋子之间的距离
		mTitleSpace_Y = (float) mScreenHeight
				* (1 - (float) 1 / 3 - (float) 40 / 480) / (CHESS_WIDTH - 1); // y方向棋子与棋子之间的距离
		mTitleHeight = (int) (mScreenHeight * ((float) 1 / 3)); // 控制界面的高度
		mTitleIndex_x = (float) mScreenWidth * ((float) 20 / 320); // 左上角棋子开始的位置
		mTitleIndex_y = mScreenHeight * ((float) 20 / 480); // 左上角棋子开始的位置
		setGameState(GS_GAME);
		invalidate();
	}

	public void setGameState(int newState) {
		mGameState = newState;
		switch (mGameState) {
		case GS_GAME:
			mGameMap = new int[CHESS_HEIGHT][CHESS_WIDTH];
			mMapHeightLengh = mGameMap.length;
			mMapWidthLengh = mGameMap[0].length;
			mCampTurn = CAMP_BLACK; // 黑子先下
			break;
		}
	}
	@Override
	protected void onDraw(Canvas canvas) {
		
		
		RenderGame(canvas);
		
		super.onDraw(canvas);
	}

	private void RenderGame(Canvas canvas) {
		switch (mGameState) {
		case GS_GAME:
			DrawRect(Color.WHITE, 0, 0, mScreenWidth, mScreenHeight,canvas);
			RenderMap(canvas);
		}
	}

	private void RenderMap(Canvas canvas) {
		int i, j;
		//画背景
		DrawImage(bitmapBg, 0, 0, 0,canvas);
		//画黑白双方
		DrawImage(mWhite, mWhitePlayerLeftTopX, mPlayerLeftTopY, 0,canvas);
		DrawImage(mBlack, mBlackPlayerLeftTopX, mPlayerLeftTopY, 0,canvas);
		// 显示当前比赛情况
		getWhiteNum(mWhiteWin);
		DrawImage(mWhiteShiNum, mWhiteNumLeftTopX, mWhiteNumLeftTopY, 0,canvas);
		DrawImage(mWhiteGeNum, mWhiteNumLeftTopX + mWhiteNumGapX,
				mWhiteNumLeftTopY, 0,canvas);
		getWhiteNum(mWhiteTie);
		DrawImage(mWhiteShiNum, mWhiteNumLeftTopX, mWhiteNumLeftTopY
				+ mWhiteNumGapY, 0,canvas);
		DrawImage(mWhiteGeNum, mWhiteNumLeftTopX + mWhiteNumGapX,
				mWhiteNumLeftTopY + mWhiteNumGapY, 0,canvas);
		getWhiteNum(mWhiteLose);
		DrawImage(mWhiteShiNum, mWhiteNumLeftTopX, mWhiteNumLeftTopY + 2
				* mWhiteNumGapY, 0,canvas);
		DrawImage(mWhiteGeNum, mWhiteNumLeftTopX + mWhiteNumGapX,
				mWhiteNumLeftTopY + 2 * mWhiteNumGapY, 0,canvas);

		getBlackNum(mBlackWin);
		DrawImage(mBlackShiNum, mBlackNumLeftTopX, mBlackNumLeftTopY, 0,canvas);
		DrawImage(mBlackGeNum, mBlackNumLeftTopX + mBlackNumGapX,
				mBlackNumLeftTopY, 0,canvas);
		getBlackNum(mBlackTie);
		DrawImage(mBlackShiNum, mBlackNumLeftTopX, mBlackNumLeftTopY
				+ mBlackNumGapY, 0,canvas);
		DrawImage(mBlackGeNum, mBlackNumLeftTopX + mBlackNumGapX,
				mBlackNumLeftTopY + mBlackNumGapY, 0,canvas);
		getBlackNum(mBlackLose);
		DrawImage(mBlackShiNum, mBlackNumLeftTopX, mBlackNumLeftTopY + 2
				* mBlackNumGapY, 0,canvas);
		DrawImage(mBlackGeNum, mBlackNumLeftTopX + mBlackNumGapX,
				mBlackNumLeftTopY + 2 * mBlackNumGapY, 0,canvas);

		for (i = 0; i < mMapHeightLengh; i++) {
			for (j = 0; j < mMapWidthLengh; j++) {
				int CampID = mGameMap[i][j];
				float x = (j * mTitleSpace_X) + mTitleIndex_x;
				float y = (i * mTitleSpace_Y) + mTitleHeight + mTitleIndex_y;
				if (CampID == CAMP_BLACK) {
					DrawImage(mBlack, x, y, ALIGN_VCENTER | ALIGN_HCENTER,canvas);
				} else if (CampID == CAMP_WHITE) {
					DrawImage(mWhite, x, y, ALIGN_VCENTER | ALIGN_HCENTER,canvas);
				}
			}
		}
	}

	private void DrawRect(int color, int x, int y, int width, int height,Canvas canvas) {
		sPaint.setColor(color);
		canvas.clipRect(x, y, width, height);
		canvas.drawRect(x, y, width, height, sPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			UpdateTouchEvent(x, y);
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			break;
		}
		return super.onTouchEvent(event);
	}

	public boolean CheckPiecesMeet(int Camp) {
		int MeetCount = 0;
		// 横向
		for (int i = 0; i < CALU_ALL_COUNT; i++) {
			int index = mMapIndexX - CALU_SINGLE_COUNT + i;
			if (index < 0 || index >= mMapWidthLengh) {
				if (MeetCount == CALU_SINGLE_COUNT) {
					return true;
				}
				MeetCount = 0;
				continue;
			}
			if (mGameMap[mMapIndexY][index] == Camp) {
				MeetCount++;
				if (MeetCount == CALU_SINGLE_COUNT) {
					return true;
				}
			} else {
				MeetCount = 0;
			}
		}
		// 纵向
		MeetCount = 0;
		for (int i = 0; i < CALU_ALL_COUNT; i++) {
			int index = mMapIndexY - CALU_SINGLE_COUNT + i;
			if (index < 0 || index >= mMapHeightLengh) {
				if (MeetCount == CALU_SINGLE_COUNT) {
					return true;
				}
				MeetCount = 0;
				continue;
			}
			if (mGameMap[index][mMapIndexX] == Camp) {
				MeetCount++;
				if (MeetCount == CALU_SINGLE_COUNT) {
					return true;
				}
			} else {
				MeetCount = 0;
			}
		}
		// 右斜
		MeetCount = 0;
		for (int i = 0; i < CALU_ALL_COUNT; i++) {
			int indexX = mMapIndexX - CALU_SINGLE_COUNT + i;
			int indexY = mMapIndexY - CALU_SINGLE_COUNT + i;
			if ((indexX < 0 || indexX >= mMapWidthLengh)
					|| (indexY < 0 || indexY >= mMapHeightLengh)) {
				if (MeetCount == CALU_SINGLE_COUNT) {
					return true;
				}
				MeetCount = 0;
				continue;
			}
			if (mGameMap[indexY][indexX] == Camp) {
				MeetCount++;
				if (MeetCount == CALU_SINGLE_COUNT) {
					return true;
				}
			} else {
				MeetCount = 0;
			}
		}
		// 左斜
		MeetCount = 0;
		for (int i = 0; i < CALU_ALL_COUNT; i++) {
			int indexX = mMapIndexX - CALU_SINGLE_COUNT + i;
			int indexY = mMapIndexY + CALU_SINGLE_COUNT - i;
			if ((indexX < 0 || indexX >= mMapWidthLengh)
					|| (indexY < 0 || indexY >= mMapHeightLengh)) {
				if (MeetCount == CALU_SINGLE_COUNT) {
					return true;
				}
				MeetCount = 0;
				continue;
			}
			if (mGameMap[indexY][indexX] == Camp) {
				MeetCount++;
				if (MeetCount == CALU_SINGLE_COUNT) {
					return true;
				}
			} else {
				MeetCount = 0;
			}
		}
		return false;
	}

	public void onRePlay() {
		setGameState(GS_GAME);
		invalidate();
		mPlay = true;
	}

	private void onUndo() {
		new AlertDialog.Builder(mActivity)
				.setTitle("悔棋")
				.setMessage("确定要悔棋吗？")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mActivity.sendMessage("悔棋请求" + "," + mLastX + ","
								+ mLastY);
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

	private void onExit() {
		new AlertDialog.Builder(mActivity)
				.setTitle("退出游戏")
				.setMessage("确定退出游戏？")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						BluetoothAdapter ba_1 = BluetoothAdapter
								.getDefaultAdapter();
						if (ba_1 != null) {
							if (ba_1.isEnabled()) // 如果蓝牙是打开的，则关闭
								ba_1.disable();
						}
						System.exit(0);
					}
				})
				.setNegativeButton("否", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}).show();
	}

	public int getLastX() {
		return mLastX;
	}

	public int getLastY() {
		return mLastY;
	}

	public boolean getUndo() {
		return mUndo;
	}

	public boolean getPlay() {
		return mPlay;
	}

	public void setUndo(boolean bUndo) {
		mUndo = bUndo;
	}

	public void setPlay(boolean bPlay) {
		mPlay = bPlay;
	}

	private void playChess(int x, int y) {
		mMapIndexX = Math.round((x - mTitleIndex_x) / mTitleSpace_X); // 将屏幕坐标转换为棋盘坐标
		mMapIndexY = Math.round((y - mTitleHeight - mTitleIndex_y)
				/ mTitleSpace_Y);
		if (mMapIndexX > mMapWidthLengh) {
			mMapIndexX = mMapWidthLengh;
		}
		if (mMapIndexX < 0) {
			mMapIndexX = 0;
		}
		if (mMapIndexY > mMapHeightLengh) {
			mMapIndexY = mMapHeightLengh;
		}
		if (mMapIndexY < 0) {
			mMapIndexY = 0;
		}
		mLastX = mMapIndexX; // 记录最近一次棋盘坐标
		mLastY = mMapIndexY;
		mUndo = true; // 设置悔棋标志
		mActivity.sendMessage("禁止悔棋" + "," + "此期间不能悔棋"); // 告知对方 此期间不能悔棋
		if (mGameMap[mMapIndexY][mMapIndexX] == CAMP_DEFAULT) {
			if (mCampTurn == CAMP_BLACK) {
				mGameMap[mMapIndexY][mMapIndexX] = CAMP_BLACK;      //让棋子闪烁两次
				invalidate();
				mGameMap[mMapIndexY][mMapIndexX] = CAMP_DEFAULT;
				invalidate();
				mGameMap[mMapIndexY][mMapIndexX] = CAMP_BLACK;
				invalidate();
				mGameMap[mMapIndexY][mMapIndexX] = CAMP_DEFAULT;
				invalidate();
				mGameMap[mMapIndexY][mMapIndexX] = CAMP_BLACK;
				invalidate();
				if (CheckPiecesMeet(CAMP_BLACK)) {
					mBlackWin += 1;
					mWhiteLose += 1;
			//		invalidate();
					mActivity.sendMessage("本局结束" + "," + "黑方胜");
					mCampWinner = R.string.Role_black;
					invalidate();
					new AlertDialog.Builder(mActivity)
							.setTitle("继续游戏")
							.setMessage(
									sResources.getString(mCampWinner) + "胜利" + ","
											+ "是否继续游戏？")
							.setPositiveButton("是",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											onRePlay();
											mCampTurn = CAMP_BLACK;
										}
									})
							.setNegativeButton("否",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											System.exit(0);
										}
									}).show();
				
			//		setGameState(GS_END);
				} else {
					if (isCheckFull()) {
						mBlackTie += 1;
						mWhiteTie += 1;
			//			invalidate();
						mActivity.sendMessage("本局结束" + "," + "平局");
						mCampWinner = R.string.Role_tie;
						invalidate();
						

						new AlertDialog.Builder(mActivity)
								.setTitle("继续游戏")
								.setMessage("平局" + "," + "是否继续游戏？")
								.setPositiveButton("是",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog,
													int which) {
												// TODO Auto-generated method stub
												onRePlay();
											}
										})
								.setNegativeButton("否",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog,
													int which) {
												System.exit(0);
											}
										}).show();
					
						
						
						
						
			//			setGameState(GS_END); // 本局游戏结束
					}
				}
			} else {
				mGameMap[mMapIndexY][mMapIndexX] = CAMP_WHITE;    //让棋子闪烁两次
				invalidate();
				mGameMap[mMapIndexY][mMapIndexX] = CAMP_DEFAULT;
				invalidate();
				mGameMap[mMapIndexY][mMapIndexX] = CAMP_WHITE;
				invalidate();
				mGameMap[mMapIndexY][mMapIndexX] = CAMP_DEFAULT;
				invalidate();
				mGameMap[mMapIndexY][mMapIndexX] = CAMP_WHITE;
				invalidate();
				if (CheckPiecesMeet(CAMP_WHITE)) {
					mWhiteWin += 1;
					mBlackLose += 1;
			//		invalidate();
					mActivity.sendMessage("本局结束" + "," + "白方胜");
					mCampWinner = R.string.Role_white;
					invalidate();
					new AlertDialog.Builder(mActivity)
					.setTitle("继续游戏")
					.setMessage(
							sResources.getString(mCampWinner) + "胜利" + ","
									+ "是否继续游戏？")
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									onRePlay();
									mCampTurn = CAMP_WHITE;
								}
							})
					.setNegativeButton("否",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									System.exit(0);
								}
							}).show();
					
					
					
					
			//		setGameState(GS_END);
				} else {
					if (isCheckFull()) {
						mWhiteTie += 1;
						mBlackTie += 1;
		//				invalidate();
						mActivity.sendMessage("本局结束" + "," + "平局");
						mCampWinner = R.string.Role_tie;
						invalidate();
						
						new AlertDialog.Builder(mActivity)
						.setTitle("继续游戏")
						.setMessage("平局" + "," + "是否继续游戏？")
						.setPositiveButton("是",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										onRePlay();
									}
								})
						.setNegativeButton("否",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										System.exit(0);
									}
								}).show();
						
					
						
						
		//				setGameState(GS_END); // 本局游戏结束
					}
				}
			}
			mActivity.sendMessage("下棋" + "," + mCampTurn + "," + mMapIndexX
					+ "," + mMapIndexY); // 将棋盘坐标传给对方玩家
			mPlay = false;
		} else {
			mPlay = true;
			Toast.makeText(mActivity, "该位置已经放置棋子，重选择其它位置", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void UpdateTouchEvent(int x, int y) {
		switch (mGameState) {
		case GS_GAME:
			if (x >= mExitX1 && x <= mExitX2 && y >= mY1 && y <= mY2) // 响应退出点击事件
			{
				onExit();
			} else if (mActivity.getConnectState()) {
				if (x >= mReplayX1 && x <= mReplayX2 && y >= mY1 && y <= mY2) // 响应重玩点击事件
				{
					new AlertDialog.Builder(mActivity)
							.setTitle("重新开始游戏")
							.setMessage("确定要重新开始游戏吗？")
							.setPositiveButton("是",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											mActivity.sendMessage("请求重玩" + ","
													+ "重新开始");
										}
									})
							.setNegativeButton("否",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
										}
									}).show();
				}

				else if (x >= mUndoX1 && x <= mUndoX2 && y >= mY1 && y <= mY2) // 响应悔棋点击事件
				{
					if (mUndo)
						onUndo();
					else
						Toast.makeText(mActivity, "此期间不允许悔棋", Toast.LENGTH_LONG)
								.show();
				} else if (x > 0 && y > mTitleHeight) {
					if (mPlay)
						playChess(x, y);
					else
						Toast.makeText(mActivity, "对方还没有下棋，请等待",
								Toast.LENGTH_SHORT).show();
				}
			} else
				Toast.makeText(mActivity, "没有玩家，请先通过蓝牙连接玩家", Toast.LENGTH_SHORT)
						.show();
			break;
		case GS_END:
			if (sResources.getString(mCampWinner).equals("平局")) {
				new AlertDialog.Builder(mActivity)
						.setTitle("继续游戏")
						.setMessage("平局" + "," + "是否继续游戏？")
						.setPositiveButton("是",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										setGameState(GS_GAME);
										onRePlay();
									}
								})
						.setNegativeButton("否",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										System.exit(0);
									}
								}).show();
			} else {
				new AlertDialog.Builder(mActivity)
						.setTitle("继续游戏")
						.setMessage(
								sResources.getString(mCampWinner) + "胜利" + ","
										+ "是否继续游戏？")
						.setPositiveButton("是",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										setGameState(GS_GAME);
										onRePlay();
									}
								})
						.setNegativeButton("否",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										System.exit(0);
									}
								}).show();
			}
			break;
		}
		invalidate();
	}

	public boolean isCheckFull() {
		for (int i = 0; i < mMapHeightLengh; i++)
			for (int j = 0; j < mMapWidthLengh; j++)
				if (mGameMap[i][j] != CAMP_DEFAULT)
					return false;
		return true;
	}

	public void setBlackWin() {
		mBlackWin += 1;
		mWhiteLose += 1;
	}

	public void setWhiteWin() {
		mWhiteWin += 1;
		mBlackLose += 1;
	}

	public void setTie() {
		mBlackTie += 1;
		mWhiteTie += 1;
	}

	public boolean isCheckInvite(String body) {
		if (body.indexOf("invite") >= 0) {
			if (mGameState != GS_INVITING && mGameState != GS_COMFIRE
					&& mGameState != GS_GAME) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 创建一个缩小或放大的新图片
	 * 
	 * @param resourcesID
	 * @param scr_width
	 * @param res_height
	 * @return
	 */
	private Bitmap CreatMatrixBitmap(int resourcesID, float scr_width,
			float res_height) {
		Bitmap bitMap = null;
		bitMap = BitmapFactory.decodeResource(sResources, resourcesID);
		int bitWidth = bitMap.getWidth();
		int bitHeight = bitMap.getHeight();
		float scaleWidth = scr_width / (float) bitWidth;
		float scaleHeight = res_height / (float) bitHeight;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		bitMap = Bitmap.createBitmap(bitMap, 0, 0, bitWidth, bitHeight, matrix,
				true);
		return bitMap;
	}

	/**
	 * 绘制一个字符串
	 * 
	 * @param text
	 * @param x
	 * @param y
	 * @param anchor
	 * @param Canvas
	 * @param paint
	 */
	void DrawString(int color, String text, int x, int y, int anchor,Canvas canvas) {
		Rect rect = new Rect();
		sPaint.getTextBounds(text, 0, text.length(), rect);
		int w = rect.width();
		int h = rect.height();
		int tx = 0;
		int ty = 0;
		if ((anchor & ALIGN_RIGHT) != 0) {
			tx = x - w;
		} else if ((anchor & ALIGN_HCENTER) != 0) {
			tx = x - (w >> 1);
		} else {
			tx = x;
		}
		if ((anchor & ALIGN_TOP) != 0) {
			ty = y + h;
		} else if ((anchor & ALIGN_VCENTER) != 0) {
			ty = y + (h >> 1);
		} else {
			ty = y;
		}
		sPaint.setColor(color);
		canvas.drawText(text, tx, ty, sPaint);
	}

	/**
	 * 绘制一张图片可以选择图片的锚点位置
	 * 
	 * @param canvas
	 * @param paint
	 * @param bitmap
	 * @param x
	 * @param y
	 * @param angle
	 */
	private void DrawImage(Bitmap bitmap, float x, float y, int anchor,Canvas canvas) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		float tx = 0;
		float ty = 0;
		if ((anchor & ALIGN_RIGHT) != 0) {
			tx = x - w;
		} else if ((anchor & ALIGN_HCENTER) != 0) {
			tx = x - (w >> 1);
		} else {
			tx = x;
		}
		if ((anchor & ALIGN_TOP) != 0) {
			ty = y + h;
		} else if ((anchor & ALIGN_VCENTER) != 0) {
			ty = y - (h >> 1);
		} else if ((anchor & ALIGN_BOTTOM) != 0) {
			ty = y - h;
		} else {
			ty = y;
		}
		canvas.drawBitmap(bitmap, tx, ty, sPaint);
	}

}
