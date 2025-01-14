package com.ekalips.fancybuttonproj;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import me.zhanghai.android.materialprogressbar.IndeterminateCircularProgressDrawable;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by Ekalips on 1/18/17.
 */
public class FancyButton extends FrameLayout {

    interface AnimationEndListener {

        void animationEnded();
    }

    private static final String TAG = FancyButton.class.getSimpleName();

    public FancyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FancyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FancyButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private Paint strokePaint, fillPaint;
    private MaterialProgressBar bar;
    private TextView view;

    /*
        0 - stroke
        1 - fill
        2 - stroke and fill
     */
    private int style;
    private boolean hideAfterCollapse = true;
    private boolean isExpanded = true;
    private AnimationEndListener listener;

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setListener(AnimationEndListener listener) {
        this.listener = listener;
    }

    private AnimatorListenerAdapter hideProgressBarListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            bar.setVisibility(INVISIBLE);
        }
    };

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    private int initPadd = 16;
    private int trueSixtee = 60;


    public void init(Context context, AttributeSet attrs) {

        this.setClickable(true);
        this.setWillNotDraw(false);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FancyButton);
        String text = ta.getString(R.styleable.FancyButton_f_text);
        int strokeColor = ta.getColor(R.styleable.FancyButton_f_strokeColor, Color.BLACK);
        int fillColor = ta.getColor(R.styleable.FancyButton_f_fillColor, Color.TRANSPARENT);
        int textColor = ta.getColor(R.styleable.FancyButton_f_textColor, Color.BLACK);
        int progressColor = ta.getColor(R.styleable.FancyButton_f_progressColor, Color.BLACK);
        int strokeWidth = ta.getInt(R.styleable.FancyButton_f_strokeWidth, -1);

        //Adding new Attribute Background for BorderRadius
        /*
         * If the Background is not Null ... It must have BorderRadius and Color
         * If it doesnt Have a BorderRadius then no need to add it
         * if u gonna add a stroke in the background .. then u dont need to add it with_f_strokeWidth or f_strokeCollor
         * */

        int background = ta.getResourceId(R.styleable.FancyButton_f_background, -1);
        //TextAppearance
        int textAppearance = ta.getResourceId(R.styleable.FancyButton_f_textAppearance, -1);

        //Textfont from Assets
        String textFont = ta.getString(R.styleable.FancyButton_f_textFont);

        if (strokeWidth == -1) {
            strokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                context.getResources().getDisplayMetrics());
        }

        boolean capsText = ta.getBoolean(R.styleable.FancyButton_f_capsText, true);
        float textSize = ta.getDimensionPixelSize(R.styleable.FancyButton_f_textSize, -1);
        if (textSize == -1) {
            textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16,
                context.getResources().getDisplayMetrics());
        }
        if (capsText && text != null) {
            text = text.toUpperCase();
        }
        String temp = ta.getString(R.styleable.FancyButton_f_btnStyle);
        if (temp != null) {
            style = Integer.parseInt(temp);
        }
        hideAfterCollapse = ta.getBoolean(R.styleable.FancyButton_f_hideFillAfterCollapse, true);
        ta.recycle();

        view = new TextView(context, attrs, android.R.attr.borderlessButtonStyle);
        view.setClickable(false);
        view.setFocusable(false);
        view.setTextColor(textColor);

        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        view.setText(text);
        //Checking if background is Null  or not
        if (background != -1) {
            view.setBackground(getResources().getDrawable(background));
        }

        //Checking if textFont is Null
        if (textFont != null) {
            AssetManager manager = getResources().getAssets();
            InputStream inputStream = null;
            try {
                inputStream = manager.open(textFont);
                Typeface face = Typeface.createFromAsset(context.getAssets(),
                    textFont);
                view.setTypeface(face);
            } catch (IOException ex) {
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        //Checking if textAppearance is not null
        if (textAppearance != -1)
            try {
                view.setTextAppearance(context,textAppearance);
            } catch (Exception e)
            {
                Log.e("Error",e.getMessage());
            }

        this.addView(view);

        bar = new MaterialProgressBar(context);
        FrameLayout.LayoutParams barParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barParams.gravity = Gravity.CENTER;
        bar.setLayoutParams(barParams);
        bar.setIndeterminate(true);

        // fixes pre-Lollipop progressBar indeterminateDrawable tinting
        IndeterminateCircularProgressDrawable drawable = new IndeterminateCircularProgressDrawable(
            context);
        drawable.setTint(progressColor);
        bar.setIndeterminateDrawable(drawable);

        bar.setVisibility(INVISIBLE);
        this.addView(bar);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(strokeColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeWidth(strokeWidth);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);

        initPadd = Utils.dp2px(context.getResources(), 5);
        trueSixtee = Utils.dp2px(context.getResources(), 20);


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (state == State.normal) {
            if (style == 0) {
                canvas.drawPath(Utils
                    .composeRoundedRectPath(initPadd, initPadd, canvas.getWidth() - initPadd,
                        canvas.getHeight() - initPadd, initPadd), strokePaint);
            } else if (style == 1) {
                canvas.drawPath(Utils
                    .composeRoundedRectPath(initPadd, initPadd, canvas.getWidth() - initPadd,
                        canvas.getHeight() - initPadd, initPadd), fillPaint);
            } else if (style == 2) {
                canvas.drawPath(Utils
                    .composeRoundedRectPath(initPadd, initPadd, canvas.getWidth() - initPadd,
                        canvas.getHeight() - initPadd, initPadd), fillPaint);
                canvas.drawPath(Utils
                    .composeRoundedRectPath(initPadd, initPadd, canvas.getWidth() - initPadd,
                        canvas.getHeight() - initPadd, initPadd), strokePaint);
            }
            destLeft = (this.getRight() - this.getLeft()) / 2 - trueSixtee;
            destRight = (this.getRight() - this.getLeft()) / 2 + trueSixtee;
            destTop = (this.getBottom() - this.getTop()) / 2 - trueSixtee;
            destBot = (this.getBottom() - this.getTop()) / 2 + trueSixtee;
            circleR = Math.abs(destLeft - destRight);
            ObjectAnimator animator = ObjectAnimator.ofFloat(bar, "alpha", 1, 0);
            animator.addListener(hideProgressBarListener);
            animator.setDuration(0);
            animator.start();
        } else if (state == State.shrieked) {
            if (!hideAfterCollapse) {
                if (style == 1 || style == 2) {
                    canvas.drawCircle(destRight - ((destRight - destLeft) / 2),
                        destBot - ((destBot - destTop) / 2), circleR / 2, fillPaint);
                }

            }
            ObjectAnimator animator = ObjectAnimator.ofFloat(bar, "alpha", 0, 1);
            animator.setDuration(0);
            animator.start();
            bar.setVisibility(VISIBLE);
        } else {
            if (state == State.shrink) {
                if (style == 0) {
                    canvas.drawPath(
                        Utils.composeRoundedRectPath((float) (0 + nowPadW), (float) (0 + nowPadH),
                            (float) (canvas.getWidth() - nowPadW),
                            (float) (canvas.getHeight() - nowPadH),
                            (float) nowRad), strokePaint);
                } else if (style == 1) {
                    canvas
                        .drawPath(
                            Utils.composeRoundedRectPath((float) (0 + nowPadW), (float) (0 + nowPadH),
                                (float) (canvas.getWidth() - nowPadW),
                                (float) (canvas.getHeight() - nowPadH),
                                (float) nowRad), fillPaint);
                } else if (style == 2) {
                    canvas
                        .drawPath(
                            Utils.composeRoundedRectPath((float) (0 + nowPadW), (float) (0 + nowPadH),
                                (float) (canvas.getWidth() - nowPadW),
                                (float) (canvas.getHeight() - nowPadH),
                                (float) nowRad), fillPaint);
                    canvas
                        .drawPath(
                            Utils.composeRoundedRectPath((float) (0 + nowPadW), (float) (0 + nowPadH),
                                (float) (canvas.getWidth() - nowPadW),
                                (float) (canvas.getHeight() - nowPadH),
                                (float) nowRad), strokePaint);
                }
            }
            if (state == State.back) {
                if (style == 0) {
                    canvas.drawPath(
                        Utils.composeRoundedRectPath((float) (0 + nowPadW), (float) (0 + nowPadH),
                            (float) (canvas.getWidth() - nowPadW), (float) (canvas.getHeight() - nowPadH),
                            (float) nowRad), strokePaint);
                } else if (style == 1) {
                    canvas
                        .drawPath(
                            Utils.composeRoundedRectPath((float) (0 + nowPadW), (float) (0 + nowPadH),
                                (float) (canvas.getWidth() - nowPadW),
                                (float) (canvas.getHeight() - nowPadH),
                                (float) nowRad), fillPaint);
                } else if (style == 2) {
                    canvas
                        .drawPath(
                            Utils.composeRoundedRectPath((float) (0 + nowPadW), (float) (0 + nowPadH),
                                (float) (canvas.getWidth() - nowPadW),
                                (float) (canvas.getHeight() - nowPadH),
                                (float) nowRad), fillPaint);
                    canvas
                        .drawPath(
                            Utils.composeRoundedRectPath((float) (0 + nowPadW), (float) (0 + nowPadH),
                                (float) (canvas.getWidth() - nowPadW),
                                (float) (canvas.getHeight() - nowPadH),
                                (float) nowRad), strokePaint);
                }
                if (bar.getVisibility() != INVISIBLE) {
                    bar.setVisibility(INVISIBLE);
                }
            }
        }
    }


    private double nowPadW = initPadd;
    private double nowPadH = initPadd;
    private double nowRad = initPadd;
    private float destLeft, destTop, destRight, destBot, circleR;
    private State state = FancyButton.State.normal;


    ValueAnimator animator;

    public void expand() {
//        if (state == State.shrieked){
        if (state == State.normal) {
            return;
        }
        if (animator != null) {
            animator.removeAllUpdateListeners();
            animator.cancel();
        }
        state = State.back;
        animator = ValueAnimator.ofInt(0, 255);
        animator.setDuration(200);

        final float addWVal = (float) Math.abs(0 - nowPadW + initPadd) / 15f;
        final float addHVal = (float) Math.abs(0 - nowPadH + initPadd) / 15f;

        nowRad = circleR;
        final float addRad = Math.abs(circleR - 10f) / 15f;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                FancyButton.this.invalidate();
                nowPadH -= addHVal;
                nowPadW -= addWVal;
                nowRad -= addRad;
                if (hideAfterCollapse) {
                    if (strokePaint.getColor() != Color.TRANSPARENT) {
                        strokePaint.setAlpha((Integer) valueAnimator.getAnimatedValue());
                    }
                    if (fillPaint.getColor() != Color.TRANSPARENT) {
                        fillPaint.setAlpha((Integer) valueAnimator.getAnimatedValue());
                    }
                }
                view.setAlpha((int) valueAnimator.getAnimatedValue() / 255f);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                state = State.normal;
                if (listener != null) {
                    listener.animationEnded();
                }
                isExpanded = true;
            }
        });
        animator.start();
