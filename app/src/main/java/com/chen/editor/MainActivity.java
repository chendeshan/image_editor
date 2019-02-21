package com.chen.editor;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.chen.editor.widget.EditImageView;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends Activity {

    private EditImageView mEditImageView;
    private SeekBar mBrightnessBar;
    private SeekBar mContrastBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        initEditView();
        initSeekBar();
    }

    private void initSeekBar() {
        mBrightnessBar = (SeekBar) findViewById(R.id.activity_main_brightness_seek_bar);
        mBrightnessBar.setOnSeekBarChangeListener(mOnBrightnessSeekBarChangeListener);

        mContrastBar = (SeekBar) findViewById(R.id.activity_main_contrast_seek_bar);
        mContrastBar.setOnSeekBarChangeListener(mOnContrastSeekBarChangeListener);
    }

    private Bitmap getBitmap() {
        try {
            InputStream flowerStream = getResources().getAssets().open("flower.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(flowerStream);


            return bitmap;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void initEditView() {
        mEditImageView = (EditImageView) findViewById(R.id.activity_main_edit_image);
        Bitmap bitmap = getBitmap();
        mEditImageView.setImage(bitmap);
    }

    public void withdrawClick(View view) {
        mEditImageView.withDraw();
        dismissSeekBar();
    }

    public void penClick(View view) {
        mEditImageView.drawLine();
        dismissSeekBar();
    }

    public void rotateClick(View view) {
        mEditImageView.rotate();
        dismissSeekBar();
    }

    public void reverseXClick(View view) {
        mEditImageView.reverseX();
        dismissSeekBar();
    }

    public void reverseYClick(View view) {
        mEditImageView.reverseY();
        dismissSeekBar();
    }

    public void brightnessClick(View view) {
        showBrightnessBar();
        dismissContrastBar();
    }

    public void contrastClick(View view) {
        showContrastBar();
        dismissBrightnessBar();
    }

    private void dismissSeekBar() {
        dismissContrastBar();
        dismissBrightnessBar();
    }

    private void showBrightnessBar() {
        mBrightnessBar.setVisibility(View.VISIBLE);
    }

    private void dismissBrightnessBar() {
        mBrightnessBar.setVisibility(View.GONE);
    }

    private void showContrastBar() {
        mContrastBar.setVisibility(View.VISIBLE);
    }

    private void dismissContrastBar() {
        mContrastBar.setVisibility(View.GONE);
    }

    private SeekBar.OnSeekBarChangeListener mOnBrightnessSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mEditImageView.brightness(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mEditImageView.brightnessDone(seekBar.getProgress());
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnContrastSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mEditImageView.contrast(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mEditImageView.contrastDone(seekBar.getProgress());
        }
    };

}
