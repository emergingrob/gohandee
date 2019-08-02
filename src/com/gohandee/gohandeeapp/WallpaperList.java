package com.gohandee.gohandeeapp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;

import com.gohandee.services.ServiceFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import android.app.WallpaperManager;

import com.google.ads.*;
import com.github.droidfu.widgets.WebImageView;

import com.gohandee.services.DrupalNode;
import com.gohandee.services.ServiceFactory;
import com.gohandee.services.DrupalService;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class WallpaperList extends Activity
{
   private GridView MyGrid;
   private DrupalService client;     
   private List<DrupalNode> wallpapers;
   private WallpaperAdapter aa;
   private AdView adView;
   private Bundle extras;
   
   int lastInScreen;
   int itemsPerPage = 20;
   boolean loadingMore = false;        
   boolean customTitleSupported;
   String taxonomy = "";
   String query = "";
   
   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
	  //Remove title bar
	  this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	
      setContentView(R.layout.wallpapers_view);
       
	  final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);             

      actionBar.addAction(new SearchAction());

		AdView ad = (AdView) findViewById(R.id.adView);
		AdRequest request = new AdRequest();
		ad.loadAd(request);

      Intent myIntent = getIntent();
      
      extras = getIntent().getExtras();
      
     if (myIntent.hasExtra("category")) {
       taxonomy = extras.getString("category");
       actionBar.setTitle(taxonomy + " Wallpapers");
     } else if (myIntent.hasExtra("query")) { 
         query = extras.getString("query");
         actionBar.setTitle("Wallpapers - " + query);
     } else {
    	 actionBar.setTitle("Wallpapers");    	 
     }
    	 
     
		client = ServiceFactory.getService(this, getString(R.string.SERVER_TYPE));
		String[] sections = getResources().getStringArray(R.array.SECTIONS);
		
		wallpapers = new ArrayList<DrupalNode>();
		int resID = R.layout.wallpaper_item;		
		MyGrid = (GridView)findViewById(R.id.MyGrid);
		
     
		/*Here we setContentView() to main.xml, get the GridView and then fill it with the
                   ImageAdapter class that extend from BaseAdapter */		
		
		try {
			if (taxonomy != "") {
				wallpapers = client.viewsGet(sections[1], null, taxonomy, 0, 26);
			} else if (query != ""){
				wallpapers = client.viewsGet(sections[1], null, "\"all\",\"" + query + "\"", 0, 26);
			} else {
				wallpapers = client.viewsGet(sections[1], null, null, 0, 26);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		aa = new WallpaperAdapter(this, resID, wallpapers);		

        MyGrid.setAdapter(aa);
        
        MyGrid.setOnItemClickListener(wallPaperClicked);
        
        MyGrid.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
			   lastInScreen = (firstVisibleItem + visibleItemCount);				

				if((lastInScreen == totalItemCount) && !(loadingMore)){					
					Thread thread =  new Thread(null, loadMoreListItems);
			        thread.start();
				}				
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}
        	
        });
   }
   
	private OnItemClickListener wallPaperClicked = new OnItemClickListener() {				
	    public void onItemClick(AdapterView parent, View v, int position, long id)
	    {
			TextView nid = (TextView) v.findViewById(R.id.nodeNid);
	    	
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
   
    public void customTitleBar(String right) {
        if (right.length() > 20) right = right.substring(0, 20);
        // set up custom title
        if (customTitleSupported) {
                getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                                R.layout.custom_titlebar);
                ImageView logoLeft = (ImageView) findViewById (R.id.logoLeft);
                TextView titleTvRight = (TextView) findViewById(R.id.titleTvRight);
 
                titleTvRight.setText(right);
                
                //titleTvRight.setText("jo");
  
        }
    }    	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wallpaper_menu, menu);
        return true;
    }    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.icon:     Toast.makeText(this, "You pressed the icon!", Toast.LENGTH_LONG).show();
                                break;
            case R.id.text:     Toast.makeText(this, "You pressed the text!", Toast.LENGTH_LONG).show();
                                break;
            case R.id.icontext: 
        		Intent myIntent = new Intent(this, com.gohandee.gohandeeapp.WallpaperCategories.class);
        		startActivityForResult(myIntent, 0);
                break;
        }
        return true;
    }    
    
   //Runnable to load the items 
   private Runnable loadMoreListItems = new Runnable() {			
		@Override
		public void run() {
			//Set flag so we cant load new items 2 at the same time
			loadingMore = true;
			
			//Reset the array that holds the new items
	    	//myListItems = new ArrayList<String>();
			wallpapers = new ArrayList<DrupalNode>();
			String[] sections = getResources().getStringArray(R.array.SECTIONS);	

			try {			
				wallpapers = client.viewsGet(sections[1], null, null, lastInScreen, itemsPerPage);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}      			
								
			
			//Done! now continue on the UI thread
	        runOnUiThread(returnRes);
	        
		}
	};	
   
	//Since we cant update our UI from a thread this Runnable takes care of that! 
    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
        	
			//Loop thru the new items and add them to the adapter
			if(MyGrid != null){
                for(int i=0;i < 20 ;i++)
                	aa.add(wallpapers.get(i));
            }
        	
			//Update the Application title
        	setTitle("Neverending List with " + String.valueOf(aa.getCount()) + " items");
			
			//Tell to the adapter that changes have been made, this will cause the list to refresh
            aa.notifyDataSetChanged();
			
			//Done loading more.
            loadingMore = false;
        }
    };   
   public class WallpaperAdapter extends ArrayAdapter<DrupalNode>
   {
		
      
		int resource;
	    private ArrayList<DrupalNode> items;
	    
		public WallpaperAdapter(Context _context, int _resource, List<DrupalNode> _items) {
			super(_context, _resource, _items);
			resource = _resource;
		} 

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
 // 		LinearLayout wallpapersView;

         View MyView = convertView;
         
         DrupalNode item = getItem(position);		

         String nodeNid = String.valueOf(item.getNid());
         String wallpaperTitle = item.getTitle();
         String wallPath = item.getWallPath();

         if ( convertView == null )
         {
// 			wallpapersView = new LinearLayout(getContext());
//			String inflater = Context.LAYOUT_INFLATER_SERVICE;
//			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
//			vi.inflate(resource, wallpapersView, true);		
			
        	LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);        	 
            MyView = li.inflate(R.layout.wallpaper_item, null);           

         } else {
			MyView = (LinearLayout) convertView;
		 }         

         // Add The Text!!!
         TextView wallNid = (TextView)MyView.findViewById(R.id.nodeNid);            
         TextView tv = (TextView)MyView.findViewById(R.id.grid_item_text);
         tv.setText(wallpaperTitle);
         wallNid.setText(nodeNid);
         
         // Add The Image!!!
         WebImageView iv = (WebImageView)MyView.findViewById(R.id.grid_item_image);
       
 	    // Add the adView to it
