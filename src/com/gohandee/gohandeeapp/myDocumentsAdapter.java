package com.gohandee.gohandeeapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.gohandee.services.DrupalNode;

public class myDocumentsAdapter extends ArrayAdapter<DrupalNode> {
	int resource;
	
	public myDocumentsAdapter(Context _context, int _resource, List<DrupalNode> _items) {
		super(_context, _resource, _items);
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
		
		docView.setText(myDocName);
		//docSize.setText(humanReadableByteCount(Long.valueOf(item.getDocSize()), false));
		docSize.setText(item.getDocSize());
		docPath.setText(String.valueOf(item.getNid()));		
		
		String ext = myDocName.substring(myDocName.lastIndexOf(".")+1);
		
		if (ext.equals("apk")) {
			imageView.setImageResource(R.drawable.apk);
		} else if (ext.equals("pdf")) {
			imageView.setImageResource(R.drawable.pdf);				
		} else {
			imageView.setImageResource(R.drawable.file);
		}				
		
		return myDocumentsView;
		
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	
}