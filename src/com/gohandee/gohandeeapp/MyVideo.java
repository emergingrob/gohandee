package com.gohandee.gohandeeapp;

import java.util.ArrayList;
import java.util.List;

import com.gohandee.quickaction.*;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.insready.drupalcloud.*;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.commonsware.cwac.endless.EndlessAdapter;
import com.gohandee.services.DrupalNode;
import com.gohandee.services.ServiceFactory;
import com.gohandee.services.DrupalService;
import com.google.ads.*;


public class MyVideo extends ListActivity {

	static int LIST_SIZE;
	private int mLastOffset = 0;
 
	static int BATCH_SIZE = 25;
 
	private List<DrupalNode> ringtones;
	private DrupalService client;
	private ListView lv;
	private Bundle extras;
	RatingBar rating;
	Float ringtoneRating;
	String taxonomy = "";
	String query = "";
	String countType = "";
	String nid = "";
	
	private static final int ID_DELETE = 1;
	private static final int ID_ACCEPT = 2;
	private static final int ID_UPLOAD = 3;
	QuickAction mQuickAction;
	
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			//	Remove title bar
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
  
			setContentView(R.layout.myaudio);
			
			client = ServiceFactory.getService(this, getString(R.string.SERVER_TYPE));
			final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);  
			actionBar.addAction(new SearchAction());
  
	        ActionItem deleteItem 	= new ActionItem(ID_DELETE, "Delete", getResources().getDrawable(R.drawable.delete));
			ActionItem acceptItem 	= new ActionItem(ID_ACCEPT, "Accept", getResources().getDrawable(R.drawable.ic_accept));
	        ActionItem uploadItem 	= new ActionItem(ID_UPLOAD, "Upload", getResources().getDrawable(R.drawable.ic_up));
			
			mQuickAction 	= new QuickAction(this);				
			
			mQuickAction.addActionItem(deleteItem);
			mQuickAction.addActionItem(acceptItem);
			mQuickAction.addActionItem(uploadItem);
			
			//setup the action item click listener
			mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
				@Override
				public void onItemClick(QuickAction quickAction, int pos, int actionId) {
					ActionItem actionItem = quickAction.getActionItem(pos);
					
					if (actionId == ID_DELETE) { //Add item selected						
						try {
							Log.v("NIDDEL",nid);
							client.nodeDelete(nid);							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Toast.makeText(getApplicationContext(), "Deleted" , Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), actionItem.getTitle() + " item selected on row " 
								, Toast.LENGTH_SHORT).show();
					}	
				}
			});			
			
			Intent myIntent = getIntent();
  
			extras = getIntent().getExtras();  
  
			actionBar.setTitle("My Videos");    	 
  
			init();

			AdView ad = (AdView) findViewById(R.id.adView);
			AdRequest request = new AdRequest();
			ad.loadAd(request);
  
			final ListView lv = getListView();
			lv.setTextFilterEnabled(false);
			lv.setItemsCanFocus(true);
			lv.setOnItemClickListener(ringtoneClicked);

		}

		private OnItemClickListener ringtoneClicked = new OnItemClickListener() {				
			public void onItemClick(AdapterView parent, View v, int position, long id)
	    {
	    	
			TextView nid = (TextView) v.findViewById(R.id.ringtoneNid);
	    	
			try {				
				Intent intent = new Intent(getBaseContext(), com.gohandee.gohandeeapp.Video.class);
				intent.putExtra("nid", nid.getText().toString());
				//intent.putExtra("rating", String.valueOf(ringtoneRating));
				intent.putExtra("rating", ringtoneRating);
				//TODO: pass off to content view activity
				startActivity(intent);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	    	
	    	
	    }
	    
	    
	};
	
	public void quickAudio(View v) {		
		RelativeLayout vwParentRow = (RelativeLayout)v.getParent(); 		
    	TextView audioNid = (TextView)vwParentRow.getChildAt(4);
		nid = audioNid.getText().toString();		
		mQuickAction.show(v);
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
        		Intent myIntent = new Intent(this, com.gohandee.gohandeeapp.RingtoneCategories.class);
        		startActivityForResult(myIntent, 0);
                break;
        }
        return true;
    } 	
 
 private void init() {
	 client = ServiceFactory.getService(this, getString(R.string.SERVER_TYPE));
	 SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
	 String uid = auth.getString("uid", null);		 
	 
	 ringtones = new ArrayList<DrupalNode>();
	 LIST_SIZE = 0; 
 
	 try {	
		LIST_SIZE = Integer.parseInt((client.countType("videofile", uid))) - 1;
		Log.v("LISTSIZE", String.valueOf(LIST_SIZE));
		if (BATCH_SIZE > LIST_SIZE) BATCH_SIZE = LIST_SIZE;
		ringtones = client.viewsGet("my_videos", null, "73", 0, 2);				
	 } catch (Exception e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
	 }  
	 Log.v("BATCHSIZE", String.valueOf(BATCH_SIZE));
	 setLastOffset(BATCH_SIZE);
	 displayList(ringtones);
 }

 private void setLastOffset(int i) {
	 mLastOffset = i;  
 }
 
 private int getLastOffset(){
	 return mLastOffset;
 }

 private void displayList(List<DrupalNode> ringtones) {  
	 setListAdapter(new DemoAdapter());
 }

 private class CustomArrayAdapter extends ArrayAdapter<DrupalNode>{
  int resource;

  public CustomArrayAdapter(Context context, int _resource, List<DrupalNode> _items) {
   super(context, _resource, _items);
	resource = _resource;
  }  

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
   LinearLayout ringtonesView;

   DrupalNode item = getItem(position);
   
   // Load data from Views list for Ringtone Items
   String ringtoneName = item.getTitle();	
   String ringtoneCat = item.getRingCategory();
   String ringtoneNid = String.valueOf(item.getNid());

   ringtoneRating = new Float(item.getRating()); 
   
   if(convertView==null){
	ringtonesView = new LinearLayout(getContext());
	String inflater = Context.LAYOUT_INFLATER_SERVICE;
	LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
	vi.inflate(resource, ringtonesView, true);
	ringtonesView.setTag(ringtoneNid);
   } else {
		ringtonesView = (LinearLayout) convertView;
   }
   
   // Load Ringtone Fields from XML Layout   
   TextView ringView = (TextView)ringtonesView.findViewById(R.id.ringtoneName);
   TextView ringCat = (TextView)ringtonesView.findViewById(R.id.ringtoneCategory);
   TextView ringNid = (TextView)ringtonesView.findViewById(R.id.ringtoneNid);
   rating = (RatingBar)ringtonesView.findViewById(R.id.ratingBar);   

   rating.setRating(ringtoneRating / 20);
   
   // Clear html from String
   ringtoneName = ringtoneName.replaceAll("x26#039;", "'");
   ringtoneName = ringtoneName.replaceAll("&#039;", "'");
   
   ringView.setText(ringtoneName);      
   ringCat.setText(" | " + ringtoneCat);
   ringNid.setText(ringtoneNid);

   return ringtonesView;

  }


 }

 class DemoAdapter extends EndlessAdapter {
  private RotateAnimation rotate=null;
  ArrayList<DrupalNode> tempList = new ArrayList<DrupalNode>();
  
  DemoAdapter() {
   super(new CustomArrayAdapter(MyVideo.this, 
     R.layout.audio_item, ringtones));
      
   rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
     0.5f, Animation.RELATIVE_TO_SELF,
     0.5f);
   rotate.setDuration(600);
   rotate.setRepeatMode(Animation.RESTART);
   rotate.setRepeatCount(Animation.INFINITE);
  }

  @Override
  protected View getPendingView(ViewGroup parent) {
   View row=getLayoutInflater().inflate(R.layout.row, null);

   View child=row.findViewById(android.R.id.text1);
   child.setVisibility(View.GONE);
   child=row.findViewById(R.id.throbber);
   child.setVisibility(View.VISIBLE);
   child.startAnimation(rotate);

   return(row);
  }

  @Override
  protected boolean cacheInBackground() {	  
   SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
   String uid = auth.getString("uid", null);
   tempList.clear();
   int lastOffset = getLastOffset();
   if(lastOffset < LIST_SIZE){
    int limit = lastOffset + BATCH_SIZE;
    //for(int i=(lastOffset+1); (i<=limit && i<LIST_SIZE); i++){
     //tempList.add(COUNTRIES[i]);
    //}    
    
	try {			
		ringtones = client.viewsGet("my_vidoes", null, uid, lastOffset, BATCH_SIZE);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}      
    
    setLastOffset(limit);
    
    if(limit<LIST_SIZE){
     return true;
    } else {
     return false;
    }
   } else  {
    return false;
   }
  }


  @Override
  protected void appendCachedData() {
   @SuppressWarnings("unchecked")
   ArrayAdapter<DrupalNode> arrAdapterNew = (ArrayAdapter<DrupalNode>)getWrappedAdapter();

   int listLen = tempList.size();
   for(int i=0; i < BATCH_SIZE; i++){
    arrAdapterNew.add(ringtones.get(i));
   }
  }
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