//        }
    }

    public void collapse() {
//        if (state == State.normal){
        if (state == State.shrieked) {
            return;
        }
        if (animator != null) {
            animator.removeAllUpdateListeners();
            animator.cancel();
        }
        state = State.shrink;
        nowPadW = initPadd;
        nowPadH = initPadd;
        nowRad = initPadd;
        animator = ValueAnimator.ofInt(255, 0);
        animator.setDuration(200);

        final float addWVal = Math.abs(initPadd - destLeft) / 15f;
        final float addHVal = Math.abs(initPadd - destTop) / 15f;
        final float addRad = circleR / 15f;

        isExpanded = false;

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                FancyButton.this.invalidate();
                nowPadH += addHVal;
                nowPadW += addWVal;
                nowRad += addRad;
                if (hideAfterCollapse) {
                    if (strokePaint.getColor() != Color.TRANSPARENT) {
                        strokePaint.setAlpha((Integer) valueAnimator.getAnimatedValue());
                    }
                    if (fillPaint.getColor() != Color.TRANSPARENT) {
                        fillPaint.setAlpha((Integer) valueAnimator.getAnimatedValue());
                    }
                }
                view.setAlpha((int) valueAnimator.getAnimatedValue() / 255f);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                state = State.shrieked;
                if (listener != null) {
                    listener.animationEnded();
                }
            }
        });
        animator.start();
//        }
    }

    public void setText(String text) {
        view.setText(text);
    }

    private enum State {normal, shrink, back, shrieked}
}
