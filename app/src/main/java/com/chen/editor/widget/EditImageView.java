package com.chen.editor.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;


import com.chen.editor.widget.action.Action;
import com.chen.editor.widget.action.BrightnessAction;
import com.chen.editor.widget.action.ContrastAction;
import com.chen.editor.widget.action.DrawLineAction;
import com.chen.editor.widget.action.ReverseX;
import com.chen.editor.widget.action.ReverseY;
import com.chen.editor.widget.action.RotateAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.chen.editor.widget.action.Action.ActionType.BRIGHTNESS;
import static com.chen.editor.widget.action.Action.ActionType.CONTRAST;
import static com.chen.editor.widget.action.Action.ActionType.DRAW_LINE;
import static com.chen.editor.widget.action.Action.ActionType.REVERSE_X;
import static com.chen.editor.widget.action.Action.ActionType.REVERSE_Y;
import static com.chen.editor.widget.action.Action.ActionType.ROTATE;


/**
 * Created by chenjc on 2018/10/17.
 */

public class EditImageView extends ImageView {
    private static String TAG = EditImageView.class.getSimpleName();
    private List<Action> mOperateAction = new ArrayList<>();
    private DrawLineAction drawLineAction;

    private enum EditState {
        IDLE,
        ROTATE,
        DRAW_LINE,
        REVERSE_Y,
        REVERSE_X,
        WITH_DRAW,
        CONTRAST,
        BRIGHTNESS
    }

    private Context mContext;

    private Bitmap mOrgainBitmap;
    private Bitmap mTempBitmap;
    private Bitmap mBrightBitmap;

    private Canvas mDrawLineCanvas;
    private Paint mLinePaint;
    private Paint mLineScalePaint;

    private float mLastX;
    private float mLastY;
    private static final int LINE_STROKE = 5;

    private Rect mDrawDesRect = new Rect();

    private String mImageSavePath = Environment.getExternalStorageDirectory().getPath() + "/pois/editImage/";

    private EditState mCurrentState = EditState.IDLE;

    public EditImageView(Context context) {
        super(context);
        init(context);
    }

    public EditImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
//        涂鸦