// 	    MyGrid.(adView);

 	    // Initiate a generic request to load it with an ad
//   	    adView.loadAd(new AdRequest());            
         
         
         StringBuffer s1 = new StringBuffer(wallPath);         
         int slash = wallPath.lastIndexOf("/");
         s1.insert(slash, "/imagecache/150x145/wallpapers");         
         String s2 = s1.toString();
         s2 = "http://www.gohandee.com/" + s2;
         s2 = s2.replaceFirst("/wallpapers", "");
         //Log.v("Wallpaper", s2);

         double width = getWindowManager().getDefaultDisplay().getWidth() / 2;         
         
         iv.reset();                                
         iv.setImageUrl(s2);
         //iv.scaleType(ScaleType.FIT_XY);        
         iv.loadImage(); 
         
         
         iv.getLayoutParams().width = (int)width - 15;
         iv.getLayoutParams().height = (int)width - 15; 
         MyView.setLayoutParams(new GridView.LayoutParams((int)width - 10, (int)width + 15));
       //  iv.setImageResource(R.drawable.icon);         
         
         return MyView;
      }

      public long getItemId(int position) { return position; }
      
 
   }
   
   public static Intent createIntent(Context context) {
       Intent i = new Intent(context, Ringtone.class);
       i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       return i;
   }

   private Intent createShareIntent() {
       final Intent intent = new Intent(Intent.ACTION_SEND);
       intent.setType("text/plain");
       intent.putExtra(Intent.EXTRA_TEXT, "Shared from the ActionBar widget.");
       return Intent.createChooser(intent, "Share");
   }       
   
   private class SearchAction implements Action {

	    @Override
	    public int getDrawable() {
	        return R.drawable.find;
	    }

	    @Override
	    public void performAction(View view) {
	    	onSearchRequested();
	    }

	}
   
}