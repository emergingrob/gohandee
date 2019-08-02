package com.gohandee.gohandeeapp;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.gohandee.quickaction.*;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.webkit.MimeTypeMap;
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


public class MyDocuments extends ListActivity implements Runnable {
	// Used to communicate state changes in the DownloaderThread
	public static final int MESSAGE_DOWNLOAD_STARTED = 1000;
	public static final int MESSAGE_DOWNLOAD_COMPLETE = 1001;
	public static final int MESSAGE_UPDATE_PROGRESS_BAR = 1002;
	public static final int MESSAGE_DOWNLOAD_CANCELED = 1003;
	public static final int MESSAGE_CONNECTING_STARTED = 1004;
	public static final int MESSAGE_ENCOUNTERED_ERROR = 1005;
	
	// instance variables
	private ProgressDialog dialog;	
	private MyDocuments thisActivity;
	private Thread downloaderThread;
	private ProgressDialog progressDialog;
	public String urlInput = "";
	public String filePath;	
    String docDir = "GoHandee/Docs";
    int docCount;
    boolean[] downloaded;
    String[] paths;    
    
	static int LIST_SIZE;
	private int mLastOffset = 0;
 
	static int BATCH_SIZE = 25;
 
	private List<DrupalNode> documents;
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
			setContentView(R.layout.documentsview);
			
	        thisActivity = this;
	        downloaderThread = null;
	        progressDialog = null;
			
