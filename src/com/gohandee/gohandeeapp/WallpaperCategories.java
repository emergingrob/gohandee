package com.gohandee.gohandeeapp;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
             
public class WallpaperCategories extends ListActivity {
	
	//final String[] taxonomy = getResources().getStringArray(R.array.WP_TAXONOMY);
	final String[] taxonomy = {"100","101","102","103","104","105","106","1107","107","108","110","111","1022","109","121","112","113","114","115","116","117","118","119","120"}; 	
	
	@Override
    	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setListAdapter(ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.WP_CATEGORIES, R.layout.wallpaper_categories));
        
        final ListView lv = getListView();
        lv.setTextFilterEnabled(false);
        lv.setItemsCanFocus(true);
        lv.setOnItemClickListener(wallCategoryClicked);
        
    }	
	
	private OnItemClickListener wallCategoryClicked = new OnItemClickListener() {				
	    public void onItemClick(AdapterView parent, View v, int position, long id)
	    {
			String content = taxonomy[position];

			Intent intent = new Intent(getBaseContext(), com.gohandee.gohandeeapp.WallpaperList.class);
			intent.putExtra("category", content);
			startActivity(intent);	    													
	    }
	    
	    
	}; 
	
}
