package com.sdsmdg.harjot.croller;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.sdsmdg.harjot.croller.utilities.Utils;

public class Croller extends View {

    private static final String PROPERTY_FADE_RATIO = "property_fade_ratio";

    private float midx, midy;
    private Paint textPaint, circlePaint, circlePaint2, linePaint,nextCirclePaint;
    private float currdeg = 0, deg = 0, downdeg = 0;

    private boolean isContinuous = false;

    private int backCircleColor = Color.parseColor("#222222");
    private int mainCircleColor = Color.parseColor("#000000");
    private int indicatorColor = Color.parseColor("#FFA036");
    private int progressPrimaryColor = Color.parseColor("#FFA036");
    private int progressSecondaryColor = Color.parseColor("#111111");

    private int backCircleDisabledColor = Color.parseColor("#82222222");
    private int mainCircleDisabledColor = Color.parseColor("#82000000");
    private int indicatorDisabledColor = Color.parseColor("#82FFA036");
    private int progressPrimaryDisabledColor = Color.parseColor("#82FFA036");
    private int progressSecondaryDisabledColor = Color.parseColor("#82111111");

    private float progressPrimaryCircleSize = -1;
    private float progressSecondaryCircleSize = -1;

    private float progressPrimaryStrokeWidth = 25;
    private float progressNextPrimaryStrokeWidth = 5;
    private float progressSecondaryStrokeWidth = 10;

    private float mainCircleRadius = -1;
    private float backCircleRadius = -1;
    private float progressRadius = -1;

    private int max = 26;
    private int min = 0;

    private float indicatorWidth = 7;

    private String label = "Label";
    private String labelFont;
    private int labelStyle = 0;
    private float labelSize = 14;
    private int labelColor = Color.WHITE;

    private int labelDisabledColor = Color.BLACK;

    private int startOffset = 30;
    private int startOffset2 = 0;
    private int sweepAngle = -1;

    private boolean isEnabled = true;

    private boolean isAntiClockwise = false;

    private boolean startEventSent = false;

    RectF oval;

    private onProgressChangedListener mProgressChangeListener;
    private OnCrollerChangeListener mCrollerChangeListener;
    private ValueAnimator mAnimator;
    private float mFadeRatio = 1;
    private boolean mProgressMode = true;
    private Paint circlePaint2Copy;

    public interface onProgressChangedListener {
        void onProgressChanged(int progress);
    }

    public void setOnProgressChangedListener(onProgressChangedListener mProgressChangeListener) {
        this.mProgressChangeListener = mProgressChangeListener;
    }

    public void setOnCrollerChangeListener(OnCrollerChangeListener mCrollerChangeListener) {
        this.mCrollerChangeListener = mCrollerChangeListener;
    }

    public Croller(Context context) {
        super(context);
        init();
    }

    public Croller(Context context, AttributeSet attrs) {
        super(context, attrs);
        initXMLAttrs(context, attrs);
        init();
    }

