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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.droidfu.widgets.WebImageView;
import com.gohandee.services.DrupalNode;
import com.gohandee.services.DrupalService;
import com.gohandee.services.ServiceFactory;
import com.gohandee.media.StreamingMediaPlayer;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class Ringtone extends Activity {
	MediaPlayer mp=new MediaPlayer();
	private static final int CREATE_CODE = 1;
	private DrupalService service;
	private Bundle extras;
	
	private Button streamButton;	
	private ImageButton playButton;	
	private boolean isPlaying;	
	private StreamingMediaPlayer audioStreamer;	
	private TextView textStreamed;
	String audioPath;
	String audioInfo;
	String p;
	String userPath;
	Float ringtoneRating;
	private int audioSize;
	private int totalSeconds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.ringtone);
		
	    final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
	    
        final Action shareAction = new IntentAction(this, createShareIntent(), R.drawable.ic_title_share_default);
        actionBar.addAction(shareAction);
        
		AdView ad = (AdView) findViewById(R.id.adView);
		AdRequest request = new AdRequest();
		ad.loadAd(request);
		  
        Button downloadRing = (Button) findViewById(R.id.downloadRingtone);
		
		
		initControls();
		extras = getIntent().getExtras();
		service = ServiceFactory.getService(this, getString(R.string.SERVER_TYPE));
		WebImageView userPic = (WebImageView)this.findViewById(R.id.userPic);		 
		String nid = extras.getString("nid");
		
		Float fRating = extras.getFloat("rating");
		
		Log.v("RATING", String.valueOf(fRating));
		DrupalNode node = new DrupalNode();
		node.setNid(Long.parseLong(nid));
		
		try {
			DrupalNode retNode = service.nodeGet(node);
			audioPath = retNode.getAudio();
			audioInfo = retNode.getAudioInfo();
			userPath = retNode.getUserPic();
			//ringtoneRating = Float.valueOf(retNode.getRating());

	        actionBar.setTitle(retNode.getTitle());		
			
			TextView author = (TextView) findViewById(R.id.contentAuthor);
			author.setText("by " + retNode.getName() + " on " + retNode.getChanged());
			WebView text = (WebView) findViewById(R.id.contentBody);
			text.loadData(retNode.getBody(), "text/html", "utf-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		userPic.reset();
		userPic.setImageUrl("http://www.gohandee.com/" +userPath);
		userPic.loadImage();
		
		   RatingBar rating = (RatingBar) findViewById(R.id.ratingBar);   
		// TextView ringRating = (TextView)ringtonesView.findViewById(R.id.ringtoneRating);

		   rating.setRating(fRating / 20);		
		
		// TODO Check if audioPath is NULL
		
		// Parse AudioInfo
	       try {
	            JSONObject myjson = new JSONObject(audioInfo);
	            JSONArray nameArray = myjson.names();
	            JSONArray valArray = myjson.toJSONArray(nameArray);
            	//nameArray.getString(9) + "," + 
                for(int i=0;i<valArray.length();i++)
                {
                    String p = nameArray.getString(i) + "," + valArray.getString(i);
                    Log.i("p",p);
                }                       
                p = valArray.getString(9);
                JSONObject audioFileInfo = new JSONObject(valArray.getString(3));
	            JSONArray audioArray = audioFileInfo.names();
	            JSONArray aValArray = audioFileInfo.toJSONArray(audioArray);
            	//nameArray.getString(9) + "," + 
                for(int i=0;i<aValArray.length();i++)
                {
                    String audio = audioArray.getString(i) + "," + aValArray.getString(i);
                    Log.i("audioFile",audio);
                }                
                audioSize = Integer.parseInt(aValArray.getString(5));
	        } catch (JSONException e) {
	                e.printStackTrace();
	        }
		
        int colon = p.indexOf(":"); 	        
	    int minutes = Integer.parseInt(p.substring(0, colon));
	    int seconds = Integer.parseInt(p.substring(colon + 1, p.length()));
	    totalSeconds = (minutes * 60) + seconds;
	    
		audioPath = audioPath.replaceAll("[{#:}\"]", "");
		audioPath = audioPath.replaceAll("\\(", "");
		audioPath = audioPath.replace("\\/", "/");
		audioPath = audioPath.replaceAll("value", "");			
		audioPath = audioPath.replace("[","").replace("]","");
		//audioPath = audioPath.replaceAll(" ", "+");
		audioPath = audioPath.replaceAll("\\s+", "+");

		audioPath = "https://s3.amazonaws.com/gohandee/" + audioPath;
		Log.i("AUDIOPATH",audioPath);
		startStreamingAudio();

		toggleComments(this.findViewById(R.layout.ringtone));
		
		downloadRing.setOnClickListener(new OnClickListener() {
    		public void onClick(View view) {
    	    	new downloadRingtone().execute();
    		}
		});
		
	}
	
	@Override
	protected void onPause () {
		try {
			if (audioStreamer.getMediaPlayer().isPlaying()) {
				audioStreamer.getMediaPlayer().pause();
				playButton.setImageResource(R.drawable.button_play);
			}
		} catch (Exception e) {
				e.printStackTrace();
		}
		
	    super.onPause();
	}
	
	
    private void initControls() {

    	textStreamed = (TextView) findViewById(R.id.text_kb_streamed);
		streamButton = (Button) findViewById(R.id.button_stream);
		streamButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startStreamingAudio();
        }});

		playButton = (ImageButton) findViewById(R.id.button_play);
		playButton.setEnabled(false);
		playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (audioStreamer.getMediaPlayer().isPlaying()) {
					audioStreamer.getMediaPlayer().pause();
					playButton.setImageResource(R.drawable.button_play);
				} else {
					audioStreamer.getMediaPlayer().start();
					audioStreamer.startPlayProgressUpdater();
					playButton.setImageResource(R.drawable.button_pause);
				}
				isPlaying = !isPlaying;
        }});
    }	

    private void startStreamingAudio() {
    	try { 
    		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    		if ( audioStreamer != null) {
    			audioStreamer.interrupt();
    		}
    		audioStreamer = new StreamingMediaPlayer(this,textStreamed, playButton, streamButton,progressBar);
    		//audioStreamer.startStreaming("http://www.pocketjourney.com/downloads/pj/tutorials/audio.mp3",1717, 214);
    		audioStreamer.startStreaming(audioPath, audioSize, totalSeconds);
    		//streamButton.setEnabled(false);
    	} catch (IOException e) {
	    	Log.e(getClass().getName(), "Error starting to stream audio.", e);            		
    	}
    	    	
    }
    
    
	private class downloadRingtone extends AsyncTask<Void, Void, Integer> {
	    final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);	   
	    
	    protected void onPreExecute()
	    {
	    	actionBar.setProgressBarVisibility(View.VISIBLE);
	    }										
		
	     protected Integer doInBackground(Void... params) {
	 		String audioName = audioPath.replace("https://s3.amazonaws.com/gohandee/sites/default/files/audio/", "");
	    	downloadFromUrl(audioPath, audioName);
	    	return 0;
	     }	    
	     
	     protected void onPostExecute(Integer result) {
	        actionBar.setProgressBarVisibility(View.GONE);
	     }	    	    
 		    	    	    	
	}
	
	public void setRingtone(View v) {
		String audioName = audioPath.replace("https://s3.amazonaws.com/gohandee/sites/default/files/audio/", "");
		File k = new File("/sdcard/GoHandee/Ringtones/", audioName); // path is a file to /sdcard/media/ringtone

		//TODO fill in values here
		ContentValues values = new ContentValues();		
		values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, "My Song title");
		values.put(MediaStore.MediaColumns.SIZE, 215454);
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
		values.put(MediaStore.Audio.Media.ARTIST, "Madonna");
		values.put(MediaStore.Audio.Media.DURATION, 230);
		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
		values.put(MediaStore.Audio.Media.IS_ALARM, true);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);
		
		//Insert it into the database
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(k.getAbsolutePath());
		Uri newUri = Ringtone.this.getContentResolver().insert(uri, values);

		RingtoneManager.setActualDefaultRingtoneUri(
		  Ringtone.this,
		  RingtoneManager.TYPE_RINGTONE,
		  newUri
		);
		
	}

	public void downloadFromUrl(String DownloadUrl, String fileName) {

		   try {
		           File root = android.os.Environment.getExternalStorageDirectory();               

		           File dir = new File (root.getAbsolutePath() + "/GoHandee/Ringtones");
		           if(dir.exists()==false) {
		                dir.mkdirs();
		           }

		           URL url = new URL(DownloadUrl); //you can write here any link
		           File file = new File(dir, fileName);

		           long startTime = System.currentTimeMillis();
		           Log.d("DownloadManager", "download begining");
		           Log.d("DownloadManager", "download url:" + url);
		           Log.d("DownloadManager", "downloaded file name:" + fileName);

		           // https://s3.amazonaws.com/gohandee/sites/default/files/audio/7661Wii+Sports+Theme+-+Ringtone.mp3
		           
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
		   //actionBar.setProgressBarVisibility(View.GONE);
		}	
	
	public void toggleComments(View v){
		LinearLayout container = (LinearLayout) findViewById(R.id.commentContainer);
		if(!container.isShown()){
			String nid = extras.getString("nid");
			Log.v("EXTRASCOMMENT", nid);
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
}
