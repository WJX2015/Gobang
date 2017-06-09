package com.example.lenovo_g50_70.gobang;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo-G50-70 on 2017/6/9.
 */

public class ChessView extends View {

    //棋盘的宽度
    private int mPanelWidth;
    //棋盘每一行的行高
    private float mLineHeight;
    //棋盘的行数
    private int MAX_LINE = 10;
    //画笔
    private Paint mPaint;

    //黑白棋子图片
    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;
    //引入棋子大小比例
    private float radioPieceOfLineHeight = 1.0f * 3 / 4;

    //存放用户点击棋盘的坐标
    private List<Point> mWhitePoints = new ArrayList<>();
    private List<Point> mBlackPoints = new ArrayList<>();
    //黑棋先手,轮到下棋的颜色
    private boolean mIsBlack = true;

    private boolean mIsGameOver; //游戏结束
    private boolean mIsWhiteWinner; //谁是赢家

    private int MAX_COUNT_WIN_LINE = 5; //五子连珠

    public ChessView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //不管三七二十一，先设置背景颜色，看View所在的位置
        setBackgroundColor(0x44ff0000);
        initPaint();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint();
        //十六进制，ARGB，半透明的黑色==灰色
        mPaint.setColor(0x88000000);
        //抗锯齿，效果边界圆滑
        mPaint.setAntiAlias(true);
        //仿抖动，效果颜色柔和
        mPaint.setDither(true);
        //画线
        mPaint.setStyle(Paint.Style.STROKE);

        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //宽高确定改变后，调用该方法
        super.onSizeChanged(w, h, oldw, oldh);
        //初始化尺寸相关的成员变量
        mPanelWidth = w;
        /**
         * 为什么选择宽，而不选择高
         * 正常情况，手机宽度比高度小
         * 当前棋盘形状为正方形
         */
        mLineHeight = mPanelWidth * 1.0f / MAX_LINE;

        //画棋子,跟随棋盘的大小变化而变化
        int pieceWidth = (int) (mLineHeight * radioPieceOfLineHeight);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //判断游戏是否结束
        if (mIsGameOver) {
            return false;
        }

        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            //获取用户点击的XY坐标
            int x = (int) event.getX();
            int y = (int) event.getY();
            //保存坐标
            Point point = getValidPoint(x, y);

            //判断点是否已经存过
            if (mWhitePoints.contains(point) || mBlackPoints.contains(point)) {
                //不处理
                return false;
            }

