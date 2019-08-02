package com.gohandee.gohandeeapp;

import com.gohandee.services.DrupalService;
import com.gohandee.services.ServiceFactory;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FrontPageActivity extends Activity {
    private static final int LOGIN_CODE = 0;
	private static final int CREATE_CODE = 1;
	private DrupalService client;	
	boolean customTitleSupported;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
        customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.frontpage);

        customTitleBar(getText(R.string.app_name).toString());        
		client = ServiceFactory.getService(this, getString(R.string.SERVER_TYPE));
		
		SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
		String uid = auth.getString("uid", null);
		
		if (uid == null) {
			
		} else {
			
		}

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
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode ==LOGIN_CODE){
    		//BasicNameValuePair[] bnvp = new BasicNameValuePair[1];
    		//bnvp[0] = new BasicNameValuePair("nid", "1");
    		String result = null;
    		Bundle extras = data.getExtras();
    		String user = extras.getString("com.gohandee.gohandeeapp.user");
    		String password = extras.getString("com.gohandee.gohandeeapp.password");
    		try {
    			client.login("robmil29", "21apples");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

    	}
    }
    
	public void startRingtones(View v) {
        Intent myIntent = new Intent(FrontPageActivity.this, EndlessRingtones.class);
        FrontPageActivity.this.startActivity(myIntent); 		
	}
	
	public void startWallpapers(View v) {
        Intent myIntent = new Intent(FrontPageActivity.this, WallpaperList.class);
        FrontPageActivity.this.startActivity(myIntent); 		
	}	
	public void startLogin(View v) {
        Intent myIntent = new Intent(FrontPageActivity.this, LoginActivity.class);
        FrontPageActivity.this.startActivity(myIntent); 		
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
			MenuItem logout = menu.findItem(R.id.logout);
			logout.setVisible(true);
		}
		else{
			MenuItem login = menu.findItem(R.id.login);
			login.setVisible(true);
			MenuItem content = menu.findItem(R.id.create);
			content.setVisible(false);
			MenuItem logout = menu.findItem(R.id.logout);
			logout.setVisible(false);
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
}
