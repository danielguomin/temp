package com.miyuan.smarthome.temp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.miyuan.smarthome.temp.db.Entry;
import com.miyuan.smarthome.temp.db.Nurse;
import com.miyuan.smarthome.temp.log.Log;
import com.miyuan.smarthome.temp.utils.TimeUtils;
import com.miyuan.smarthome.temp.utils.UIUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * @author haizhuo
 */
public class WaterMeterView extends View {

    public static final String DEFAULT = "yyyy-MM-dd HH:mm:ss";
    /**
     * Y方向有数据的Item个数
     * 默认7个
     */
    private static final int itemYSize = 7;
    private static final int ITEMXCOUNT = 7;
    private static SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT);
    private final int[] DEGRESS = new int[]{35, 36, 37, 38, 39, 40, 41, 42};
    private int currentType = 0;
    /**
     * 折线画笔
     */
    private Paint brokenLinePaint;
    /**
     * 坐标画笔
     */
    private Paint coordinatePaint;
    /**
     * 圆点画笔
     */
    private Paint circlePaint;
    /**
     * 文字画笔（x、y轴）
     */
    private Paint textPaint;
    /**
     * 文字画笔（水表详情）
     */
    private Paint textPaintDetail;
    /**
     * 渐变背景画笔
     */
    private Paint backGroundPaint;
    /**
     * 详情背景画笔
     */
    private Paint backGroundDetailPaint;
    /**
     * 背景颜色
     */
    private int backGroundColor = Color.WHITE;
    /**
     * 文字画笔颜色
     * 默认：黑色
     */
    private int colorTextPaint = Color.BLACK;
    /**
     * 折现画笔颜色
     * 默认：蓝色 0xFF1281FD
     */
    private int colorBrokenLinePaint = 0xFF1281FD;
    /**
     * 坐标画笔颜色
     * 默认 灰色 0xFFEEEEEE
     */
    private int colorCoordinatePaint = 0xFFEEEEEE;
    /**
     * 详情文字背景颜色
     */
    private int colorDetailTextBg = 0x66000000;
    /**
     * 文字大小
     * 默认11sp
     */
    private float textSize;
    /**
     * 水表详情文字大小
     * 默认10sp
     */
    private float textSizeDetail;
    /**
     * 单位高度差
     * 默认：itemWidth/unitYItem
     */
    private float unitVerticalGap;
    /**
     * itemWidth对应的用水量（吨）
     */
    private int unitYItem;
    /**
     * 折线图左和下的间距，同横纵单位间隔
     * 默认：42dp
     */
    private int defaultPadding;
    private int itemWidth;
    private int itemHeight;
    /**
     * 控件期望高度
     * 默认为8个itemWidth
     */
    private int expectViewHeight;
    /**
     * 折线点的半径(默认2.5dp的像素)
     */
    private float pointRadius;
    private int viewWidth;
    private int viewHeight;
    private int screenWidth;
    private int screenHeight;
    /**
     * 曲线路径
     */
    private Path curvePath;
    /**
     * 水表详情背景路径
     */
    private Path WaterDetailBgPath;
    /**
     * 水表详情背景图范围
     */
    private RectF rectF;
    /**
     * 最多用量
     */
    private float maxDosage;
    /**
     * 最少用量
     */
    private float minDosage;
    /**
     * 数据列表data
     */
    private List<Entry> list = new ArrayList<>();

    private List<Nurse> allNurses = new ArrayList<>();
    private List<Nurse> nurses = new ArrayList<>();
    /**
     * 每个月的坐标点集
     */
    private List<PointF> pointFList = new ArrayList<>();
    private List<PointF> nursePointFList = new ArrayList<>();
    /**
     * 选中的那个点
     */
    private PointF pointFSelected = null;
    private int pointFSelectedPosition = -1;

    private PointF pointFNurse = null;
    private int pointSelectedNurse = -1;
    /**
     * 最大点
     */
    private PointF pointFMax;
    private String maxTemp;
    /**
     * 速度追踪器
     */
    private VelocityTracker velocityTracker;
    /**
     * 关于UI的标准常量
     */
    private ViewConfiguration viewConfiguration;
    private long startTime = 0;
    /**
     * Scroller
     */
    private Scroller scroller;
    private float x, y;

    public WaterMeterView(Context context) {
        this(context, null);
    }

    public WaterMeterView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterMeterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关闭硬件加速，绘制虚线需要
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        //设置一些默认值
        textSize = UIUtil.dp2pxF(11);
        textSizeDetail = UIUtil.dp2pxF(10);
        pointRadius = UIUtil.dp2pxF(2.5f);
        setBackgroundColor(backGroundColor);