    public Croller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initXMLAttrs(context, attrs);
        init();
    }

    private void init() {

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(labelSize);

        generateTypeface();

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(progressSecondaryStrokeWidth);
        circlePaint.setStyle(Paint.Style.FILL);

        circlePaint2 = new Paint();
        circlePaint2.setAntiAlias(true);
        circlePaint2.setStrokeWidth(progressPrimaryStrokeWidth);
        circlePaint2.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(indicatorWidth);

        nextCirclePaint = new Paint();
        nextCirclePaint.setAntiAlias(true);
        nextCirclePaint.setStrokeWidth(progressNextPrimaryStrokeWidth);
        nextCirclePaint.setStyle(Paint.Style.STROKE);



        if (isEnabled) {
            circlePaint2.setColor(progressPrimaryColor);
            circlePaint.setColor(progressSecondaryColor);
            linePaint.setColor(indicatorColor);
            textPaint.setColor(labelColor);
            nextCirclePaint.setColor(progressPrimaryColor);

        } else {
            circlePaint2.setColor(progressPrimaryDisabledColor);
            nextCirclePaint.setColor(progressPrimaryDisabledColor);
            circlePaint.setColor(progressSecondaryDisabledColor);
            linePaint.setColor(indicatorDisabledColor);
            textPaint.setColor(labelDisabledColor);
        }
        circlePaint2Copy = new Paint(circlePaint2);
        oval = new RectF();

    }

    private void generateTypeface() {
        Typeface plainLabel = Typeface.DEFAULT;
        if (getLabelFont() != null && !getLabelFont().isEmpty()) {
            AssetManager assetMgr = getContext().getAssets();
            plainLabel = Typeface.createFromAsset(assetMgr, getLabelFont());
        }

        switch (getLabelStyle()) {
            case 0:
                textPaint.setTypeface(plainLabel);
                break;
            case 1:
                textPaint.setTypeface(Typeface.create(plainLabel, Typeface.BOLD));
                break;
            case 2:
                textPaint.setTypeface(Typeface.create(plainLabel, Typeface.ITALIC));
                break;
            case 3:
                textPaint.setTypeface(Typeface.create(plainLabel, Typeface.BOLD_ITALIC));
                break;

        }

    }

    private void initXMLAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Croller);

        setEnabled(a.getBoolean(R.styleable.Croller_enabled, true));
        setProgress(a.getInt(R.styleable.Croller_start_progress, 1));
        setLabel(a.getString(R.styleable.Croller_label));

        setBackCircleColor(a.getColor(R.styleable.Croller_back_circle_color, backCircleColor));
        setMainCircleColor(a.getColor(R.styleable.Croller_main_circle_color, mainCircleColor));
        setIndicatorColor(a.getColor(R.styleable.Croller_indicator_color, indicatorColor));
        setProgressPrimaryColor(a.getColor(R.styleable.Croller_progress_primary_color, progressPrimaryColor));
        setProgressSecondaryColor(a.getColor(R.styleable.Croller_progress_secondary_color, progressSecondaryColor));

        setBackCircleDisabledColor(a.getColor(R.styleable.Croller_back_circle_disable_color, backCircleDisabledColor));
        setMainCircleDisabledColor(a.getColor(R.styleable.Croller_main_circle_disable_color, mainCircleDisabledColor));
        setIndicatorDisabledColor(a.getColor(R.styleable.Croller_indicator_disable_color, indicatorDisabledColor));
        setProgressPrimaryDisabledColor(a.getColor(R.styleable.Croller_progress_primary_disable_color, progressPrimaryDisabledColor));
        setProgressSecondaryDisabledColor(a.getColor(R.styleable.Croller_progress_secondary_disable_color, progressSecondaryDisabledColor));

        setLabelSize(a.getDimension(R.styleable.Croller_label_size, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                labelSize, getResources().getDisplayMetrics())));
        setLabelColor(a.getColor(R.styleable.Croller_label_color, labelColor));
        setlabelDisabledColor(a.getColor(R.styleable.Croller_label_disabled_color, labelDisabledColor));
        setLabelFont(a.getString(R.styleable.Croller_label_font));
        setLabelStyle(a.getInt(R.styleable.Croller_label_style, 0));
        setIndicatorWidth(a.getFloat(R.styleable.Croller_indicator_width, 7));
        setIsContinuous(a.getBoolean(R.styleable.Croller_is_continuous, false));

        setProgressPrimaryCircleSize(a.getDimension(R.styleable.Croller_progress_primary_circle_size, -1));
        setProgressSecondaryCircleSize(a.getDimension(R.styleable.Croller_progress_secondary_circle_size, -1));
        setProgressPrimaryStrokeWidth(a.getDimension(R.styleable.Croller_progress_primary_stroke_width, 5));
        setProgressSecondaryStrokeWidth(a.getDimension(R.styleable.Croller_progress_secondary_stroke_width, 10));


        setSweepAngle(a.getInt(R.styleable.Croller_sweep_angle, -1));
        setStartOffset(a.getInt(R.styleable.Croller_start_offset, 30));
        setMax(a.getInt(R.styleable.Croller_max, 25));
        setMin(a.getInt(R.styleable.Croller_min, 0));
        deg = min;
        setBackCircleRadius(a.getFloat(R.styleable.Croller_back_circle_radius, -1));
        setProgressRadius(a.getFloat(R.styleable.Croller_progress_radius, -1));
        setAntiClockwise(a.getBoolean(R.styleable.Croller_anticlockwise, false));
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int minWidth = (int) Utils.convertDpToPixel(160, getContext());
        int minHeight = (int) Utils.convertDpToPixel(160, getContext());

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(minWidth, widthSize);
        } else {
            // only in case of ScrollViews, otherwise MeasureSpec.UNSPECIFIED is never triggered
            // If width is wrap_content i.e. MeasureSpec.UNSPECIFIED, then make width equal to height
            width = heightSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(minHeight, heightSize);
        } else {
            // only in case of ScrollViews, otherwise MeasureSpec.UNSPECIFIED is never triggered
            // If height is wrap_content i.e. MeasureSpec.UNSPECIFIED, then make height equal to width
            height = widthSize;
        }

        if (widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED) {
            width = minWidth;
            height = minHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        midx = getWidth() / 2;
        midy = getHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mProgressChangeListener != null)
            mProgressChangeListener.onProgressChanged((int) (deg - 2));

        if (mCrollerChangeListener != null)
            mCrollerChangeListener.onProgressChanged(this, (int) (deg - 2));

        if (isEnabled) {
            circlePaint2.setColor(progressPrimaryColor);
            circlePaint.setColor(progressSecondaryColor);
            linePaint.setColor(indicatorColor);
            textPaint.setColor(labelColor);
            nextCirclePaint.setColor(progressPrimaryColor);
            circlePaint2Copy.setColor(progressPrimaryColor);
        } else {
            circlePaint2.setColor(progressPrimaryDisabledColor);
            circlePaint.setColor(progressSecondaryDisabledColor);
            linePaint.setColor(indicatorDisabledColor);
            textPaint.setColor(labelDisabledColor);
            nextCirclePaint.setColor(progressPrimaryDisabledColor);
            circlePaint2Copy.setColor(progressPrimaryDisabledColor);
        }

        if (!isContinuous) {

            startOffset2 = startOffset;

            linePaint.setStrokeWidth(indicatorWidth);
            textPaint.setTextSize(labelSize);

            int radius = (int) (Math.min(midx, midy) * ((float) 14.5 / 16));

            if (sweepAngle == -1) {
                sweepAngle = 360;
            }

            if (mainCircleRadius == -1) {
                mainCircleRadius = radius * ((float) 11 / 15);
            }
            if (backCircleRadius == -1) {
                backCircleRadius = radius * ((float) 13 / 15);
            }
            if (progressRadius == -1) {
                progressRadius = radius;
            }

            float x, y;
            float deg2 = Math.max(0, deg);


            drawSecondaryProgress(canvas, radius, deg2);
            drawPrimaryProgress(canvas, deg2);


            double tmp2 = ((float) sweepAngle / 360) * (2 * Math.PI / max) * (deg -1) - Math.PI/2 + Math.PI/max + 2 * ( startOffset2) * Math.PI / 360;


            if (isAntiClockwise)  tmp2 = 1.0f - tmp2;


            float x1 = midx + (float) (radius * ((float) 2 / 5) * Math.cos(tmp2));
            float y1 = midy + (float) (radius * ((float) 2 / 5) * Math.sin(tmp2));
            float x2 = midx + (float) (radius * ((float) 3 / 5) * Math.cos(tmp2));
            float y2 = midy + (float) (radius * ((float) 3 / 5) * Math.sin(tmp2));

            if (isEnabled)  circlePaint.setColor(backCircleColor);
            else            circlePaint.setColor(backCircleDisabledColor);


            canvas.drawCircle(midx, midy, backCircleRadius, circlePaint);
            if (isEnabled)
                circlePaint.setColor(mainCircleColor);
            else
                circlePaint.setColor(mainCircleDisabledColor);

            canvas.drawCircle(midx, midy, mainCircleRadius, circlePaint);
            canvas.drawText(label, midx, midy + (float) (radius * 1.1)-textPaint.getFontMetrics().descent, textPaint);
            canvas.drawLine(x1, y1, x2, y2, linePaint);

        } else {

            int radius = (int) (Math.min(midx, midy) * ((float) 14.5 / 16));

            if (sweepAngle == -1) {
                sweepAngle = 360;
            }

            if (mainCircleRadius == -1) {
                mainCircleRadius = radius * ((float) 11 / 15);
            }
            if (backCircleRadius == -1) {
                backCircleRadius = radius * ((float) 13 / 15);
            }
            if (progressRadius == -1) {
                progressRadius = radius;
            }

            circlePaint.setStrokeWidth(progressSecondaryStrokeWidth);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint2.setStrokeWidth(progressPrimaryStrokeWidth);
            circlePaint2.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(indicatorWidth);
            textPaint.setTextSize(labelSize);

            float deg3 = Math.min(deg, max + 2);

            oval.set(midx - progressRadius, midy - progressRadius, midx + progressRadius, midy + progressRadius);

            canvas.drawArc(oval, (float) 90 + startOffset, (float) sweepAngle, false, circlePaint);
            if (isAntiClockwise) {
                canvas.drawArc(oval, (float) 90 - startOffset, -1 * ((deg3 - 2) * ((float) sweepAngle / max)), false, circlePaint2);
            } else {
                canvas.drawArc(oval, (float) 90 + startOffset, ((deg3 - 2) * ((float) sweepAngle / max)), false, circlePaint2);
            }

            float tmp2 = ((float) startOffset / 360) + (((float) sweepAngle / 360) * ((deg - 2) / (max)));

            if (isAntiClockwise) {
                tmp2 = 1.0f - tmp2;
            }

            float x1 = midx + (float) (radius * ((float) 2 / 5) * Math.sin(2 * Math.PI * (1.0 - tmp2)));
            float y1 = midy + (float) (radius * ((float) 2 / 5) * Math.cos(2 * Math.PI * (1.0 - tmp2)));
            float x2 = midx + (float) (radius * ((float) 3 / 5) * Math.sin(2 * Math.PI * (1.0 - tmp2)));
            float y2 = midy + (float) (radius * ((float) 3 / 5) * Math.cos(2 * Math.PI * (1.0 - tmp2)));

            circlePaint.setStyle(Paint.Style.FILL);

            if (isEnabled)
                circlePaint.setColor(backCircleColor);
            else
                circlePaint.setColor(backCircleDisabledColor);
            canvas.drawCircle(midx, midy, backCircleRadius, circlePaint);
            if (isEnabled)
                circlePaint.setColor(mainCircleColor);
            else
                circlePaint.setColor(mainCircleDisabledColor);
            canvas.drawCircle(midx, midy, mainCircleRadius, circlePaint);
            canvas.drawText(label, midx, midy + (float) (radius * 1.1)-textPaint.getFontMetrics().descent, textPaint);
            canvas.drawLine(x1, y1, x2, y2, linePaint);
        }
    }

    private void drawSecondaryProgress(Canvas canvas, int radius, float deg2) {
        float x;
        float y;

        for (int i = 0; i < max; i++) {

            double tmp =   ((float) sweepAngle / 360) * (2 * Math.PI / max) * i - Math.PI/2 + Math.PI/max + 2 * ( startOffset2) * Math.PI / 360;

            if (isAntiClockwise) {
                tmp = 1.0f - tmp;
            }

            x = midx + (float) (progressRadius * Math.cos(tmp));
            y = midy + (float) (progressRadius * Math.sin(tmp));

            if (progressSecondaryCircleSize == -1) {
                float secondCircleRadius = (float) (Math.PI * radius * sweepAngle / (2 * max * 360));
                canvas.drawCircle(x, y, secondCircleRadius, circlePaint);
            }
            else  canvas.drawCircle(x, y, progressSecondaryCircleSize, circlePaint);

            if((i == deg2 || i == deg2 -1) && deg2 != max && mProgressMode){
                if (progressSecondaryCircleSize == -1) {
                    float secondCircleRadius = (float) (Math.PI * radius * sweepAngle / (2 * max * 360));
                    canvas.drawCircle(x, y, secondCircleRadius, nextCirclePaint);
                }
                else  canvas.drawCircle(x, y, progressSecondaryCircleSize, nextCirclePaint);
            }

            else if(i== deg2 && deg2 != 0 && !mProgressMode){
                float circleRatio=  255;

                nextCirclePaint.setAlpha((int) (circleRatio ));
                if (progressSecondaryCircleSize == -1) {
                    float secondCircleRadius = (float) (Math.PI * radius * sweepAngle / (2 * max * 360));
                    canvas.drawCircle(x, y, secondCircleRadius, nextCirclePaint);
                }
                else  canvas.drawCircle(x, y, progressSecondaryCircleSize, nextCirclePaint);
            }

        }
    }

    private void drawPrimaryProgress(Canvas canvas, float deg2) {
        float x;
        float y;
        for (int i = 0; i <= deg2; i++) {

            double tmp = ((float) sweepAngle / 360) *  (2 * Math.PI / max) * i - Math.PI/2 + Math.PI/max + 2 * ( startOffset2) * Math.PI / 360;


            if (isAntiClockwise) {
                tmp = 1.0f - tmp;
            }

            x = midx + (float) (progressRadius * Math.cos(tmp));
            y = midy + (float) (progressRadius * Math.sin(tmp));
            float  circleRadius;

            float circleRatio;
            if(i== deg2 -1 && mProgressMode || (i== deg2 && !mProgressMode)) {
                circleRatio = mFadeRatio * 255;
            }
            else if(i != deg2)  circleRatio = 255;
            else circleRatio = 0;

            circlePaint2Copy.setAlpha((int) (circleRatio));

            if (progressPrimaryCircleSize == -1)
                circleRadius= (progressRadius / 15 * ((float) 20 / max) * ((float) sweepAngle / 270));

            else circleRadius = progressPrimaryCircleSize;

            canvas.drawCircle(x, y, circleRadius, circlePaint2Copy);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if (!isEnabled)
            return false;

        if (Utils.getDistance(e.getX(), e.getY(), midx, midy) > Math.max(mainCircleRadius, Math.max(backCircleRadius, progressRadius))) {
            if (startEventSent && mCrollerChangeListener != null) {
                mCrollerChangeListener.onStopTrackingTouch(this);
                startEventSent = false;
            }
            return super.onTouchEvent(e);
        }

        if (e.getAction() == MotionEvent.ACTION_DOWN) {

            float dx = e.getX() - midx;
            float dy = e.getY() - midy;
            downdeg = (float)((Math.atan2(dy, dx) + Math.PI/2)* 180 / Math.PI);

            downdeg =  downdeg - startOffset2 -  180 /max ;
            if (downdeg < 0) {
                downdeg += 360;
            }
            downdeg = (float) Math.floor((downdeg / sweepAngle) * max);
            Log.d("DEBUG",  "DOWN DEG is " + downdeg);
            if (mCrollerChangeListener != null) {
                mCrollerChangeListener.onStartTrackingTouch(this);
                startEventSent = true;
            }

            return true;
        }
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = e.getX() - midx;
            float dy = e.getY() - midy;
            currdeg =  (float)((Math.atan2(dy, dx) + Math.PI/2)* 180 / Math.PI);

            currdeg =  currdeg - startOffset2 -  180 /max ;
            if (currdeg < 0) {
                currdeg += 360;
            }
            currdeg = (float) Math.floor((currdeg / sweepAngle) * max);
            Log.d("DEBUG",  "MOVE DEG is " + currdeg);

            if ((currdeg / (max )) > 0.75f && ((downdeg - 0) / (max )) < 0.25f) {
                if (isAntiClockwise) {
                    deg++;
                    if (deg > max) {
                        deg = max;
                    }
                } else {
                    deg--;
                    if (deg < (min)) {
                        deg = (min);
                    }
                }
            } else if ((downdeg / (max)) > 0.75f && ((currdeg - 0) / (max )) < 0.25f) {
                if (isAntiClockwise) {
                    deg--;
                    if (deg < (min)) {
                        deg = (min);
                    }
                } else {
                    deg++;
                    if (deg > max) {
                        deg = max;
                    }
                }
            } else {
                if (isAntiClockwise) {
                    deg -= (currdeg - downdeg);
                } else {
                    deg += (currdeg - downdeg);
                }
                if (deg > max) {
                    deg = max;
                }
                if (deg < (min)) {
                    deg = (min);
                }
            }

            downdeg = currdeg;
            Log.d("DEBUG",  "FINAL DEG is " + deg);
            invalidate();
            return true;

        }
        if (e.getAction() == MotionEvent.ACTION_UP) {
            if (mCrollerChangeListener != null) {
                mCrollerChangeListener.onStopTrackingTouch(this);
                startEventSent = false;
            }
            return true;
        }
        return super.onTouchEvent(e);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getParent() != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        invalidate();
    }

    public int getProgress() {
        return (int) (deg);
    }

    public void setProgress(int x) {

        if(x > max || x < min) return;
        PropertyValuesHolder propertyRadius;
        if(deg < x) {
            propertyRadius  = PropertyValuesHolder.ofFloat(PROPERTY_FADE_RATIO, 0, 1);
            mProgressMode  = true;
        }
        else        {
            propertyRadius  = PropertyValuesHolder.ofFloat(PROPERTY_FADE_RATIO, 1, 0);
            mProgressMode  = false;
        }

        deg = x;
        if(mAnimator != null)   mAnimator.cancel();
        mAnimator = new ValueAnimator();
        mAnimator.setInterpolator(new FastOutLinearInInterpolator());
        mAnimator.setValues(propertyRadius);
        mAnimator.setDuration(200);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFadeRatio = (float) animation.getAnimatedValue(PROPERTY_FADE_RATIO);
                invalidate();
            }
        });
        mAnimator.start();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String txt) {
        label = txt;
        invalidate();
    }

    public int getBackCircleColor() {
        return backCircleColor;
    }

    public void setBackCircleColor(int backCircleColor) {
        this.backCircleColor = backCircleColor;
        invalidate();
    }

    public int getMainCircleColor() {
        return mainCircleColor;
    }

    public void setMainCircleColor(int mainCircleColor) {
        this.mainCircleColor = mainCircleColor;
        invalidate();
    }

    public int getIndicatorColor() {
        return indicatorColor;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        invalidate();
    }

    public int getProgressPrimaryColor() {
        return progressPrimaryColor;
    }

    public void setProgressPrimaryColor(int progressPrimaryColor) {
        this.progressPrimaryColor = progressPrimaryColor;
        invalidate();
    }

    public int getProgressSecondaryColor() {
        return progressSecondaryColor;
    }

    public void setProgressSecondaryColor(int progressSecondaryColor) {
        this.progressSecondaryColor = progressSecondaryColor;
        invalidate();
    }

    public int getBackCircleDisabledColor() {
        return backCircleDisabledColor;
    }

    public void setBackCircleDisabledColor(int backCircleDisabledColor) {
        this.backCircleDisabledColor = backCircleDisabledColor;
        invalidate();
    }

    public int getMainCircleDisabledColor() {
        return mainCircleDisabledColor;
    }

    public void setMainCircleDisabledColor(int mainCircleDisabledColor) {
        this.mainCircleDisabledColor = mainCircleDisabledColor;
        invalidate();
    }

    public int getIndicatorDisabledColor() {
        return indicatorDisabledColor;
    }

    public void setIndicatorDisabledColor(int indicatorDisabledColor) {
        this.indicatorDisabledColor = indicatorDisabledColor;
        invalidate();
    }

    public int getProgressPrimaryDisabledColor() {
        return progressPrimaryDisabledColor;
    }

    public void setProgressPrimaryDisabledColor(int progressPrimaryDisabledColor) {
        this.progressPrimaryDisabledColor = progressPrimaryDisabledColor;
        invalidate();
    }

    public int getProgressSecondaryDisabledColor() {
        return progressSecondaryDisabledColor;
    }

    public void setProgressSecondaryDisabledColor(int progressSecondaryDisabledColor) {
        this.progressSecondaryDisabledColor = progressSecondaryDisabledColor;
        invalidate();
    }

    public float getLabelSize() {
        return labelSize;
    }

    public void setLabelSize(float labelSize) {
        this.labelSize = labelSize;
        invalidate();
    }

    public int getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(int labelColor) {
        this.labelColor = labelColor;
        invalidate();
    }

    public int getlabelDisabledColor() {
        return labelDisabledColor;
    }

    public void setlabelDisabledColor(int labelDisabledColor) {
        this.labelDisabledColor = labelDisabledColor;
        invalidate();
    }

    public String getLabelFont() {
        return labelFont;
    }

    public void setLabelFont(String labelFont) {
        this.labelFont = labelFont;
        if (textPaint != null)
            generateTypeface();
        invalidate();
    }

    public int getLabelStyle() {
        return labelStyle;
    }

    public void setLabelStyle(int labelStyle) {
        this.labelStyle = labelStyle;
        invalidate();
    }

    public float getIndicatorWidth() {
        return indicatorWidth;
    }

    public void setIndicatorWidth(float indicatorWidth) {
        this.indicatorWidth = indicatorWidth;
        invalidate();
    }

    public boolean isContinuous() {
        return isContinuous;
    }

    public void setIsContinuous(boolean isContinuous) {
        this.isContinuous = isContinuous;
        invalidate();
    }

    public float getProgressPrimaryCircleSize() {
        return progressPrimaryCircleSize;
    }

    public void setProgressPrimaryCircleSize(float progressPrimaryCircleSize) {
        if(progressPrimaryCircleSize != -1){
            this.progressPrimaryCircleSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, progressPrimaryCircleSize,getResources().getDisplayMetrics());
        }
        this.progressPrimaryCircleSize = progressPrimaryCircleSize;
        invalidate();
    }

    public float getProgressSecondaryCircleSize() {
        return progressSecondaryCircleSize;
    }

    public void setProgressSecondaryCircleSize(float progressSecondaryCircleSize) {
        if(progressSecondaryCircleSize != -1){
            this.progressSecondaryCircleSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, progressSecondaryCircleSize,getResources().getDisplayMetrics());
        }
        this.progressSecondaryCircleSize = progressSecondaryCircleSize;
        invalidate();
    }

    public float getProgressPrimaryStrokeWidth() {
        return progressPrimaryStrokeWidth;
    }

    public void setProgressPrimaryStrokeWidth(float progressPrimaryStrokeWidth) {
        this.progressPrimaryStrokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, progressPrimaryStrokeWidth,getResources().getDisplayMetrics());
        invalidate();
    }

    public float getProgressSecondaryStrokeWidth() {
        return progressSecondaryStrokeWidth;
    }

    public void setProgressSecondaryStrokeWidth(float progressSecondaryStrokeWidth) {



        this.progressSecondaryStrokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, progressSecondaryStrokeWidth,getResources().getDisplayMetrics());
        invalidate();
    }

    public int getSweepAngle() {
        return sweepAngle;
    }

    public void setSweepAngle(int sweepAngle) {

        if(sweepAngle == 0) this.sweepAngle = -1;
        else this.sweepAngle = sweepAngle;
        invalidate();
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
        invalidate();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        if (max < min) {
            this.max = min;
        } else {
            this.max = max;
        }
        invalidate();
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        if (min < 0) {
            this.min = 0;
        } else if (min > max) {
            this.min = max;
        } else {
            this.min = min;
        }
        invalidate();
    }

    public float getMainCircleRadius() {
        return mainCircleRadius;
    }

    public void setMainCircleRadius(float mainCircleRadius) {
        this.mainCircleRadius = mainCircleRadius;
        invalidate();
    }

    public float getBackCircleRadius() {
        return backCircleRadius;
    }

    public void setBackCircleRadius(float backCircleRadius) {
        this.backCircleRadius = backCircleRadius;
        invalidate();
    }

    public float getProgressRadius() {
        return progressRadius;
    }

    public void setProgressRadius(float progressRadius) {
        this.progressRadius = progressRadius;
        invalidate();
    }

    public boolean isAntiClockwise() {
        return isAntiClockwise;
    }

    public void setAntiClockwise(boolean antiClockwise) {
        isAntiClockwise = antiClockwise;
        invalidate();
    }
}
