package com.gohandee.gohandeeapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CreateContentActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.create);

	}

	public void close(View v){
		
		Bundle stats = new Bundle();
		TextView title = (TextView)findViewById(R.id.title);
		TextView body = (TextView)findViewById(R.id.body);
        stats.putString("com.gohandee.gohandeeapp.title",title.getText().toString()); 
        stats.putString("com.gohandee.gohandeeapp.body", body.getText().toString());
        //set bundle
        this.getIntent().putExtras(stats);
        setResult(RESULT_OK, this.getIntent());//(RESULT_OK, stats);
		finish();
	}
}