			final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);  
			//actionBar.addAction(new SearchAction());
			actionBar.addAction(new uploadAction());
  			actionBar.setTitle("My Documents & Files");    	 
  
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
  			
	        Thread thread = new Thread(this);
	        thread.start();

			final ListView lv = getListView();
			lv.setTextFilterEnabled(false);
			lv.setItemsCanFocus(true);
			lv.setOnItemClickListener(docClicked);
	        
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
  


		}

		private OnItemClickListener docClicked = new OnItemClickListener() {				
			public void onItemClick(AdapterView parent, View v, int position, long id)
	    {
		    	String nid =(String) ((TextView) v.findViewById(R.id.docPath)).getText();

				DrupalNode node = new DrupalNode();
				node.setNid(Long.parseLong(nid));
				Log.v("NID", nid);
//		        ImageView iv = (ImageView) this.findViewById(R.id.romImage);

				//node.setNid(nid);
				try {
					DrupalNode retNode = client.nodeGet(node);							
					filePath = retNode.getDocPath();
					downloadFile(filePath);
					Log.v("FP", filePath);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 	
	    	
	    }

	    
	};

	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}   	
	
	public void quickAudio(View v) {		
		RelativeLayout vwParentRow = (RelativeLayout)v.getParent(); 		
    	TextView docNid = (TextView)vwParentRow.getChildAt(2);
		nid = docNid.getText().toString();
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
    
    private class init extends AsyncTask<Void, Void, List<DrupalNode>>  {
		 @Override		 
		 protected List<DrupalNode> doInBackground(Void... params) {	
		        client = ServiceFactory.getService(getApplicationContext(), getString(R.string.SERVER_TYPE));
				SharedPreferences auth = getSharedPreferences(getString(R.string.sharedpreferences_name), 0);
				String uid = auth.getString("uid", null);
								
				AdView ad = (AdView) findViewById(R.id.adView);
				AdRequest request = new AdRequest();
				ad.loadAd(request);					
				
				 documents = new ArrayList<DrupalNode>();
				 LIST_SIZE = 0; 
				 
				 try {	
						LIST_SIZE = Integer.parseInt((client.countType("documents", uid))) - 2;
						Log.v("LISTSIZE", String.valueOf(LIST_SIZE));
						if (BATCH_SIZE > LIST_SIZE) BATCH_SIZE = LIST_SIZE;
						documents = client.viewsGet("MyDocuments", null, uid, 0, 20);				
					 } catch (Exception e) {
						 // TODO Auto-generated catch block
						 e.printStackTrace();
					 }  
				 
				 Log.v("BATCHSIZE", String.valueOf(BATCH_SIZE));
				 setLastOffset(BATCH_SIZE);
				 
				 
				 return documents;
		 }
		 protected void onPostExecute(List<DrupalNode> result) {			
			 dialog.dismiss();
			 displayList(documents);
		 }		 
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
	LinearLayout myDocumentsView;

	DrupalNode item = getItem(position);		
	String myDocName = item.getTitle();		
	//long size = Long.valueOf(item.getDocSize());
	
	if (convertView == null) {
		myDocumentsView = new LinearLayout(getContext());
		String inflater = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
		vi.inflate(resource, myDocumentsView, true);			
	} else {
		myDocumentsView = (LinearLayout) convertView;
	}
	
	ImageView imageView = (ImageView) myDocumentsView.findViewById(R.id.fileIcon);			
	TextView docView = (TextView)myDocumentsView.findViewById(R.id.docName);
	TextView docPath = (TextView)myDocumentsView.findViewById(R.id.docPath);
	TextView docSize = (TextView)myDocumentsView.findViewById(R.id.docSize);
	TextView docChange = (TextView)myDocumentsView.findViewById(R.id.docChanged);
	
	docChange.setText(item.getChanged());
	docView.setText(myDocName);
	String getSize = item.getDocSize();
	
	if (getSize != "null") {
		docSize.setText(humanReadableByteCount(Long.valueOf(getSize), false));
	} else {
		docSize.setText(item.getDocSize());
	}
	
	docPath.setText(String.valueOf(item.getNid()));		

	String ext = myDocName.substring(myDocName.lastIndexOf(".")+1);
	
	if (ext.equals("apk")) {
		imageView.setImageResource(R.drawable.apk);
	} else if (ext.equals("pdf")) {
		imageView.setImageResource(R.drawable.pdf);	
	} else if (ext.equals("mobi") || ext.equals("lrf") || ext.equals("lrx") || ext.equals("epub") || ext.equals("pdb") || ext.equals("ibooks")) {
		imageView.setImageResource(R.drawable.book);
	} else if (ext.equals("zip") || ext.equals("rar") || ext.equals("z") || ext.equals("7z") || ext.equals("ace") || ext.equals("alz")
			|| ext.equals("tar") || ext.equals("gz")) {
		imageView.setImageResource(R.drawable.archive);		
	} else {
		imageView.setImageResource(R.drawable.file);
	}				
	
	return myDocumentsView;

  }


 }

 class DemoAdapter extends EndlessAdapter {
  private RotateAnimation rotate=null;
  ArrayList<DrupalNode> tempList = new ArrayList<DrupalNode>();
  
  DemoAdapter() {
   super(new CustomArrayAdapter(MyDocuments.this, 
     R.layout.mydocuemts_item, documents));
      
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
		documents = client.viewsGet("MyAudio", null, uid, lastOffset, BATCH_SIZE);
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
    arrAdapterNew.add(documents.get(i));
   }
  }
 }
 
 public void downloadFile(String filePath){
		URL url;
		URLConnection conn;
		int fileSize;
		
		urlInput = "http://gohandee.s3.amazonaws.com/" + filePath;	
						
		Log.v("DTHREAD", urlInput);
		String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

		File file = new File(Environment.getExternalStorageDirectory() + "/" + docDir + "/" + fileName);		
		if(file.exists()) {
			openFile(file);         		
		} else {
			try {
				url = new URL(urlInput);
				conn = url.openConnection();
				conn.setUseCaches(false);
				fileSize = conn.getContentLength();	
				
				if (fileSize < 1)
					urlInput = "http://www.gohandee.com/" + filePath;
				
				
				Log.v("DTHREAD", urlInput);
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			downloaderThread = new DownloaderThread(thisActivity, urlInput, docDir + "/");
			downloaderThread.start();
		}
  }	
	
  public void openFile(File file) {
      Intent intent = new Intent();
      intent.setAction(android.content.Intent.ACTION_VIEW);
    
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      String ext = file.getName().substring(file.getName().lastIndexOf(".")+1);
      String type = mime.getMimeTypeFromExtension(ext);
      
      Log.v("MIMETYPE", type);
      Log.v("EXT", ext);
      
      //if (ext == "apk") type = "application/vnd.android.package-archive";
      
      intent.setDataAndType(Uri.fromFile(file),type);
    
      PackageManager pm = getPackageManager();
      List<ResolveInfo> apps = 
          pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

      if (apps.size() > 0)
          startActivity(intent);
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
 
	 public void run() {		 		 
		 //myDocuments ---a list of drupal node with doc data
			URL url;
			URLConnection conn;
			int fileSize;
			long nid;				
			paths = new String[docCount];
			downloaded = new boolean[docCount];

//					url = new URL(urlInput);
//					conn = url.openConnection();
//					conn.setUseCaches(false);
//					fileSize = conn.getContentLength();	
					
//					if (fileSize < 1)
						urlInput = "http://www.gohandee.com/" + filePath;			
			
		 for (int i = 0; i < docCount; i++) {
				DrupalNode node = documents.get(i);				
				nid = node.getNid();
				node.setNid(nid);
				
				try {
					node = client.nodeGet(node);
					paths[i] = node.getDocPath();
					
					Log.v("DOCSIZE", node.getDocSize());
					
					urlInput = "http://gohandee.s3.amazonaws.com/" + paths[i];
					url = new URL(urlInput);
					conn = url.openConnection();
					conn.setUseCaches(false);
					fileSize = conn.getContentLength();
					
					if (fileSize < 1) {
						urlInput = "http://www.gohandee.com/" + paths[i];
					url = new URL(urlInput);
					conn = url.openConnection();
					conn.setUseCaches(false);
					fileSize = conn.getContentLength();
					}
					
					String fileName = paths[i].substring(paths[i].lastIndexOf('/') + 1);
					File file = new File(Environment.getExternalStorageDirectory() + "/" + docDir + "/" + fileName);
					
					Log.v("ON-SIZE", String.valueOf(node.getDocSize()));
					Log.v("LOC-SIZE", String.valueOf(file.length()));

					
					if(fileSize == file.length()) {
						downloaded[i] = true;
						Log.v("DLED", "true");
					} else {
						downloaded[i] = false;
						Log.v("DLED", "false");						
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		    			 
		 }
		 
		 
	 }
		public static String humanReadableByteCount(long bytes, boolean si) {
		    int unit = si ? 1000 : 1024;
		    if (bytes < unit) return bytes + " B";
		    int exp = (int) (Math.log(bytes) / Math.log(unit));
		    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
		    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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

 private class uploadAction implements Action {

	    @Override
	    public int getDrawable() {
	        return R.drawable.upload;
	    } 

	    @Override
	    public void performAction(View view) {
	        Intent myIntent = new Intent(MyDocuments.this, Upload.class);
	        MyDocuments.this.startActivity(myIntent); 		
	    }

	}  
 
}