            //判断棋子存放哪个列中
            if (mIsBlack) {
                mBlackPoints.add(point);
            } else {
                mWhitePoints.add(point);
            }
            //下棋后，请求重绘
            invalidate();
            //棋换颜色
            mIsBlack = !mIsBlack;
        }

        return true;
    }

    /**
     * 以左上角坐标为例,左上角的坐标是(0,0)
     *
     * @param x x=mLineHeight/2,x/mLineHeight=0.5,(int)0.5=0;
     * @param y y=mLineHeight/2,y/mLineHeight=0.5,(int)0.5=0;
     * @return 网格交汇的点
     */
    private Point getValidPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //从父布局测量出子控件在父视图所占的宽度
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        //从父布局测量出子控件在父视图所占的高度
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //棋盘是正方形，宽高相等，取小值
        int width = Math.min(widthSize, heightSize);

        /**
         * 判断控件是否可以随意使用空间 , 以ScrollView为例子
         * 使用ScrollView控件嵌套时，可能使自定义View无法显示
         * 某一方向空间足够，测量出的尺寸可能是0，也可能是某一个确定的值
         */
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            //水平ScrollView时，垂直高度能确定
            width = heightSize;//宽度由高度决定
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            //垂直ScrollView时，水平宽度能确定
            width = widthSize;
        }

        //自定义控件不推荐在布局文件宽高设置wrap_content.测量时会出问题
        //设置测量尺寸
        setMeasuredDimension(width, width);

        /**
         * 精确模式（MeasureSpec.EXACTLY）
         * 在这种模式下，尺寸的值是多少，那么这个组件的长或宽就是多少。
         * 最大模式（MeasureSpec.AT_MOST）
         * 这个也就是父组件，能够给出的最大的空间，当前组件的长或宽最大只能为这么大.
         * 未指定模式（MeasureSpec.UNSPECIFIED）
         * 当前组件，可以随便用空间，不受限制。
         */
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPiece(canvas);
        checkGameOver();
    }

    /**
     * 检测游戏是否结束
     */
    private void checkGameOver() {
        boolean whiteWin = checkFiveInLine(mWhitePoints);
        boolean blackWin = checkFiveInLine(mBlackPoints);

        //当某一方胜利时执行
        if (whiteWin || blackWin) {
            //游戏已经结束
            mIsGameOver = true;
            //获取白棋的结果
            mIsWhiteWinner = whiteWin;
            //判断白棋是否胜利
            String text = mIsWhiteWinner ? "白棋胜利" : "黑棋胜利";
            GameOverDialog(text);
        }
    }

    /**
     * 游戏结束时，显示对话框
     * @param text
     */
    private void GameOverDialog(String text) {
        //对话框的显示
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("游戏结束");
        builder.setMessage(text);
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    /**
     * 检测五子连珠
     *
     * @param points
     * @return
     */
    private boolean checkFiveInLine(List<Point> points) {
        for (Point p : points) {
            int x = p.x;
            int y = p.y;

            boolean win = checkHorizontal(x, y, points);
            if (win) return true;

            win = checkVertical(x, y, points);
            if (win) return true;

            win = checkLeftDiagonal(x, y, points);
            if (win) return true;

            win = checkRightDiagonal(x, y, points);
            if (win) return true;
        }
        return false;
    }

    /**
     * 水平五子连珠
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count = 1;

        //向左边数
        for (int i = 1; i < MAX_COUNT_WIN_LINE; i++) {
            if (points.contains(new Point(x - i, y))) {
                count++;
            } else {
                break;
            }
        }

        //如果五子连珠
        if (count == MAX_COUNT_WIN_LINE) {
            return true;
        }

        //向右边数
        for (int i = 1; i < MAX_COUNT_WIN_LINE; i++) {
            if (points.contains(new Point(x + i, y))) {
                count++;
            } else {
                break;
            }
        }

        //如果五子连珠
        if (count == MAX_COUNT_WIN_LINE) {
            return true;
        }

        return false;
    }

    /**
     * 垂直五子连珠
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkVertical(int x, int y, List<Point> points) {
        int count = 1;

        //向上边数
        for (int i = 1; i < MAX_COUNT_WIN_LINE; i++) {
            if (points.contains(new Point(x, y - i))) {
                count++;
            } else {
                break;
            }
        }

        //如果五子连珠
        if (count == MAX_COUNT_WIN_LINE) {
            return true;
        }

        //向下边数
        for (int i = 1; i < MAX_COUNT_WIN_LINE; i++) {
            if (points.contains(new Point(x, y + i))) {
                count++;
            } else {
                break;
            }
        }

        //如果五子连珠
        if (count == MAX_COUNT_WIN_LINE) {
            return true;
        }

        return false;
    }

    /**
     * 左斜线五子连珠
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkLeftDiagonal(int x, int y, List<Point> points) {
        int count = 1;

        //向斜下边数
        for (int i = 1; i < MAX_COUNT_WIN_LINE; i++) {
            if (points.contains(new Point(x - i, y + i))) {
                count++;
            } else {
                break;
            }
        }

        //如果五子连珠
        if (count == MAX_COUNT_WIN_LINE) {
            return true;
        }

        //向斜上边数
        for (int i = 1; i < MAX_COUNT_WIN_LINE; i++) {
            if (points.contains(new Point(x + i, y - i))) {
                count++;
            } else {
                break;
            }
        }

        //如果五子连珠
        if (count == MAX_COUNT_WIN_LINE) {
            return true;
        }

        return false;
    }

    /**
     * 向右斜边数
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkRightDiagonal(int x, int y, List<Point> points) {
        int count = 1;

        //向斜下边数
        for (int i = 1; i < MAX_COUNT_WIN_LINE; i++) {
            if (points.contains(new Point(x + i, y + i))) {
                count++;
            } else {
                break;
            }
        }

        //如果五子连珠
        if (count == MAX_COUNT_WIN_LINE) {
            return true;
        }

        //向斜上边数
        for (int i = 1; i < MAX_COUNT_WIN_LINE; i++) {
            if (points.contains(new Point(x - i, y - i))) {
                count++;
            } else {
                break;
            }
        }

        //如果五子连珠
        if (count == MAX_COUNT_WIN_LINE) {
            return true;
        }

        return false;
    }

    /**
     * 画棋子
     *
     * @param canvas
     */
    private void drawPiece(Canvas canvas) {
        float x, y;
        //绘制黑棋
        for (int i = 0, n = mBlackPoints.size(); i < n; i++) {
            Point blackPoint = mBlackPoints.get(i);

            /**
             * 棋盘距离左边1/2个mLineHeight
             * 棋子一半的宽度 1/2个mLineHeight*radioPieceOfLineHeight
             * 棋子左边的空隙 1/2个mLineHeight*1/4;
             */
            x = ((blackPoint.x + (1 - radioPieceOfLineHeight) / 2) * mLineHeight);
            y = ((blackPoint.y + (1 - radioPieceOfLineHeight) / 2) * mLineHeight);

            //画布从图片的左上角开始画的
            canvas.drawBitmap(mBlackPiece, x, y, null);
        }
        //绘制白棋
        for (int i = 0, n = mWhitePoints.size(); i < n; i++) {
            Point whitePoint = mWhitePoints.get(i);
            x = ((whitePoint.x + (1 - radioPieceOfLineHeight) / 2) * mLineHeight);
            y = ((whitePoint.y + (1 - radioPieceOfLineHeight) / 2) * mLineHeight);
            //画布从图片的左上角开始画的
            canvas.drawBitmap(mWhitePiece, x, y, null);
        }
    }

    /**
     * 绘制棋盘
     */
    private void drawBoard(Canvas canvas) {
        //棋盘的宽度
        int width = mPanelWidth;
        //棋盘的行高
        float lineHeight = mLineHeight;

        for (int i = 0; i < lineHeight; i++) {

            int startX = (int) (lineHeight / 2);
            int endX = (int) (width - lineHeight / 2);
            int y = (int) ((0.5 + i) * lineHeight);

            //画横线
            canvas.drawLine(startX, y, endX, y, mPaint);
            //画竖线
            canvas.drawLine(y, startX, y, endX, mPaint);
        }
    }
}
