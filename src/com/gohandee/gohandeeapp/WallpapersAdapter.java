package com.gohandee.gohandeeapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.SystemClock;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.*;

import com.gohandee.services.DrupalNode;
import com.gohandee.gohandeeapp.EndlessAdapter;

public class WallpapersAdapter extends ArrayAdapter<DrupalNode> {
    private RotateAnimation rotate=null;

	int resource;
    private ArrayList<DrupalNode> items;
    
	public WallpapersAdapter(Context _context, int _resource, List<DrupalNode> _items) {
		super(_context, _resource, _items);
		resource = _resource;
	}
    
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout ringtonesView;
		
		DrupalNode item = getItem(position);
//       DrupalNode item = items.get(position);
        
		String wallpaperName = item.getTitle();	
		String wallpaperNid = String.valueOf(item.getNid());
		
		if (convertView == null) {
			ringtonesView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
			vi.inflate(resource, ringtonesView, true);			
		} else {
			ringtonesView = (LinearLayout) convertView;
		}
		
		TextView ringView = (TextView)ringtonesView.findViewById(R.id.ringtoneName);
		TextView ringCat = (TextView)ringtonesView.findViewById(R.id.ringtoneCategory);
		
		ringView.setText(wallpaperName);
		ringCat.setText(wallpaperNid);
		
		
		return ringtonesView;
		
	}
}