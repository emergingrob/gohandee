package com.gohandee.gohandeeapp;

import java.util.ArrayList;
import java.util.List;

import com.gohandee.services.DrupalNode;
import com.gohandee.services.DrupalService;
import com.gohandee.services.ServiceFactory;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyAccount extends ListActivity {
	private DrupalService client;
	DrupalNode user = new DrupalNode();
	private ProgressDialog dialog;	
	String list_array[]={"Documents & Files", "Audio", "Videos", "Images & Wallpapers", "Friends", "Profile" };
	String countType[] = new String[5];
	ActionBar actionBar;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.myaccount);
        actionBar = (ActionBar) findViewById(R.id.actionbar);
        
		boolean check = isOnline();
		if (check) {  
			dialog = ProgressDialog.show(this, "Loading List", "Searching...", true);			
			new init().execute();
			
		} else {
			AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
			alt_bld.setMessage("No Connection found. Please make sure you are connected.");
			alt_bld.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
			    } }); 
		}	        
    }
    
    private class init extends AsyncTask<Void, Void, DrupalNode>  {
		 @Override		 
		 protected DrupalNode doInBackground(Void... params) {			    		        
		        client = ServiceFactory.getService(getApplicationContext(), getString(R.string.SERVER_TYPE));
				SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
				String uid = auth.getString("uid", null);
								
				AdView ad = (AdView) findViewById(R.id.adView);
				AdRequest request = new AdRequest();
				ad.loadAd(request);
				
		        try {
		        	countType[0] = client.countType("document", uid);
		        	countType[1] = client.countType("audio", uid);
		        	countType[2] = client.countType("videofile", uid);
		        	countType[3] = client.countType("wallpaper", uid);
		        	user = client.userGet(Integer.parseInt(uid));
				} catch (Exception e) {				
					e.printStackTrace();
				}
		        
		        return user;
		 }
		 
		 protected void onPostExecute(DrupalNode result) {
			    actionBar.setTitle("My Account - " + user.getName());
			    actionBar.addAction(new SearchAction());
			    setListAdapter(new MyAccountAdapter(getApplicationContext(), list_array));
			    dialog.dismiss();
		 }

    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
 
		//get selected items
		String selectedValue = (String) getListAdapter().getItem(position);
		if (selectedValue == "Documents & Files") {
	        Intent myIntent = new Intent(MyAccount.this, MyDocuments.class);
	        MyAccount.this.startActivity(myIntent); 				
		}
		if (selectedValue == "Audio") {
	        Intent myIntent = new Intent(MyAccount.this, MyAudio.class);
	        MyAccount.this.startActivity(myIntent); 				
		}
		if (selectedValue == "Videos") {
	        Intent myIntent = new Intent(MyAccount.this, MyVideo.class);
	        MyAccount.this.startActivity(myIntent); 				
		}
		if (selectedValue == "Images & Wallpapers") {
	        Intent myIntent = new Intent(MyAccount.this, MyDocuments.class);
	        MyAccount.this.startActivity(myIntent); 				
		}
		if (selectedValue == "Friends") {
	        Intent myIntent = new Intent(MyAccount.this, MyDocuments.class);
	        MyAccount.this.startActivity(myIntent); 				
		}
		if (selectedValue == "Profile") {
	        Intent myIntent = new Intent(MyAccount.this, MyDocuments.class);
	        MyAccount.this.startActivity(myIntent); 				
		}		
 
	}    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.myaccount_menu, menu);
        return true;
    }	

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
		String uid = auth.getString("uid", null);
		if(uid != null){
			//MenuItem login = menu.findItem(R.id.login);
			//login.setVisible(false);
			//MenuItem content = menu.findItem(R.id.create);
			//content.setVisible(true);
		}
		else{
			//MenuItem login = menu.findItem(R.id.login);
			//login.setVisible(true);
			//MenuItem content = menu.findItem(R.id.create);
			//content.setVisible(false);
		}
		return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	if(item.getItemId() == R.id.logout){
    		try {
    			SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
    			String sessionID = auth.getString("sessionid", null);
				client.logout(sessionID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		MyAccount.this.finish();
    	}
    	    	

    	return true;
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

		public boolean isOnline() {
		    ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		        return true;
		    }
		    return false;
		}   

		public class MyAccountAdapter extends ArrayAdapter<String> {
			private final Context context;
			private final String[] values;	
			private DrupalService client;
			private List<DrupalNode> myFriends;
			
			public MyAccountAdapter(Context context, String[] values) {
				super(context, R.layout.myaccount_items, values);
				this.context = context;
				this.values = values;				
			}
		 
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		 
				View rowView = inflater.inflate(R.layout.myaccount_items, parent, false);
				TextView textView = (TextView) rowView.findViewById(R.id.label);
				TextView count = (TextView) rowView.findViewById(R.id.count);
				ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
				textView.setText(values[position]);
		 
				// Change icon based on name
				String s = values[position];
		 
				System.out.println(s);
		 
				client = ServiceFactory.getService(this.context, this.context.getString(R.string.SERVER_TYPE));
				SharedPreferences auth = this.context.getSharedPreferences(this.context.getString(R.string.sharedpreferences_name), 0);
				String uid = auth.getString("uid", null);
				//String countType = "";

				try {			
					if (s.equals("Documents & Files")) {
						imageView.setImageResource(R.drawable.document);
					} else if (s.equals("Audio")) {
						imageView.setImageResource(R.drawable.audio);
					} else if (s.equals("Videos")) {
						imageView.setImageResource(R.drawable.video);
					} else if (s.equals("Images & Wallpapers")) {
						imageView.setImageResource(R.drawable.images);
					} else if (s.equals("Friends")) {
						//myFriends = new ArrayList<DrupalNode>();					
						//myFriends = client.myUserRelationships();
						//countType[5] = String.valueOf(myFriends.size());								
						imageView.setImageResource(R.drawable.friends);				
					} else if (s.equals("Profile")) {
						imageView.setImageResource(R.drawable.profile);						
					} else {
						imageView.setImageResource(R.drawable.button_play);
					}		     						
									
					count.setText(countType[position]);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				
				
				return rowView;
			}
		}		
		
}
