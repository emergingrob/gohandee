package com.gohandee.gohandeeapp;

import com.gohandee.services.DrupalService;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
	}
	
	public void close(View v){
		
		Bundle stats = new Bundle();
		TextView user = (TextView)findViewById(R.id.user);
		TextView password = (TextView)findViewById(R.id.pass);
        stats.putString("com.gohandee.gohandeeapp.user",user.getText().toString()); 
        stats.putString("com.gohandee.gohandeeapp.password", password.getText().toString());
        
        //set bundle
        this.getIntent().putExtras(stats);
        setResult(RESULT_OK, this.getIntent());//(RESULT_OK, stats);
		finish();
	}
	
}