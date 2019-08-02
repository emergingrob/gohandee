package com.gohandee.gohandeeapp;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gohandee.gohandeeapp.MyAccount.MyAccountAdapter;
import com.gohandee.services.DrupalNode;
import com.gohandee.services.DrupalService;
import com.gohandee.services.ServiceFactory;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Upload extends ListActivity {

	   private File currentDir;
	   private FileArrayAdapter adapter;
	   ActionBar actionBar;
	    
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.myaccount);
	        actionBar = (ActionBar) findViewById(R.id.actionbar);
	        actionBar.setTitle("Upload file(s)");
	        currentDir = new File("/sdcard/");
	        fill(currentDir);
	    }
	    private void fill(File f)
	    {
	    	File[]dirs = f.listFiles();
			 this.setTitle("Current Dir: "+f.getName());
			 List<Option>dir = new ArrayList<Option>();
			 List<Option>fls = new ArrayList<Option>();
			 try{
				 for(File ff: dirs)
				 {
					if(ff.isDirectory())
						dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
					else
					{
						fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
					}
				 }
			 }catch(Exception e)
			 {
				 
			 }
			 Collections.sort(dir);
			 Collections.sort(fls);
			 dir.addAll(fls);
			 if(!f.getName().equalsIgnoreCase("sdcard"))
				 dir.add(0,new Option("..","Parent Directory",f.getParent()));
			 adapter = new FileArrayAdapter(Upload.this,R.layout.files,dir);
			 this.setListAdapter(adapter);
	    }
	    @Override
		protected void onListItemClick(ListView l, View v, int position, long id) {
			// TODO Auto-generated method stub
			super.onListItemClick(l, v, position, id);
			Option o = adapter.getItem(position);
			if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
					currentDir = new File(o.getPath());
					fill(currentDir);
			}
			else
			{
				onFileClick(o);
			}
		}
	    private void onFileClick(Option o)
	    {
	    	Toast.makeText(this, "File Clicked: "+o.getName(), Toast.LENGTH_SHORT).show();
	    }


	public class FileArrayAdapter extends ArrayAdapter<Option>{

		private Context c;
		private int id;
		private List<Option>items;
		
		public FileArrayAdapter(Context context, int textViewResourceId,
				List<Option> objects) {
			super(context, textViewResourceId, objects);
			c = context;
			id = textViewResourceId;
			items = objects;
		}
		public Option getItem(int i)
		 {
			 return items.get(i);
		 }
		 @Override
	       public View getView(int position, View convertView, ViewGroup parent) {
	               View v = convertView;
	               if (v == null) {
	                   LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                   v = vi.inflate(id, null);
	               }
	               final Option o = items.get(position);
	               if (o != null) {
	                       TextView t1 = (TextView) v.findViewById(R.id.TextView01);
	                       TextView t2 = (TextView) v.findViewById(R.id.TextView02);
	                       
	                       if(t1!=null)
	                       	t1.setText(o.getName());
	                       if(t2!=null)
	                       	t2.setText(o.getData());
	                       
	               }
	               return v;
	       }

	}
}