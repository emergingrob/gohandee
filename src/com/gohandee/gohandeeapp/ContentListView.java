package com.gohandee.gohandeeapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.insready.drupalcloud.ServiceNotAvailableException;
import com.gohandee.services.DrupalNode;
import com.gohandee.services.DrupalService;

public class ContentListView extends ListView {

	private DrupalService sandbox;

	public ContentListView(Context context) {
		super(context);
	}

	public ContentListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void loadData(String type, DrupalService sandbox) {
		List<DrupalNode> response;
		this.sandbox = sandbox;
		String check = "1";
		
		try {

				response = sandbox.viewsGet(type, null, check, 0, 10);
				
			//TODO: handle response data
			//JSONObject obj = new JSONObject(response);
			//JSONArray array = obj.getJSONArray("#data");
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();
			for (DrupalNode node: response) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("title",node.getTitle());
				map.put("date", node.getChanged());
				map.put("teaser", node.getTeaser());
				map.put("nid", Long.toString(node.getNid()));
				data.add(map);
			}
			String[] from = { "title", "date", "teaser", "nid" };
			int[] to = { R.id.titleV, R.id.authorV, R.id.teaserV, R.id.nodeIDV };
			SimpleAdapter sa = new SimpleAdapter(getContext(), data,
					R.layout.list_view, from, to);
			sa.setViewBinder(new WebViewBinder());
			setAdapter(sa);
			//sa.notifyDataSetChanged();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public class WebViewBinder implements SimpleAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Object data,
				String textRepresentation) {
			int viewId = view.getId();
			if (viewId == R.id.titleV) {
				TextView text = (TextView) view.findViewById(R.id.titleV);
				text.setText(textRepresentation);
			} else if (viewId == R.id.authorV) {
				TextView text = (TextView) view.findViewById(R.id.authorV);
				text.setText(textRepresentation);
			} else if (viewId == R.id.teaserV) {
				WebView text = (WebView) view.findViewById(R.id.teaserV);
				text.loadData(textRepresentation, "text/html", "utf-8");
			} else if (viewId == R.id.nodeIDV) {
				TextView text = (TextView) view.findViewById(R.id.nodeIDV);
				text.setText(textRepresentation);
			}
			return true;
		}

	}
}
