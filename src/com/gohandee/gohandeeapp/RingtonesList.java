package com.gohandee.gohandeeapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.gohandee.gohandeeapp.EndlessAdapter;
import com.gohandee.services.DrupalNode;
import com.gohandee.services.ServiceFactory;
import com.gohandee.services.DrupalService;

public class RingtonesList extends Activity {

	private List<DrupalNode> ringtones;
	private ListView ringtonesView;
	private RingtonesAdapter aa;
	private DrupalService client;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.ringtonesview);
		
		client = ServiceFactory.getService(this, getString(R.string.SERVER_TYPE));
		String[] sections = getResources().getStringArray(R.array.SECTIONS);

		// Get references to UI widget
		ringtonesView = (ListView) findViewById(R.id.ringtonesView);
		
		// Create the array List of documents
		ringtones = new ArrayList<DrupalNode>();
		int resID = R.layout.ringtones_item;
		
		try {			
			ringtones = client.viewsGet("solr_ringtones", null, "ring", 0, 100);
			          //client.viewsGet(view_name, display_id, args, offset, limit)
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
			
		
		// Create the array adapter to bind the array to the listview
		aa = new RingtonesAdapter(this, resID, ringtones);
		
		// bind the array to the list view
		ringtonesView.setAdapter(aa);
		
		ringtonesView.setOnItemClickListener(ringtoneClicked);
		

	}
	
	private OnItemClickListener ringtoneClicked = new OnItemClickListener() {				
	    public void onItemClick(AdapterView parent, View v, int position, long id)
	    {
	    	//Toast.makeText(getApplicationContext(), "HEYHEY", Toast.LENGTH_SHORT).show();
			try {
				TextView ringCat = (TextView)ringtonesView.findViewById(R.id.ringtoneCategory);				
				Intent intent = new Intent(getBaseContext(), com.gohandee.gohandeeapp.Ringtone.class);
				intent.putExtra("nid", ringCat.getText().toString());
				//TODO: pass off to content view activity
				startActivity(intent);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	    	
	    	
	    }
	    
	    
	};
	
}