        initPaint();
        initBitmap();
    }

    private void initBitmap() {
        mOrgainBitmap = getBitmap(mImageSavePath + "/eye3.jpg", 1763, 1014);

        if (mOrgainBitmap == null) {
            return;
        }

        mTempBitmap = mOrgainBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    private void initPaint() {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(LINE_STROKE);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mLineScalePaint = new Paint();
        mLineScalePaint.setAntiAlias(true);
        mLineScalePaint.setColor(Color.BLACK);
        mLineScalePaint.setStrokeJoin(Paint.Join.ROUND);
        mLineScalePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mOrgainBitmap == null) {
            return;
        }

        getDrawingRect(mDrawDesRect);

        if (mCurrentState == EditState.BRIGHTNESS || mCurrentState == EditState.CONTRAST) {
            drawBitmap(canvas, mBrightBitmap, mDrawDesRect);
        } else {
            Bitmap resultBitmap = mTempBitmap.copy(Bitmap.Config.ARGB_8888, true);
            innerAdjustRGB(resultBitmap);
            drawBitmap(canvas, resultBitmap, mDrawDesRect);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);

        if (mCurrentState != EditState.DRAW_LINE || mOrgainBitmap == null) {
            return ret;
        }

        Setting setting = getDrawSetting(mTempBitmap);
        float eventX = event.getX();
        float x = (eventX - setting.getOffX()) / setting.getScale();
        float eventY = event.getY();
        float y = (eventY - setting.getOffY()) / setting.getScale();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawLineAction = new DrawLineAction();
                ret = true;
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                DrawLineAction.Line line = new DrawLineAction.Line(mLastX, mLastY, x, y);
                drawLineAction.addLine(line);

                ret = true;
                mDrawLineCanvas.drawLine(mLastX, mLastY, x, y, mLinePaint);

                mLastX = x;
                mLastY = y;
                this.postInvalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mOperateAction.add(drawLineAction);
                ret = false;
                break;
        }

        return ret;
    }

    public Bitmap getTempBitmap() {
        return mTempBitmap;
    }

    public Bitmap getScaleBitmap() {
        Bitmap origainBitmap = getBitmap(mImageSavePath + "/eye3.jpg", 1763, 1014);
        Bitmap resultBitmap = origainBitmap.copy(Bitmap.Config.ARGB_8888, true);

        recycleBitmap(origainBitmap);

        float scale = getScale(mTempBitmap, resultBitmap);
        resultBitmap = drawActionsByScale(resultBitmap, scale);
        innerAdjustRGB(resultBitmap);

        return resultBitmap;
    }

    public void rotate() {
        if (mTempBitmap == null) {
            return;
        }

        mCurrentState = EditState.ROTATE;
        RotateAction rotateAction = new RotateAction(1);
        mOperateAction.add(rotateAction);
        mTempBitmap = rotate(mTempBitmap, -90);

        invalidate();
    }

    public void drawLine() {
        if (mTempBitmap == null) {
            return;
        }

        mCurrentState = EditState.DRAW_LINE;
        mDrawLineCanvas = new Canvas(mTempBitmap);
    }

    public void withDraw() {
        if (mTempBitmap == null) {
            return;
        }

        mCurrentState = EditState.WITH_DRAW;
        recycleBitmap(mTempBitmap);
        mTempBitmap = mOrgainBitmap.copy(Bitmap.Config.ARGB_8888, true);

        if (mOperateAction.size() > 0) {
            mOperateAction.remove(mOperateAction.size() - 1);
        }

        mTempBitmap = drawActions(mTempBitmap);

        invalidate();
    }

    public void reverseX() {
        if (mTempBitmap == null) {
            return;
        }

        mCurrentState = EditState.REVERSE_X;
        mOperateAction.add(new ReverseX(1));
        mTempBitmap = reverseX(mTempBitmap);

        invalidate();
    }

    public void reverseY() {
        if (mTempBitmap == null) {
            return;
        }

        mCurrentState = EditState.REVERSE_Y;
        mOperateAction.add(new ReverseY(1));
        mTempBitmap = reverseY(mTempBitmap);

        invalidate();
    }

    public void contrast(float degree) {
        if (mTempBitmap == null) {
            return;
        }

        mCurrentState = EditState.CONTRAST;
        mBrightBitmap = mTempBitmap.copy(Bitmap.Config.ARGB_8888, true);
        float contrast = degree / 128;

        BrightnessAction lastBrightnessAction = getLastBrightnessAction();
        float brightness = 0;

        if (lastBrightnessAction != null) {
            brightness = lastBrightnessAction.getDegree();
        }

        adjustRGB(mBrightBitmap, contrast, brightness);
        invalidate();
    }

    public void brightness(float degree) {
        if (mTempBitmap == null) {
            return;
        }

        mCurrentState = EditState.BRIGHTNESS;
        mBrightBitmap = mTempBitmap.copy(Bitmap.Config.ARGB_8888, true);
        ContrastAction lastContrastAction = getLastContrastAction();

        float contrast = 1;
        if (lastContrastAction != null) {
            contrast = lastContrastAction.getDegree() / 128;
        }

        adjustRGB(mBrightBitmap, contrast, degree);

        invalidate();
    }

    public void contrastDone(int degree) {
        mCurrentState = EditState.IDLE;
        ContrastAction action = new ContrastAction(degree);
        mOperateAction.add(action);

        recycleBitmap(mBrightBitmap);

        invalidate();
    }

    public void brightnessDone(int degree) {
        mCurrentState = EditState.IDLE;
        BrightnessAction action = new BrightnessAction(degree);
        mOperateAction.add(action);

        recycleBitmap(mBrightBitmap);

        invalidate();
    }

    public void saveImage() {
        if (!ensurePathAccess(mImageSavePath) || mTempBitmap == null) {
            return;
        }

        Bitmap scaleBitmap = getScaleBitmap();

        String imageSavePath = mImageSavePath + "/" + UUID.randomUUID().toString() + ".jpg";
        Uri uri = saveToSDCard(scaleBitmap, imageSavePath);
        notifyScanImage(uri);

        recycleBitmap(scaleBitmap);
    }

    private void adjustRGB(Bitmap bitmap, float contrast, float brightness) {
        ColorMatrix colorMatrix = new ColorMatrix();
//        colorMatrix.setSaturation(size);
        colorMatrix.set(new float[]{
                contrast, 0, 0, 0,
                brightness, 0, contrast, 0,
                0, brightness, 0, 0,
                contrast, 0, brightness, 0,
                0, 0, 1, 0});

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }

    private void innerAdjustRGB(Bitmap bitmap) {
        ContrastAction lastContrastAction = getLastContrastAction();
        BrightnessAction lastBrightnessAction = getLastBrightnessAction();
        float brightness = 0;
        float contrast = 1;

        if (lastContrastAction != null) {
            contrast = lastContrastAction.getDegree() / 128;
        }

        if (lastBrightnessAction != null) {
            brightness = lastBrightnessAction.getDegree();
        }

        adjustRGB(bitmap, contrast, brightness);
    }


    private float getScale(Bitmap srcBitmap, Bitmap desBitmap) {
        int srcHeight = srcBitmap.getHeight();
        int desHeight = desBitmap.getHeight();

        float scale = desHeight / (float) srcHeight;

        return scale;
    }

    private Bitmap drawActionsByScale(Bitmap bitmap, float scale) {

        Bitmap resultBitmap = bitmap;
        List<Action> simplyActions = simplyActions(mOperateAction);

        for (Action action : simplyActions) {
            Action.ActionType currentActionType = action.getActionType();

            switch (currentActionType) {
                case ROTATE:
                    RotateAction rotateAction = (RotateAction) action;
                    int rotateDegree = getRotateDegree(rotateAction);

                    if (rotateDegree == 0) {
                        break;
                    }

                    resultBitmap = rotate(resultBitmap, rotateDegree);
                    break;
                case DRAW_LINE:
                    drawLineByScale(resultBitmap, ((DrawLineAction) action), scale);
                    break;
                case REVERSE_X:
                    ReverseX reverseX = (ReverseX) action;
                    int reverseXTimes = reverseX.getReverseXTimes();

                    if (reverseXTimes % 2 == 0) {
                        break;
                    }

                    resultBitmap = reverseX(resultBitmap);
                    break;
                case REVERSE_Y:
                    ReverseY reverseY = (ReverseY) action;
                    int reverseYTimes = reverseY.getReverseYTimes();

                    if (reverseYTimes % 2 == 0) {
                        break;
                    }

                    resultBitmap = reverseY(resultBitmap);
                    break;
                case CONTRAST:
//                    contrastInner(mTempBitmap, ((ContrastAction) action));
                    break;
            }
        }

        return resultBitmap;
    }

    private int getRotateDegree(RotateAction rotateAction) {
        int rotateTimes = rotateAction.getRotateTimes();
        int simpleTimes = rotateTimes % 4;

        return simpleTimes * -90;
    }

    private List<Action> simplyActions(List<Action> operateActions) {
        Action.ActionType currentActionType = Action.ActionType.DEFAULT;
        List<Action> resultActions = new ArrayList<>();


        for (Action action : operateActions) {
            Action.ActionType actionType = action.getActionType();

            if (actionType != currentActionType || isNeedRepeat(actionType)) {
                resultActions.add(cloneAction(action));
            } else {
                Action lastAction = getLastAction(resultActions);
                increaseRepeatCount(lastAction);
            }

            currentActionType = actionType;
        }

        return resultActions;
    }

    private Action cloneAction(Action action) {
        Action resultAction = null;

        Action.ActionType actionType = action.getActionType();
        if (actionType == REVERSE_X) {
            resultAction = new ReverseX(1);
        } else if (actionType == REVERSE_Y) {
            resultAction = new ReverseY(1);
        } else if (actionType == CONTRAST) {
            ContrastAction contrastAction = (ContrastAction) action;
            resultAction = new ContrastAction(contrastAction.getDegree());
        } else if (actionType == BRIGHTNESS) {
            BrightnessAction brightnessAction = (BrightnessAction) action;
            resultAction = new BrightnessAction(brightnessAction.getDegree());
        } else if (actionType == DRAW_LINE) {
            DrawLineAction drawLineAction = (DrawLineAction) action;
            DrawLineAction lineAction = new DrawLineAction();
            lineAction.setLines(drawLineAction.getLines());

            resultAction = lineAction;
        } else if (actionType == ROTATE) {
            resultAction = new RotateAction(1);
        }

        return resultAction;
    }


    private void increaseRepeatCount(Action action) {
        if (action == null) {
            return;
        }

        Action.ActionType actionType = action.getActionType();
        if (actionType == REVERSE_X) {
            ReverseX reverseX = (ReverseX) action;
            int reverseXTimes = reverseX.getReverseXTimes();
            reverseX.setReverseXTimes(reverseXTimes + 1);
        } else if (actionType == REVERSE_Y) {
            ReverseY reverseY = (ReverseY) action;
            int reverseYTimes = reverseY.getReverseYTimes();
            reverseY.setReverseYTimes(reverseYTimes + 1);
        } else if (actionType == ROTATE) {
            RotateAction rotateAction = (RotateAction) action;
            int rotateTimes = rotateAction.getRotateTimes();
            rotateAction.setRotateTimes(rotateTimes + 1);
        }
    }

    private Action getLastAction(List<Action> actions) {
        Action lastAction = null;

        if (actions.isEmpty()) {
            return lastAction;
        }

        int size = actions.size();
        lastAction = actions.get(size - 1);

        return lastAction;
    }

    private boolean isNeedRepeat(Action.ActionType type) {
        return type == BRIGHTNESS
                || type == CONTRAST
                || type == DRAW_LINE;
    }

    private void drawLineByScale(Bitmap bitmap, DrawLineAction action, float scale) {

        mLineScalePaint.setStrokeWidth(LINE_STROKE * scale);
        List<DrawLineAction.Line> lines = action.getLines();
        Canvas canvas = new Canvas(bitmap);

        for (DrawLineAction.Line line : lines) {
            float startX = line.getStartX() * scale;
            float startY = line.getStartY() * scale;
            float endY = line.getEndY() * scale;
            float endX = line.getEndX() * scale;

            canvas.drawLine(startX, startY, endX, endY, mLineScalePaint);
        }

    }

    private Bitmap drawActions(Bitmap bitmap) {
        Bitmap resultBitmap = bitmap;
        List<Action> actions = simplyActions(mOperateAction);

        for (Action action : actions) {
            Action.ActionType currentActionType = action.getActionType();

            switch (currentActionType) {
                case ROTATE:
                    RotateAction rotateAction = (RotateAction) action;
                    int rotateDegree = getRotateDegree(rotateAction);

                    if (rotateDegree == 0) {
                        break;
                    }

                    resultBitmap = rotate(resultBitmap, rotateDegree);
                    break;
                case DRAW_LINE:
                    drawLine(resultBitmap, ((DrawLineAction) action), mLinePaint);
                    break;
                case REVERSE_X:
                    ReverseX reverseX = (ReverseX) action;
                    int reverseXTimes = reverseX.getReverseXTimes();

                    if (reverseXTimes % 2 == 0) {
                        break;
                    }

                    resultBitmap = reverseX(resultBitmap);
                    break;
                case REVERSE_Y:
                    ReverseY reverseY = (ReverseY) action;
                    int reverseYTimes = reverseY.getReverseYTimes();

                    if (reverseYTimes % 2 == 0) {
                        break;
                    }

                    resultBitmap = reverseY(resultBitmap);
                    break;
                case CONTRAST:
//                    contrastInner(mTempBitmap, ((ContrastAction) action));
                    break;
            }
        }

        return resultBitmap;
    }

    private Uri saveToSDCard(Bitmap bitmap, String fileName) {
        File file = new File(fileName);
        FileOutputStream fos;

        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "FileNotFoundException");
        } catch (IOException e) {
            Log.w(TAG, "IOExeption");
        }

        return Uri.fromFile(file);
    }

    private void notifyScanImage(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(uri);
        mContext.sendBroadcast(intent);
    }

    private boolean ensurePathAccess(String screenShotPath) {
        File file = new File(screenShotPath);

        if (file.exists()) {
            return true;
        } else if (file.mkdirs()) {
            return true;
        }

        return false;
    }

    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private ContrastAction getLastContrastAction() {
        ContrastAction contrastAction = null;

        for (Action action : mOperateAction) {
            if (action.getActionType() == CONTRAST) {
                contrastAction = ((ContrastAction) action);
            }
        }

        return contrastAction;
    }

    private BrightnessAction getLastBrightnessAction() {
        BrightnessAction brightnessAction = null;

        for (Action action : mOperateAction) {
            if (action.getActionType() == BRIGHTNESS) {
                brightnessAction = ((BrightnessAction) action);
            }
        }

        return brightnessAction;
    }

    private Bitmap drawLine(Bitmap bitmap, DrawLineAction action, Paint paint) {
        List<DrawLineAction.Line> lines = action.getLines();
        Canvas canvas = new Canvas(bitmap);

        for (DrawLineAction.Line line : lines) {
            canvas.drawLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY(), paint);
        }

        return bitmap;
    }

    private Bitmap reverseY(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setScale(1, -1);
        matrix.postTranslate(0, bitmap.getHeight());

        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap reverseX(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        matrix.postTranslate(bitmap.getWidth(), 0);

        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap rotate(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, Rect desRect) {

        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        int srcWidth = srcRect.width();
        int srcHeight = srcRect.height();

        int desHeight = desRect.height();
        int desWidth = desRect.width();

        float heightScale = (float) desHeight / srcHeight;
        float widthScale = (float) desWidth / srcWidth;
        float minScale = Math.min(heightScale, widthScale);

        int halfWidth = srcWidth / 2;
        int halfHeight = srcHeight / 2;
        int centerX = desRect.centerX();
        int centerY = desRect.centerY();

        RectF rectF = new RectF(srcRect);

        Matrix matrix = new Matrix();
        matrix.postScale(minScale, minScale);
        matrix.mapRect(rectF);

        int desLeft = ((int) (centerX - halfWidth * minScale));
        int desRight = (int) (centerX + halfWidth * minScale);
        int desTop = (int) (centerY - halfHeight * minScale);
        int desBottom = (int) (centerY + halfHeight * minScale);
        desRect.set(desLeft, desTop, desRight, desBottom);

        canvas.drawBitmap(bitmap, srcRect, desRect, null);
    }

    private Setting getDrawSetting(Bitmap bitmap) {
        Rect desRect = new Rect();
        getDrawingRect(desRect);
        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        int srcWidth = srcRect.width();
        int srcHeight = srcRect.height();

        int desHeight = desRect.height();
        int desWidth = desRect.width();

        float heightScale = (float) desHeight / srcHeight;
        float widthScale = (float) desWidth / srcWidth;
        float minScale = Math.min(heightScale, widthScale);

        int halfWidth = srcWidth / 2;
        int halfHeight = srcHeight / 2;
        int centerX = desRect.centerX();
        int centerY = desRect.centerY();

        RectF rectF = new RectF(srcRect);

        Matrix matrix = new Matrix();
        matrix.postScale(minScale, minScale);
        matrix.mapRect(rectF);

        float desLeft = (centerX - halfWidth * minScale);
        float desTop = (centerY - halfHeight * minScale);

        return new Setting(minScale, desLeft, desTop);
    }

    private Bitmap getBitmap(String filePath, int destWidth, int destHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        int outWidth = options.outWidth;
        int outHeight = options.outHeight;

        int sampleSize = 1;
        while (outHeight / sampleSize > destHeight || outWidth / sampleSize > destWidth) {
            sampleSize *= 2;
        }

        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeFile(filePath, options);
    }

    class Setting {
        float _scale;
        float _offX;
        float _offY;

        public Setting(float scale, float offX, float offY) {
            _scale = scale;
            _offX = offX;
            _offY = offY;
        }

        public float getScale() {
            return _scale;
        }

        public void setScale(float scale) {
            this._scale = scale;
        }

        public float getOffX() {
            return _offX;
        }

        public void setOffX(float offX) {
            this._offX = offX;
        }

        public float getOffY() {
            return _offY;
        }

        public void setOffY(float offY) {
            this._offY = offY;
        }
    }

}
