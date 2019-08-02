package com.gohandee.gohandeeapp;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.gohandee.gohandeeapp.GoWallPicker.WallpaperLoader;

public class GoRingPicker extends Activity implements AdapterView.OnItemSelectedListener,
        OnClickListener {
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        findViewById(R.id.set).setOnClickListener(this);

        //mImageView = (ImageView) findViewById(R.id.wallpaper);
    }
    
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onItemSelected(AdapterView parent, View v, int position, long id) {

    }    
	
    public void onNothingSelected(AdapterView parent) {
    }    
    
    public void onClick(View v) {
    }    
}