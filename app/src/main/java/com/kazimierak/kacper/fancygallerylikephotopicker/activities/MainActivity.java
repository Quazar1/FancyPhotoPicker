package com.kazimierak.kacper.fancygallerylikephotopicker.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import com.kazimierak.kacper.fancygallerylikephotopicker.R;
import com.kazimierak.kacper.fancygallerylikephotopicker.fragments.LoadPhotoFragment;

public class MainActivity extends AppCompatActivity implements LoadPhotoFragment.OnPhotoLoaded{
    
    private static final String TOOLBAR_TITLE = "Pick photo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoadPhotoFragment loadPhotoFragment = new LoadPhotoFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.mainContentContainer, loadPhotoFragment).commit();
        
        createToolbar(TOOLBAR_TITLE);
    }

    /**
     * Method called from fragment handling photo loading,
     * when image was successfully loaded and put into cache
     */
    @Override
    public void imageLoadSuccess(String path) {
        Intent mainIntent = new Intent(MainActivity.this, PresenterActivity.class);
        mainIntent.putExtra("path", path);
        startActivity(mainIntent);
    }
    
    private void createToolbar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) toolbar.setTitle(title);
    }
}
