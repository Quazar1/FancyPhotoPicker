package com.kazimierak.kacper.fancygallerylikephotopicker.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.kazimierak.kacper.fancygallerylikephotopicker.R;

/**
 * Created by Kacperon 2016-05-29.
 */
public class PresenterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_present);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String path = extras.getString("path");
            Bitmap photo = BitmapFactory.decodeFile(path);
            ImageView iv = (ImageView) findViewById(R.id.imageView);
            iv.setImageBitmap(photo);
        }

    }
}
