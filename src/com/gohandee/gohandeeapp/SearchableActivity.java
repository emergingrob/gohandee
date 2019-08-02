package com.gohandee.gohandeeapp;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import com.Leadbolt.AdController;
import com.gohandee.services.DrupalNode;
import com.gohandee.services.DrupalService;
import com.gohandee.services.ServiceFactory;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchableActivity extends Activity {
	private List<DrupalNode> ringtones;
	private List<DrupalNode> wallpapers;	
	private ListView ringtonesList;
	private ListView wallpapersList;
	private RingtonesAdapter rA;
	private WallpapersAdapter wA;
	private DrupalService client;
	String query = "";
	private AdController myController;
	private ListView ringList;
	private ListView wallList;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_results);
		
		//WebView wv = (WebView) findViewById(R.id.webbanner);
		//wv.getSettings().setJavaScriptEnabled(true);
		//wv.setBackgroundColor(Color.TRANSPARENT);
		//String html = "<html><body style='margin:0;padding:0;'><script type='text/javascript' src='http://ad.leadboltads.net/show_app_ad.js?section_id=547860741'></script></body></html>";
		//wv.loadData(html, "text/html", "utf-8");
		
		myController = new AdController(this, "358025604");
		myController.loadAd();
		
		  AdView ad = (AdView) findViewById(R.id.adView);
		  AdRequest request = new AdRequest();
		  ad.loadAd(request);		
		
		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
							
			client = ServiceFactory.getService(this, getString(R.string.SERVER_TYPE));
			String[] sections = getResources().getStringArray(R.array.SECTIONS);		

			ringtonesList = (ListView) findViewById(R.id.ringtonesSearchList);
			wallpapersList = (ListView) findViewById(R.id.wallpapersSearchList);
			ringtones = new ArrayList<DrupalNode>();
			wallpapers = new ArrayList<DrupalNode>();
			
			int resID = R.layout.ringtones_item;	

			try {			
				ringtones = client.viewsGet(sections[0], null, "\"all\",\"" + query + "\"", 0, 3);
				wallpapers = client.viewsGet(sections[1], null, "\"all\",\"" + query + "\"", 0, 3);
			} catch (Exception e) {
				// 	TODO Auto-generated catch block
				e.printStackTrace();
			}		
		
			// 	Create the array adapter to bind the array to the listview
			rA = new RingtonesAdapter(this, resID, ringtones);
			wA = new WallpapersAdapter(this, resID, wallpapers);
			// bind the array to the list view
			ringtonesList.setAdapter(rA);
			wallpapersList.setAdapter(wA);
			
		    ringtonesList.setOnItemClickListener(ringtoneClicked);
		    wallpapersList.setOnItemClickListener(wallpaperClicked);
		
		
		}

	}
	
	private OnItemClickListener wallpaperClicked = new OnItemClickListener() {				
	    public void onItemClick(AdapterView parent, View v, int position, long id)
	    {
			TextView nid = (TextView) v.findViewById(R.id.ringtoneCategory);
	    	
			try {				
				Intent intent = new Intent(getBaseContext(), com.gohandee.gohandeeapp.Wallpaper.class);
				intent.putExtra("nid", nid.getText().toString());
				//TODO: pass off to content view activity
				startActivity(intent);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	    	
	    	
	    }
	    
	    
	};    	
	
	private OnItemClickListener ringtoneClicked = new OnItemClickListener() {				
	    public void onItemClick(AdapterView parent, View v, int position, long id)
	    {
			TextView nid = (TextView) v.findViewById(R.id.ringtoneCategory);
	    	
			try {				
				Intent intent = new Intent(getBaseContext(), com.gohandee.gohandeeapp.Ringtone.class);
				intent.putExtra("nid", nid.getText().toString());
				//TODO: pass off to content view activity
				startActivity(intent);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	    	
	    	
	    }
	    
	    
	};    	
	
	public void ringtoneOnClick(View v) {		
		Intent intent = new Intent(getBaseContext(), com.gohandee.gohandeeapp.EndlessRingtones.class);
		intent.putExtra("query", query);
		startActivity(intent);	   
    }  
	
	public void wallpaperOnClick(View v) {
		Intent intent = new Intent(getBaseContext(), com.gohandee.gohandeeapp.WallpaperList.class);
		intent.putExtra("query", query);
		startActivity(intent);	  
    }  	

}
