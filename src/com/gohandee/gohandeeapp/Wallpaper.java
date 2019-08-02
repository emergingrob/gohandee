package com.gohandee.gohandeeapp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.droidfu.widgets.WebImageView;
import com.gohandee.services.DrupalNode;
import com.gohandee.services.DrupalService;
import com.gohandee.services.ServiceFactory;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.markupartist.android.widget.ActionBar;

public class Wallpaper extends Activity {
	private static final int CREATE_CODE = 1;
	private DrupalService service;
	private Bundle extras;
	String wallPath = "";
	String theWallPath;
	String userPath;

	// Used to communicate state changes in the DownloaderThread
	public static final int MESSAGE_DOWNLOAD_STARTED = 1000;
	public static final int MESSAGE_DOWNLOAD_COMPLETE = 1001;
	public static final int MESSAGE_UPDATE_PROGRESS_BAR = 1002;
	public static final int MESSAGE_DOWNLOAD_CANCELED = 1003;
	public static final int MESSAGE_CONNECTING_STARTED = 1004;
	public static final int MESSAGE_ENCOUNTERED_ERROR = 1005;
	
	// instance variables
	private Wallpaper thisActivity;
	private Thread downloaderThread;
	private ProgressDialog progressDialog;	
	public String urlInput = "";
	public String filePath;		
    private final String PATH = "/GoHandeeDocs/";  //put the downloaded file here

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.wallpaper);

		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		AdView ad = (AdView) findViewById(R.id.adView);
		AdRequest request = new AdRequest();
		ad.loadAd(request);
		
		extras = getIntent().getExtras();
		service = ServiceFactory.getService(this, getString(R.string.SERVER_TYPE));

		final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
		
		String nid = extras.getString("nid");
		DrupalNode node = new DrupalNode();
		node.setNid(Long.parseLong(nid));
		
        final WebImageView iv = (WebImageView)this.findViewById(R.id.wallpaperImage);
        ImageButton setWallpaper = (ImageButton) findViewById(R.id.setWallpaper);
        Button downloadWallpaper = (Button) findViewById(R.id.downloadWallpaper);
        
        WebImageView userPic = (WebImageView)this.findViewById(R.id.userPic);
        
		try {
			DrupalNode retNode = service.nodeGet(node);			
	        wallPath = retNode.getNodeWall();
	        userPath = retNode.getUserPic();
	        actionBar.setTitle(retNode.getTitle());	
	    	
			TextView author = (TextView) findViewById(R.id.contentAuthor);
			if (retNode.getName() == "admin") {
				author.setText("by robmil29" + " on " + retNode.getChanged());
			} else {
				author.setText("by " + retNode.getName() + " on " + retNode.getChanged());
			}
		} catch (Exception e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
        StringBuffer s1 = new StringBuffer(wallPath);         
        int slash = wallPath.lastIndexOf("/");
        s1 = s1.insert(slash - 1, "/imagecache/150x145");         
        
        String wUrl = "http://www.gohandee.com/" + wallPath;
        
		int start = wallPath.indexOf("sites");
		int end = wallPath.indexOf(".jpg");
		wallPath = wallPath.substring(start, end + 4);
		wallPath = wallPath.replaceAll("\\\\", "");
		final String webUrl = "http://www.gohandee.com/" + wallPath;			
        		
		userPic.reset();
		userPic.setImageUrl("http://www.gohandee.com/" +userPath);
		userPic.loadImage();
				
    	iv.reset();                                
    	iv.setImageUrl(webUrl);
    	iv.loadImage();	       			

    	downloadWallpaper.setOnClickListener(new OnClickListener() {
    		public void onClick(View view) {
    	    	//downloadFile(webUrl);	
    			wallPath = wallPath.replace("sites/default/files/wallpapers/", "");
    	    	downloadFromUrl(webUrl, wallPath);
    		}
    	});
    	
        setWallpaper.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
     //               wallpaperDrawable = wallpaperManager.getDrawable();
                  URL aURL = new URL(webUrl);
                  URLConnection conn = aURL.openConnection();
                  conn.connect();
                  InputStream is = conn.getInputStream();
                  /* Buffered is always good for a performance plus. */
                  BufferedInputStream bis = new BufferedInputStream(is);
                  /* Decode url-data to a bitmap. */
                  Bitmap bm = BitmapFactory.decodeStream(bis);
//                  bis.close();
//                  is.close();                	
                    //wallpaperManager.setBitmap(iv.getDrawingCache()); 
                    //wallpaperManager.setStream(is);
                    wallpaperManager.setBitmap(bm);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    	
    	
	}
	
	public void downloadFromUrl(String DownloadUrl, String fileName) {

		   try {
		           File root = android.os.Environment.getExternalStorageDirectory();               

		           File dir = new File (root.getAbsolutePath() + "/GoHandee/Wallpapers");
		           if(dir.exists()==false) {
		                dir.mkdirs();
		           }

		           URL url = new URL(DownloadUrl); //you can write here any link
		           File file = new File(dir, fileName);

		           long startTime = System.currentTimeMillis();
		           Log.d("DownloadManager", "download begining");
		           Log.d("DownloadManager", "download url:" + url);
		           Log.d("DownloadManager", "downloaded file name:" + fileName);

		           /* Open a connection to that URL. */
		           URLConnection ucon = url.openConnection();

		           /*
		            * Define InputStreams to read from the URLConnection.
		            */
		           InputStream is = ucon.getInputStream();
		           BufferedInputStream bis = new BufferedInputStream(is);

		           /*
		            * Read bytes to the Buffer until there is nothing more to read(-1).
		            */
		           ByteArrayBuffer baf = new ByteArrayBuffer(5000);
		           int current = 0;
		           while ((current = bis.read()) != -1) {
		              baf.append((byte) current);
		           }


		           /* Convert the Bytes read to a String. */
		           FileOutputStream fos = new FileOutputStream(file);
		           fos.write(baf.toByteArray());
		           fos.flush();
		           fos.close();
		           Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

		   } catch (IOException e) {
		       Log.d("DownloadManager", "Error: " + e);
		   }

		}

	public void toggleComments(View v){
		LinearLayout container = (LinearLayout) findViewById(R.id.commentContainer);
		if(!container.isShown()){
			String nid = extras.getString("nid");
			try {
				//load comments
				List<DrupalNode> comments = service.commentLoadNodeComments(Long.parseLong(nid), 20, 0);
				
				for(DrupalNode node:comments){
					CommentView cv = new CommentView(this);
					cv.loadData(node);
					container.addView(cv);
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			container.setVisibility(ListView.VISIBLE);
		}
		else{
			//visible, just hide
			container.setVisibility(ListView.INVISIBLE);
		}
	}	
	
	public void newReply(View v){
		v.getId();
		LinearLayout parent = (LinearLayout) v.getParent();
		String nid = extras.getString("nid");
		TextView cid = (TextView)parent.findViewById(R.id.commentCid);
		Intent myIntent = new Intent(this, com.gohandee.gohandeeapp.CreateContentActivity.class);
		myIntent.putExtra("type", "comment");
		//set parent id to comment id we are replying to (cid)
		myIntent.putExtra("pid", cid.getText());
		myIntent.putExtra("nid", nid);
    	startActivityForResult(myIntent, CREATE_CODE);    		    		
		
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == CREATE_CODE){
    		String result = null;
    		Bundle extras = data.getExtras();
    		String title = extras.getString("com.gohandee.gohandeeapp.title");
    		String body = extras.getString("com.gohandee.gohandeeapp.body");
    		String type = extras.getString("type");
    		String pid = extras.getString("pid");
    		String nid = extras.getString("nid");
    		SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
    		String uid = auth.getString("uid", null);
    		String name = auth.getString("name", null);
    		try {
    			DrupalNode node = new DrupalNode();
    			node.setTitle(title);
    			node.setBody(body);
    			node.setName(name);
    			node.setUID(uid);
    			node.setType(type);
    			node.setNid(Long.parseLong(nid));
    			//use cid as holder for pid (parent id)
    			node.setCid(Long.parseLong(pid));
    			//TODO: load node data
    			service.commentSave(node);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	//super.onActivityResult(requestCode, resultCode, data);
    }	
    
	/**
	 * This is the Handler for this activity. It will receive messages from the
	 * DownloaderThread and make the necessary updates to the UI.
	 */
	public Handler activityHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				/*
				 * Handling MESSAGE_UPDATE_PROGRESS_BAR:
				 * 1. Get the current progress, as indicated in the arg1 field
				 *    of the Message.
				 * 2. Update the progress bar.
				 */
				case MESSAGE_UPDATE_PROGRESS_BAR:
					if(progressDialog != null)
					{
						int currentProgress = msg.arg1;
						progressDialog.setProgress(currentProgress);
					}
					break;
				
				/*
				 * Handling MESSAGE_CONNECTING_STARTED:
				 * 1. Get the URL of the file being downloaded. This is stored
				 *    in the obj field of the Message.
				 * 2. Create an indeterminate progress bar.
				 * 3. Set the message that should be sent if user cancels.
				 * 4. Show the progress bar.
				 */
				case MESSAGE_CONNECTING_STARTED:
					if(msg.obj != null && msg.obj instanceof String)
					{
						String url = (String) msg.obj;
						// truncate the url
						if(url.length() > 16)
						{
							String tUrl = url.substring(0, 15);
							tUrl += "...";
							url = tUrl;
						}
						String pdTitle = thisActivity.getString(R.string.progress_dialog_title_connecting);
						String pdMsg = thisActivity.getString(R.string.progress_dialog_message_prefix_connecting);
						pdMsg += " Rom Server";
						
						dismissCurrentProgressDialog();
						progressDialog = new ProgressDialog(thisActivity);
						progressDialog.setTitle(pdTitle);
						progressDialog.setMessage(pdMsg);
						progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progressDialog.setIndeterminate(true);
						// set the message to be sent when this dialog is canceled
						Message newMsg = Message.obtain(this, MESSAGE_DOWNLOAD_CANCELED);
						progressDialog.setCancelMessage(newMsg);
						progressDialog.show();
					}
					break;
					
				/*
				 * Handling MESSAGE_DOWNLOAD_STARTED:
				 * 1. Create a progress bar with specified max value and current
				 *    value 0; assign it to progressDialog. The arg1 field will
				 *    contain the max value.
				 * 2. Set the title and text for the progress bar. The obj
				 *    field of the Message will contain a String that
				 *    represents the name of the file being downloaded.
				 * 3. Set the message that should be sent if dialog is canceled.
				 * 4. Make the progress bar visible.
				 */
				case MESSAGE_DOWNLOAD_STARTED:
					// obj will contain a String representing the file name
					if(msg.obj != null && msg.obj instanceof String)
					{
						int maxValue = msg.arg1;
						String fileName = (String) msg.obj;
						String pdTitle = thisActivity.getString(R.string.progress_dialog_title_downloading);
						String pdMsg = thisActivity.getString(R.string.progress_dialog_message_prefix_downloading);
						pdMsg += " " + fileName;
						
						dismissCurrentProgressDialog();
						progressDialog = new ProgressDialog(thisActivity);
						progressDialog.setTitle(pdTitle);
						progressDialog.setMessage(pdMsg);
						progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						progressDialog.setProgress(0);
						progressDialog.setMax(maxValue);
						// set the message to be sent when this dialog is canceled
						Message newMsg = Message.obtain(this, MESSAGE_DOWNLOAD_CANCELED);
						progressDialog.setCancelMessage(newMsg);
						progressDialog.setCancelable(true);
						progressDialog.show();
					}
					break;
				
				/*
				 * Handling MESSAGE_DOWNLOAD_COMPLETE:
				 * 1. Remove the progress bar from the screen.
				 * 2. Display Toast that says download is complete.
				 */
				case MESSAGE_DOWNLOAD_COMPLETE:
					dismissCurrentProgressDialog();
					displayMessage(getString(R.string.user_message_download_complete));
					break;
					
				/*
				 * Handling MESSAGE_DOWNLOAD_CANCELLED:
				 * 1. Interrupt the downloader thread.
				 * 2. Remove the progress bar from the screen.
				 * 3. Display Toast that says download is complete.
				 */
				case MESSAGE_DOWNLOAD_CANCELED:
					if(downloaderThread != null)
					{
						downloaderThread.interrupt();
					}
					dismissCurrentProgressDialog();
					displayMessage(getString(R.string.user_message_download_canceled));
					break;
				
				/*
				 * Handling MESSAGE_ENCOUNTERED_ERROR:
				 * 1. Check the obj field of the message for the actual error
				 *    message that will be displayed to the user.
				 * 2. Remove any progress bars from the screen.
				 * 3. Display a Toast with the error message.
				 */
				case MESSAGE_ENCOUNTERED_ERROR:
					// obj will contain a string representing the error message
					if(msg.obj != null && msg.obj instanceof String)
					{
						String errorMessage = (String) msg.obj;
						dismissCurrentProgressDialog();
						displayMessage(errorMessage);
					}
					break;
					
				default:
					// nothing to do here
					break;
			}
		}
	};	
	
	/**
	 * If there is a progress dialog, dismiss it and set progressDialog to
	 * null.
	 */
	public void dismissCurrentProgressDialog()
	{
		if(progressDialog != null)
		{
			progressDialog.hide();
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	/**
	 * Displays a message to the user, in the form of a Toast.
	 * @param message Message to be displayed.
	 */
	public void displayMessage(String message)
	{
		if(message != null)
		{
			Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();
		}
	}    
}