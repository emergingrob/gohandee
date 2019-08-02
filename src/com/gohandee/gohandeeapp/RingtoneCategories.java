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
             
public class RingtoneCategories extends ListActivity {
	
	//final String[] taxonomy = getResources().getStringArray(R.array.WP_TAXONOMY);
	final String[] taxonomy = {"34","33","876","913","914","948","934","936","875","965","932","32","964","109","919","920","921","922","941","874","923","924","31","962","940","858","877","1106"}; 	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setListAdapter(ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.RT_CATEGORIES, R.layout.wallpaper_categories));
        
        final ListView lv = getListView();
        lv.setTextFilterEnabled(false);
        lv.setItemsCanFocus(true);
        lv.setOnItemClickListener(rtCategoryClicked);
        
    }	
	
	private OnItemClickListener rtCategoryClicked = new OnItemClickListener() {				
	    public void onItemClick(AdapterView parent, View v, int position, long id)
	    {
			String content = taxonomy[position];

			Intent intent = new Intent(getBaseContext(), com.gohandee.gohandeeapp.EndlessRingtones.class);
			intent.putExtra("category", content);
			startActivity(intent);	    													
	    }
	    
	    
	}; 
	
}