//        defaultPadding = itemWidth = (int) UIUtil.dp2pxF(42);
//        itemHeight = (int) UIUtil.dp2pxF(42);
        //期望高度默认值为7个itemWidth，（itemYSize默认值+1+2）
        expectViewHeight = itemWidth * (7);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        //初始化画笔
        brokenLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        brokenLinePaint.setColor(colorBrokenLinePaint);
        brokenLinePaint.setStrokeWidth(UIUtil.dp2pxF(1f));
        brokenLinePaint.setStyle(Paint.Style.STROKE);
        coordinatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coordinatePaint.setColor(colorCoordinatePaint);
        coordinatePaint.setStrokeWidth(UIUtil.dp2pxF(0.5f));
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setColor(colorTextPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaintDetail = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaintDetail.setTextSize(textSizeDetail);
        textPaintDetail.setColor(backGroundColor);
        textPaintDetail.setTextAlign(Paint.Align.LEFT);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coordinatePaint.setStrokeWidth(UIUtil.dp2pxF(0.5f));
        backGroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backGroundDetailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backGroundDetailPaint.setColor(colorDetailTextBg);
        curvePath = new Path();
        WaterDetailBgPath = new Path();
        rectF = new RectF();

        viewConfiguration = ViewConfiguration.get(context);
        scroller = new Scroller(context);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointFSelected = null;
                pointFSelectedPosition = -1;
                pointFNurse = null;
                pointSelectedNurse = -1;
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                for (int i = 0; i < pointFList.size(); i++) {
                    if (Math.abs(x - (pointFList.get(i).x)) < UIUtil.dp2pxF(5) && Math.abs(y - pointFList.get(i).y) < UIUtil.dp2pxF(5)) {
                        pointFSelected = pointFList.get(i);
                        pointFSelectedPosition = i;
                        invalidate();
                        return true;
                    }
                }
                for (int i = 0; i < nursePointFList.size(); i++) {
                    if (Math.abs(x - (nursePointFList.get(i).x)) < UIUtil.dp2pxF(10) && Math.abs(y - nursePointFList.get(i).y) < UIUtil.dp2pxF(10)) {
                        pointFNurse = nursePointFList.get(i);
                        pointSelectedNurse = i;
                        invalidate();
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        invalidate();
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            //动画尚未完成
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY)// match_parent , accurate
        {
            viewHeight = specSize;
        } else {
            int desire = getPaddingTop() + getPaddingBottom() + getHeight();
            if (specMode == MeasureSpec.AT_MOST) // wrap_content
            {
                viewHeight = Math.min(desire, specSize);
            } else
                viewHeight = desire;
        }

        //确定宽度
        specSize = MeasureSpec.getSize(widthMeasureSpec);
        specMode = MeasureSpec.getMode(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            viewWidth = specSize;
        } else {
            // 由图片决定的宽
            int desireWidth = getPaddingLeft() + getPaddingRight()
                    + getWidth();
            if (specMode == MeasureSpec.AT_MOST)// wrap_content
            {
                viewWidth = Math.min(desireWidth, specSize);
            } else {
                viewWidth = desireWidth;
            }
        }
        itemWidth = viewWidth / 7;
        viewHeight = viewWidth * 3 / 5;
        itemHeight = viewHeight / 9;
        setMeasuredDimension(viewWidth, viewHeight);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        itemWidth = viewWidth / 7;
    }

    /**
     * 更改横坐标
     */
    public void changeStyle(List<Entry> data, int type) {
        currentType = type;
        setData(data, this.allNurses);
    }

    private long beginTime;

    /**
     * 公开方法，用于设置元数据
     *
     * @param data
     */
    public void setData(List<Entry> data, List<Nurse> nurses) {
//        if (data == null) {
//            invalidate();
//            return;
//        }
        this.allNurses = nurses;
        this.nurses.clear();
        //数据清理
        maxTemp = "";
        pointFMax = null;
        list.clear();
        pointFList.clear();
        nursePointFList.clear();
        pointFSelectedPosition = -1;
        pointFSelected = null;
        pointSelectedNurse = -1;
        pointFNurse = null;
        beginTime = System.currentTimeMillis();
        switch (currentType) {
            case 0:
                startTime = beginTime / 1000 - 30 * 60;
                break;
            case 1:
                startTime = beginTime / 1000 - 2 * 60 * 60;
                break;
            case 2:
                startTime = beginTime / 1000 - 6 * 60 * 60;
                break;
            case 3:
                startTime = beginTime / 1000 - 12 * 60 * 60;
                break;
            case 4:
                startTime = beginTime / 1000 - 24 * 60 * 60;
                break;
            case 5:
                startTime = TimeUtils.getTimeTodayBegin(data.get(0).getTime()) / 1000;
                break;
        }
        for (Entry entity : data) {
            if (entity.getTime() >= startTime) {
                list.add(entity);
            }
        }

        for (Nurse nurse : allNurses) {
            if (nurse.getTime() >= startTime) {
                this.nurses.add(nurse);
            }
        }
        initPointFData();
        invalidate();
    }

    public void setNurses(List<Nurse> nurses) {
        this.allNurses = nurses;
        initPointFData();
    }

    /**
     * 获取数据点集
     */
    private void initPointFData() {
        float centerX = 0;
        float centerY = 0;
        pointFList.clear();
        nursePointFList.clear();
        int dividend = 30 * 60;
        switch (currentType) {
            case 0:
                dividend = 30 * 60;
                break;
            case 1:
                dividend = 2 * 60 * 60;
                break;
            case 2:
                dividend = 6 * 60 * 60;
                break;
            case 3:
                dividend = 12 * 60 * 60;
                break;
            case 4:
            case 5:
                dividend = 24 * 60 * 60;
                break;

        }
        for (int i = 0; i < list.size(); i++) {
            long time = list.get(i).getTime();
            centerX = getXFromTime(time / 1000, dividend);
            centerY = getYFromTemp(list.get(i).getTemp());
            pointFList.add(new PointF(centerX, centerY));
        }
        for (int i = 0; nurses != null && i < nurses.size(); i++) {
            long time = nurses.get(i).getTime();
            centerX = getXFromTime(time / 1000, dividend);
            centerY = viewHeight - itemHeight * 2 / 3 + UIUtil.dp2pxF(1f);
            nursePointFList.add(new PointF(centerX, centerY));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画渐变蓝色背景
        drawBackBlue(canvas);
        //画坐标
        drawAxis(canvas);
        //画护理
        drawNurseAxis(canvas);
        //画曲线
        drawCurve(canvas);
        //画最大温度点
        drawMaxPoint(canvas);
        //画小圆点和虚线
        drawPointsAndLine(canvas);
        //画水温度详情
        drawWaterDetailsText(canvas);
        //画护理详情
        drawNurseDetailsText(canvas);
    }

    private void drawNurseAxis(Canvas canvas) {
        if (nursePointFList.size() == 0) {
            return;
        }
        canvas.save();
        circlePaint.reset();
        for (PointF pointF : nursePointFList) {
            if (pointF.x - UIUtil.dp2pxF(14) < itemWidth * 3 / 4) {
                continue;
            }
            //画圆
            float x_point = pointF.x;
            float y_point = pointF.y;
            //用背景色在拐点处画实心圆
            circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            circlePaint.setColor(Color.GRAY);
            canvas.drawCircle(x_point, y_point, pointRadius + UIUtil.dp2pxF(2f), circlePaint);
        }
        canvas.restore();
    }

    private void drawNurseDetailsText(Canvas canvas) {
        canvas.save();
        WaterDetailBgPath.reset();
        if (pointFNurse == null || pointSelectedNurse == -1) {
            return;
        }
        Nurse entity = nurses.get(pointSelectedNurse);
        String textO = "记录时间：" + sdf.format(entity.getTime());
        String textT = "";
        switch (entity.getType()) {
            case 0:
                textT = "护理方式：用药";
                break;
            case 1:
                textT = "护理方式：物理降温";
                break;
            case 2:
                textT = "护理方式：其他";
                break;
        }
        String content = entity.getContent();

        rectF.left = pointFNurse.x + UIUtil.dp2pxF(12);
        //88dp是当text0，没有读数的时候，默认的长度。
        rectF.right = pointFNurse.x + UIUtil.dp2pxF(12) + UIUtil.getTextWidth(textPaintDetail, textO) + UIUtil.dp2pxF(50);
        rectF.bottom = pointFNurse.y - UIUtil.dp2pxF(12);

        if (rectF.right > viewWidth) {
            //调整文字框位置
            rectF.left = pointFNurse.x - UIUtil.dp2pxF(12) - UIUtil.getTextWidth(textPaintDetail, textO) - UIUtil.dp2pxF(50);
            rectF.right = pointFNurse.x - UIUtil.dp2pxF(12);
        }
        float verticalOffset = 0;
        int start = 0;
        int count = 0;
        while (start < content.length()) {
            count = textPaintDetail.breakText(content, start, content.length(), true, UIUtil.getTextWidth(textPaintDetail, textO) + UIUtil.dp2pxF(40), null);
            start += count;
            verticalOffset += textPaintDetail.getFontSpacing();
        }
        rectF.top = pointFNurse.y - verticalOffset - UIUtil.dp2pxF(40);
        WaterDetailBgPath.moveTo(rectF.left, rectF.top);
        WaterDetailBgPath.addRoundRect(rectF, UIUtil.dp2pxF(5), UIUtil.dp2pxF(5), Path.Direction.CW);
        //画背景
        canvas.drawPath(WaterDetailBgPath, backGroundDetailPaint);
        //写第一行文字
        textPaintDetail.setColor(backGroundColor);
        verticalOffset = rectF.top + UIUtil.dp2pxF(12);
        canvas.drawText(textO, 0, textO.length(), rectF.left + UIUtil.dp2pxF(8), verticalOffset, textPaintDetail);
        verticalOffset += textPaintDetail.getFontSpacing();
        canvas.drawText(textT, 0, textT.length(), rectF.left + UIUtil.dp2pxF(8), verticalOffset, textPaintDetail);
        verticalOffset += textPaintDetail.getFontSpacing();
        start = 0;
        count = 0;
        while (start < content.length()) {
            count = textPaintDetail.breakText(content, start, content.length(), true, UIUtil.getTextWidth(textPaintDetail, textO) + UIUtil.dp2pxF(40), null);
            canvas.drawText(content, start, start + count, rectF.left + UIUtil.dp2pxF(8), verticalOffset, textPaintDetail);
            start += count;
            verticalOffset += textPaintDetail.getFontSpacing();
        }
        Log.d("verticalOffset " + verticalOffset);
        canvas.restore();
    }

    /**
     * 画水表详情框
     */
    private void drawWaterDetailsText(Canvas canvas) {
        canvas.save();
        WaterDetailBgPath.reset();
        if (pointFSelected == null || pointFSelectedPosition == -1) {
            return;
        }


        Entry entity = list.get(pointFSelectedPosition);

        String textO = "时间：" + sdf.format(entity.getTime());
        String textT = "温度：" + entity.getTemp() + "°C";

        rectF.left = pointFSelected.x + UIUtil.dp2pxF(12);
        //88dp是当text0，没有读数的时候，默认的长度。
        rectF.right = pointFSelected.x + UIUtil.dp2pxF(12) + UIUtil.getTextWidth(textPaintDetail, textO) + UIUtil.dp2pxF(12);
        rectF.top = pointFSelected.y - UIUtil.dp2pxF(52);
        rectF.bottom = pointFSelected.y - UIUtil.dp2pxF(12);

        if (rectF.right > viewWidth) {
            //调整文字框位置
            rectF.left = pointFSelected.x - UIUtil.dp2pxF(12) - UIUtil.getTextWidth(textPaintDetail, textO) - UIUtil.dp2pxF(12);
            rectF.right = pointFSelected.x - UIUtil.dp2pxF(12);
        }
        if (rectF.top < itemHeight / 3) {
            rectF.top = itemHeight / 3;
            rectF.bottom = itemHeight / 3 + UIUtil.dp2pxF(52) - UIUtil.dp2pxF(12);
        }
        WaterDetailBgPath.moveTo(rectF.left, rectF.top);
        WaterDetailBgPath.addRoundRect(rectF, UIUtil.dp2pxF(5), UIUtil.dp2pxF(5), Path.Direction.CW);
        //画背景
        canvas.drawPath(WaterDetailBgPath, backGroundDetailPaint);
        //写第一行文字
        Paint.FontMetrics m = textPaintDetail.getFontMetrics();
        textPaintDetail.setColor(backGroundColor);
        canvas.drawText(textO, 0, textO.length(), rectF.left + UIUtil.dp2pxF(8), rectF.bottom - UIUtil.dp2pxF(28) - (m.ascent + m.descent) / 2, textPaintDetail);
        //写第二行文字
        canvas.drawText(textT, 0, textT.length(), rectF.left + UIUtil.dp2pxF(8), rectF.bottom - UIUtil.dp2pxF(12) - (m.ascent + m.descent) / 2, textPaintDetail);
        canvas.restore();
    }

    private void drawMaxPoint(Canvas canvas) {
        if (pointFMax == null || "".equals(maxTemp)) {
            return;
        }
        //画圆
        float x_point = pointFMax.x;
        float y_point = pointFMax.y;

        if (x_point - UIUtil.dp2pxF(14) < itemWidth * 3 / 4) {
            return;
        }
        canvas.save();
        circlePaint.reset();

        //用背景色在拐点处画实心圆
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(0XFFFF0000);
        circlePaint.setStrokeWidth(5);
        canvas.drawCircle(x_point, y_point, pointRadius, circlePaint);

        //画温度
        textPaintDetail.setColor(0XFFFF0000);
        Paint.FontMetrics m = textPaintDetail.getFontMetrics();
        canvas.drawText(maxTemp, 0, maxTemp.length(), x_point - UIUtil.dp2pxF(14), y_point - UIUtil.dp2pxF(8) - (m.ascent + m.descent) / 2, textPaintDetail);

        canvas.restore();
    }

    /**
     * 画小圆点和虚线
     */
    private void drawPointsAndLine(Canvas canvas) {
        canvas.save();
        if (pointFSelected == null || pointFSelectedPosition == -1) {
            return;
        }
        circlePaint.reset();
        //画圆
        float x_point = pointFSelected.x;
        float y_point = pointFSelected.y;
        //用背景色在拐点处画实心圆
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaint.setColor(backGroundColor);
        canvas.drawCircle(x_point, y_point, pointRadius + UIUtil.dp2pxF(1f), circlePaint);
        //用折线颜色在拐点处画空心圆
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(colorBrokenLinePaint);
        canvas.drawCircle(x_point, y_point, pointRadius, circlePaint);


        //画虚线
        circlePaint.setColor(0xFF999999);
        //f数组两个值分别为循环的实线长度、空白长度
        float[] f = {UIUtil.dp2pxF(5f), UIUtil.dp2pxF(2f)};
        PathEffect pathEffect = new DashPathEffect(f, 0);
        circlePaint.setPathEffect(pathEffect);

        canvas.drawLine(x_point, y_point + UIUtil.dp2pxF(pointRadius + 0.5f), x_point, viewHeight - itemWidth, circlePaint);

        canvas.restore();
    }

    /**
     * 画渐变蓝色背景
     */
    private void drawBackBlue(Canvas canvas) {
        if (pointFList.isEmpty()) {
            return;
        }
        canvas.save();
        //遍历一遍点集合，找到用量最大的点的坐标
        pointFMax = pointFList.get(0);
        maxTemp = String.valueOf(list.get(0).getTemp()) + "°C".trim();
        for (int i = 0; i < pointFList.size(); i++) {
            if (pointFList.get(i).y < pointFMax.y) {
                pointFMax = pointFList.get(i);
                maxTemp = String.valueOf(list.get(i).getTemp()) + "°C".trim();
            }
        }
        //设置抗锯齿
        backGroundPaint.setAntiAlias(true);
        //为Paint设置渐变
        LinearGradient linearGradient = new LinearGradient(pointFMax.x, pointFMax.y, pointFList.get(0).x, viewHeight - itemHeight, new int[]{
                0xFFE1F1FF, 0xFFEFF7FF, 0xFFFAFCFF},
                null, Shader.TileMode.CLAMP);
        backGroundPaint.setShader(linearGradient);
        canvas.drawPath(getCurveAndAliasPath(), backGroundPaint);
        canvas.restore();
    }

    /**
     * 画曲线
     */
    private void drawCurve(Canvas canvas) {
        if (pointFList.isEmpty()) {
            return;
        }
        canvas.save();
        curvePath.reset();

        for (int i = 0; i < pointFList.size(); i++) {
            PointF pointF = pointFList.get(i);
            if (pointF.x < itemWidth * 3 / 4 || pointF.x > viewWidth - itemWidth / 4) {
                continue;
            }

            float temp = list.get(i).getTemp();
            if (temp <= 37.3f) {
                colorBrokenLinePaint = 0XFF08BE62;
            } else if (temp >= 37.4f && temp < 38) {
                colorBrokenLinePaint = 0XFFFFDE00;
            } else if (temp >= 38 && temp <= 39.5) {
                colorBrokenLinePaint = 0XFFFF9C01;
            } else {
                colorBrokenLinePaint = 0XFFFF0101;
            }
//            if (i == 0) {
//                curvePath.moveTo(pointFList.get(i).x, pointFList.get(i).y);
//            }
////            } else {
////                curvePath.lineTo(pointFList.get(i).x, pointFList.get(i).y);
////            }
//            if (i != pointFList.size() - 1) {
//                curvePath.cubicTo((pointFList.get(i).x + pointFList.get(i + 1).x) / 2, pointFList.get(i).y,
//                        (pointFList.get(i).x + pointFList.get(i + 1).x) / 2, pointFList.get(i + 1).y,
//                        pointFList.get(i + 1).x, pointFList.get(i + 1).y);
//            }
            brokenLinePaint.setColor(colorBrokenLinePaint);
            canvas.drawCircle(pointFList.get(i).x, pointFList.get(i).y, 2, brokenLinePaint);
        }
//        canvas.drawPath(curvePath, brokenLinePaint);
        canvas.restore();
    }

    /**
     * 画轴线
     */
    private void drawAxis(Canvas canvas) {
        canvas.save();
        coordinatePaint.setColor(colorCoordinatePaint);
        textPaint.setColor(colorTextPaint);
        //画横轴
        for (int i = 0; i <= itemYSize + 1; i++) {
            canvas.drawLine(itemWidth * 3 / 4, viewHeight - i * itemHeight - itemHeight * 2 / 3,
                    viewWidth - itemWidth / 4, viewHeight - i * itemHeight - itemHeight * 2 / 3, coordinatePaint);
        }
        //划时间
        float centerX;
        float centerY = viewHeight + UIUtil.dp2pxF(10f) - itemHeight * 2 / 3;
        for (int i = 0; i < ITEMXCOUNT; i++) {
            String text = "";
            switch (currentType) {
                case 0:  // 间隔5分钟
                    text = TimeUtils.getHourMinStr(beginTime - ((ITEMXCOUNT - 1 - i) * 5 * 60 * 1000));
                    break;
                case 1:  // 间隔20分钟
                    text = TimeUtils.getHourMinStr(beginTime - ((ITEMXCOUNT - 1 - i) * 20 * 60 * 1000));
                    break;
                case 2:  // 间隔1小时
                    text = TimeUtils.getHourMinStr(beginTime - ((ITEMXCOUNT - 1 - i) * 60 * 60 * 1000));
                    break;
                case 3:  // 间隔2小时
                    text = TimeUtils.getHourMinStr(beginTime - ((ITEMXCOUNT - 1 - i) * 2 * 60 * 60 * 1000));
                    break;
                case 4:  // 间隔4小时
                    text = TimeUtils.getHourMinStr(beginTime - ((ITEMXCOUNT - 1 - i) * 4 * 60 * 60 * 1000));
                    break;
                case 5:
                    text = (24 - ((ITEMXCOUNT - 1 - i) * 4)) + ":00";
                    break;
            }
            centerX = itemWidth * 3 / 4 + i * itemWidth;
            Paint.FontMetrics m = textPaint.getFontMetrics();
            canvas.drawText(text, 0, text.length(), centerX, centerY - (m.ascent + m.descent) / 2, textPaint);
        }

        //画纵轴
        for (int i = 0; i < itemYSize; i++) {
            canvas.drawLine(itemWidth * 3 / 4 + i * itemWidth, itemHeight * 1 / 3, itemWidth * 3 / 4 + i * itemWidth, viewHeight - itemHeight * 2 / 3, coordinatePaint);
        }
        //画度数
        float centerXNew = itemWidth * 2 / 5;
        float centerYNew;
        for (int i = 0; i < DEGRESS.length; i++) {
            if (i == 3) {
                continue;
            }
            String text = DEGRESS[i] + "°C";
            centerYNew = viewHeight - (i * itemHeight) - itemHeight - itemHeight * 2 / 3;
            Paint.FontMetrics m = textPaint.getFontMetrics();
            canvas.drawText(text, 0, text.length(), centerXNew, centerYNew - (m.ascent + m.descent) / 2, textPaint);
        }
        // 画特殊线 37.3,38,39.5
        float tempY = getYFromTemp(37.3f);
        String temp = "37.3°C";
        coordinatePaint.setColor(0XFFFFDF00);
        textPaint.setColor(0XFFFFDF00);
        canvas.drawLine(itemWidth * 3 / 4, tempY,
                viewWidth - itemWidth / 4, tempY, coordinatePaint);
        Paint.FontMetrics m = textPaint.getFontMetrics();
        canvas.drawText(temp, 0, temp.length(), centerXNew, tempY - (m.ascent + m.descent) / 2, textPaint);

        temp = "38°C";
        tempY = getYFromTemp(38f);
        coordinatePaint.setColor(0XFFFDB439);
        textPaint.setColor(0XFFFDB439);
        canvas.drawLine(itemWidth * 3 / 4, tempY,
                viewWidth - itemWidth / 4, tempY, coordinatePaint);
        canvas.drawText(temp, 0, temp.length(), centerXNew, tempY - (m.ascent + m.descent) / 2, textPaint);


        temp = "39.5°C";
        coordinatePaint.setColor(0XFFFF0000);
        textPaint.setColor(0X80FF0000);
        tempY = getYFromTemp(39.5f);
        canvas.drawLine(itemWidth * 3 / 4, tempY,
                viewWidth - itemWidth / 4, tempY, coordinatePaint);
        canvas.drawText(temp, 0, temp.length(), centerXNew, tempY - (m.ascent + m.descent) / 2, textPaint);

        canvas.restore();
    }

    private float getYFromTemp(float temp) {
        return viewHeight - (((viewHeight - itemHeight) * (temp - 35f)) / 8f) - itemHeight - itemHeight * 2 / 3;
    }

    private float getXFromTime(long time, int dividend) {
        return ((viewWidth - itemWidth) * (time - startTime)) / dividend + itemWidth * 3 / 4;
    }

    /**
     * 获取曲线图路径（曲线+x轴+2竖线）
     *
     * @return 闭合曲线图路径
     */
    private Path getCurveAndAliasPath() {
        curvePath.reset();
        List<PointF> tempList = new ArrayList<>();
        for (int i = 0; i < pointFList.size(); i++) {
            if (pointFList.get(i).x >= itemWidth * 3 / 4) {
                tempList.add(pointFList.get(i));
            }
        }
        for (int i = 0; i < tempList.size(); i++) {
            if (i == 0) {
                curvePath.moveTo(tempList.get(i).x, tempList.get(i).y);
            }
            if (i != tempList.size() - 1) {
                curvePath.cubicTo((tempList.get(i).x + tempList.get(i + 1).x) / 2, tempList.get(i).y,
                        (tempList.get(i).x + tempList.get(i + 1).x) / 2, tempList.get(i + 1).y,
                        tempList.get(i + 1).x, tempList.get(i + 1).y);
            } else {
                curvePath.lineTo(tempList.get(i).x, viewHeight - itemHeight / 2);
                curvePath.lineTo(tempList.get(0).x, viewHeight - itemHeight / 2);
                curvePath.close();
            }
        }
        return curvePath;
    }

}

