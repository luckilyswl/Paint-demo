package com.qingshangzuo.paint;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;

public class PhotoAty extends AppCompatActivity {

    private ImageView photo_img;
    private Uri uri;
    private Button photo_output;
    private Bitmap bitmap;
    private String base64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_aty);

        initView();
        initListener();
    }

    private void initView() {
        bitmap = null;
        base64 = "";
        photo_img = (ImageView) findViewById(R.id.photo_img);
        Intent intent = getIntent();
        uri = intent.getData();
        bitmap = decodeUriAsBitmap(uri);
        // 把解析到的位图显示出来
        photo_img.setImageBitmap(bitmap);
        photo_output = (Button) findViewById(R.id.photo_output);
    }

    private void initListener() {
        photo_output.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap != null) {
                    base64 = Base64BitmapUtil.bitmapToBase64(bitmap);
                    Log.i("base4------", base64);
                }

            }
        });
    }

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            // 先通过getContentResolver方法获得一个ContentResolver实例，
            // 调用openInputStream(Uri)方法获得uri关联的数据流stream
            // 把上一步获得的数据流解析成为bitmap
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

}
