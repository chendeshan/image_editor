package com.chen.editor;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.chen.editor.widget.EditImageView;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends Activity {

    private EditImageView mEditImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initEditView();


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
    }

    public void penClick(View view) {
        mEditImageView.drawLine();
    }

    public void rotateClick(View view) {
        mEditImageView.rotate();
    }

    public void reverseXClick(View view) {
        mEditImageView.reverseX();
    }

    public void reverseYClick(View view) {
        mEditImageView.reverseY();
    }

    public void brightnessClick(View view) {

    }

    public void contrastClick(View view) {

    }
}
