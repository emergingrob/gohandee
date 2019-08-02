package com.gohandee.gohandeeapp;

import java.util.List;

import com.gohandee.services.DrupalNode;
import com.gohandee.services.DrupalService;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommentView extends LinearLayout {

	private DrupalNode node;
	
	public CommentView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.comment, this, true);
		
	}
	
	public CommentView(Context context, AttributeSet attrs){
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.comment, this, true);
	
//		  android:layout_width="fill_parent"
//			  android:layout_height="wrap_content"
//			  android:orientation="vertical"
//			  android:layout_marginTop="1px">

	}

	public void loadData(DrupalNode node) {
		this.node = node;
		//subject
		TextView title = (TextView)findViewById(R.id.commentTitle);
		title.setText(node.getTitle());
		//info (user, time)
		TextView desc = (TextView)findViewById(R.id.commentDesc);
		String description = "by " + node.getName() + " on " + node.getChanged();
		desc.setText(description);
		//comment
		WebView body = (WebView)findViewById(R.id.commentBody);
		body.loadData(node.getBody(), "text/html", "utf-8");
		//extras (pid)
		TextView cid = (TextView)findViewById(R.id.commentCid);
		cid.setText(Long.toString(node.getCid()));
		//thread position
		String thread = node.getThread();
		int depth = findDepth(thread);
		setPadding(10 * depth, 0, 0, 0);
	}

	private int findDepth(String thread) {
		int count = 0;
		int p = 0;
		while(p != -1){
			p = thread.indexOf('.', p + 1);
			count++;
		}
		return count;
	}

}
