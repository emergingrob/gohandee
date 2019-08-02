package com.gohandee.gohandeeapp;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gohandee.services.DrupalNode;
import com.gohandee.services.DrupalService;
import com.gohandee.services.ServiceFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class GoHandee extends ActionBarActivity {
    private static final int LOGIN_CODE = 0;
	private static final int CREATE_CODE = 1;
	private DrupalService client;
	boolean customTitleSupported;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.frontpage);
				
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
        actionBar.setTitle("Home");
		
		ImageButton loginButton = (ImageButton) findViewById(R.id.LoginButton);

		client = ServiceFactory.getService(this, getString(R.string.SERVER_TYPE));               
		        
        //customTitleBar(getText(R.string.app_name).toString());        

		SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
		String uid = auth.getString("uid", null);        
        

		if(uid != null){						
			loginButton.setVisibility(View.INVISIBLE);
		}
		else{			 
			loginButton.setVisibility(View.VISIBLE);
		}

        //Intent myIntent = new Intent(GoHandee.this, FrontPageActivity.class);
        //GoHandee.this.startActivity(myIntent);             
        
    }
    
	public void startRingtones(View v) {
        Intent myIntent = new Intent(GoHandee.this, EndlessRingtones.class);
        GoHandee.this.startActivity(myIntent); 		
	}
	
	public void startWallpapers(View v) {
        Intent myIntent = new Intent(GoHandee.this, WallpaperList.class);
        GoHandee.this.startActivity(myIntent); 		
	}	
	public void startLogin(View v) {
        Intent myIntent = new Intent(GoHandee.this, LoginActivity.class);
        GoHandee.this.startActivity(myIntent); 		
	}		
	public void startAccount(View v) {
        Intent myIntent = new Intent(GoHandee.this, MyAccount.class);
        GoHandee.this.startActivity(myIntent); 		
	}
	public void createAccount(View v) throws Exception {
		DrupalNode user = new DrupalNode();
		//user = client.userGet(1);
		//Log.v("USER", user.toString());
		
		//user.setUID("3000");
		//user.setType("blog");
		//user.setUID("73");
		//user.setTitle("Test Service Blog");
		//user.setBody("This is a test");
		//user.setName("admin");		        		
		//client.nodeSave(user);
		
		//user.setUID("3");
		//user.setName("itsme");        		
		client.userSave(user);
		
	}		
    
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
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
		String uid = auth.getString("uid", null);
		if(uid != null){
			MenuItem login = menu.findItem(R.id.login);
			login.setVisible(false);
			MenuItem content = menu.findItem(R.id.create);
			content.setVisible(true);
		}
		else{
			MenuItem login = menu.findItem(R.id.login);
			login.setVisible(true);
			MenuItem content = menu.findItem(R.id.create);
			content.setVisible(false);
		}
		return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	if(item.getItemId() == R.id.login){
    		Intent myIntent = new Intent(this, com.gohandee.gohandeeapp.LoginActivity.class);
    		startActivityForResult(myIntent, LOGIN_CODE);
    	}
    	else if(item.getItemId() == R.id.logout){
    		try {
    			SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
    			String sessionID = auth.getString("sessionid", null);
				client.logout(sessionID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		ImageButton loginButton = (ImageButton) findViewById(R.id.LoginButton);
    		loginButton.setVisibility(View.VISIBLE);
    	}
    	int group = item.getGroupId();
    	if(group == R.id.createGroup){
    		int index = Character.getNumericValue(item.getNumericShortcut());
    		Intent myIntent = new Intent(this, com.gohandee.gohandeeapp.CreateContentActivity.class);
    		String[] types = getResources().getStringArray(R.array.CREATE);
    		myIntent.putExtra("type", types[index]);
        	startActivityForResult(myIntent, CREATE_CODE);    		    		
    	}
    	else if(group == R.id.sectionGroup){
    		
    		int index = Character.getNumericValue(item.getNumericShortcut());
            ContentListView clv = (ContentListView) findViewById(R.id.listView);
    		String[] sections = getResources().getStringArray(R.array.SECTIONS);
            clv.loadData(sections[index], client);
    	} 
    	//return super.onOptionsItemSelected(item);
    	return true;
    }
    
//    public void showFrontPage(View v){
//    	Intent myIntent = new Intent(this, com.jon.android.mgodroid.FrontPageActivity.class);
//    	startActivity(myIntent); 
//    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
		String uid = auth.getString("uid", null);    	
		ImageButton loginButton = (ImageButton) findViewById(R.id.LoginButton);

		if(uid != null){						
			loginButton.setVisibility(View.INVISIBLE);
		}
		else{			
			loginButton.setVisibility(View.VISIBLE);
		}

    	
    	if(requestCode ==LOGIN_CODE){
    		//BasicNameValuePair[] bnvp = new BasicNameValuePair[1];
    		//bnvp[0] = new BasicNameValuePair("nid", "1");
    		String result = null;
    		Bundle extras = data.getExtras();
    		String user = extras.getString("com.gohandee.gohandeeapp.user");
    		String password = extras.getString("com.gohandee.gohandeeapp.password");
    		try {
    			client.login(user, password);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

    	}
    	else if(requestCode == CREATE_CODE){
    		String result = null;
    		Bundle extras = data.getExtras();
    		String title = extras.getString("com.gohandee.gohandeeapp.title");
    		String body = extras.getString("com.gohandee.gohandeeapp.body");
    		String type = extras.getString("type");

    		String name = auth.getString("name", null);
    		try {
    			DrupalNode node = new DrupalNode();
    			node.setTitle(title);
    			node.setBody(body);
    			node.setType(type);
    			node.setUID(uid);
    			//TODO: load node data
    			client.nodeSave(node);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	//super.onActivityResult(requestCode, resultCode, data);
    }

	public class ContentListViewSelectionListener implements OnItemSelectedListener{

		private Context context;

		public ContentListViewSelectionListener(
				Context context) {
			this.context = context;
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View v, int arg2,
				long arg3) {
			TextView nid = (TextView) v.findViewById(R.id.nodeIDV);
			try {
				Intent intent = new Intent(context, com.gohandee.gohandeeapp.Ringtone.class);
				intent.putExtra("nid", nid.getText().toString());
				//TODO: pass off to content view activity
				startActivity(intent);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
		
	
	public class ContentListViewClickListener implements OnItemClickListener{

		private Context context;

		public ContentListViewClickListener(Context context) {
			this.context = context;
		}


		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int arg2,
				long arg3) {
			TextView nid = (TextView) v.findViewById(R.id.nodeIDV);
			try {
				Intent intent = new Intent(context, com.gohandee.gohandeeapp.Ringtone.class);
				intent.putExtra("nid", nid.getText().toString());
				//TODO: pass off to content view activity
				startActivity(intent);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}