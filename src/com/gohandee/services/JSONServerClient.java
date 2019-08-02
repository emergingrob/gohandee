/**
 * 
 */
package com.gohandee.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.insready.drupalcloud.ServiceNotAvailableException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Services Server output formats that are currently supported:
 * 
 * @author Jingsheng Wang
 */
public class JSONServerClient implements DrupalService {
	public HttpPost mSERVER;
	public static String mAPI_KEY;
	public static String mDOMAIN;
	public static String mALGORITHM;
	public static Long mSESSION_LIFETIME;
	private HttpClient mClient = new DefaultHttpClient();
	private List<NameValuePair> mPairs = new ArrayList<NameValuePair>(15);
	private Context mCtx;
	private final String mPREFS_AUTH;

	/**
	 * 
	 * @param _ctx
	 *            Context
	 * @param _prefs_auth
	 *            Preference storage
	 * @param _server
	 *            Server address
	 * @param _api_key
	 *            API_Key
	 * @param _domain
	 *            Domain name
	 * @param _algorithm
	 *            Encrypition algorithm
	 * @param _session_lifetime
	 *            Session lifetime
	 */
	public JSONServerClient(Context _ctx, String _prefs_auth, String _server,
			String _api_key, String _domain, String _algorithm,
			Long _session_lifetime) {
		mPREFS_AUTH = _prefs_auth;
		mSERVER = new HttpPost(_server);
		mSERVER.setHeader("User-Agent", "DrupalCloud-1.x");
		mAPI_KEY = _api_key;
		mDOMAIN = _domain;
		mALGORITHM = _algorithm;
		mSESSION_LIFETIME = _session_lifetime;
		mCtx = _ctx;
	}

	private String getSessionID() throws ServiceNotAvailableException {
		SharedPreferences auth = mCtx.getSharedPreferences(mPREFS_AUTH, 0);
		Long timestamp = auth.getLong("sessionid_timestamp", 0);
		Long currenttime = new Date().getTime() / 100;
		String sessionid = auth.getString("sessionid", null);
		if (sessionid == null || (currenttime - timestamp) >= mSESSION_LIFETIME) {
			systemConnect();
			sessionid = auth.getString("sessionid", null);
			//return getSessionID();
		} //else
			return sessionid;
	}

	/**
	 * Generic request
	 * 
	 * @param method
	 *            Request name
	 * @param parameters
	 *            Parameters
	 * @return result string
	 */
	public String call(String method, BasicNameValuePair[] parameters)
			throws ServiceNotAvailableException {
		String sessid = this.getSessionID();
		mPairs.clear();
		String nonce = Integer.toString(new Random().nextInt());
		Mac hmac;

		try {
			hmac = Mac.getInstance(JSONServerClient.mALGORITHM);
			final Long timestamp = new Date().getTime() / 100;
			final String time = timestamp.toString();
			hmac.init(new SecretKeySpec(JSONServerClient.mAPI_KEY.getBytes(),
					JSONServerClient.mALGORITHM));
			String message = time + ";" + JSONServerClient.mDOMAIN + ";"
					+ nonce + ";" + method;
			hmac.update(message.getBytes());
			String hmac_value = new String(Hex.encodeHex(hmac.doFinal()));
			mPairs.add(new BasicNameValuePair("hash", "\"" + hmac_value + "\""));
			mPairs.add(new BasicNameValuePair("domain_name",
					"\"" + JSONServerClient.mDOMAIN + "\""));
			mPairs.add(new BasicNameValuePair("domain_time_stamp", "\"" + time + "\""));
			mPairs.add(new BasicNameValuePair("nonce", "\"" + nonce + "\""));
			mPairs.add(new BasicNameValuePair("method", "\"" + method + "\""));
			mPairs.add(new BasicNameValuePair("api_key",
					"\"" + JSONServerClient.mAPI_KEY + "\""));
			mPairs.add(new BasicNameValuePair("sessid", "\"" + sessid + "\""));
			for (int i = 0; i < parameters.length; i++) {
				mPairs.add(parameters[i]);
			}
			mSERVER.setEntity(new UrlEncodedFormEntity(mPairs));
			HttpResponse response = mClient.execute(mSERVER);
			InputStream is = response.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String result = br.readLine();
			JSONObject jso;
			jso = new JSONObject(result);
			boolean error = jso.getBoolean("#error");
			if (error) {
				String errorMsg = jso.getString("#data");
				throw new ServiceNotAvailableException(errorMsg);
			}
			return result;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
			throw new ServiceNotAvailableException("Remote server is not available");
		}
		return null;
	}

	/**
	 * system.connect request for Key Auth
	 */
	private void systemConnect() throws ServiceNotAvailableException {
		// Cloud server hand shake
		mPairs.add(new BasicNameValuePair("method", "\"system.connect\""));
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(mPairs);
			String entityString = entity.toString();
			mSERVER.setEntity(entity);
			HttpResponse response = mClient.execute(mSERVER);
			InputStream result = response.getEntity().getContent();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(result));
			JSONObject jso = new JSONObject(br.readLine());
			boolean error = jso.getBoolean("#error");
			String data = jso.getString("#data");
			if (error) {
				throw new ServiceNotAvailableException(data);
			}

			jso = new JSONObject(data);
			// Save the sessionid to storage
			saveSession(data);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void saveSession(String data) throws JSONException {
		JSONObject jso = new JSONObject(data);
		SharedPreferences auth = mCtx.getSharedPreferences(mPREFS_AUTH, 0);
		SharedPreferences.Editor editor = auth.edit();
		editor.putString("sessionid", jso.getString("sessid"));
		editor.putLong("sessionid_timestamp", new Date().getTime() / 100);
		editor.commit();
	}

	@Override
	public void login(String username, String password)
			throws ServiceNotAvailableException {
		String response = null;
		try {
			BasicNameValuePair[] parameters = new BasicNameValuePair[2];
			parameters[0] = new BasicNameValuePair("username", "\"" + username + "\"");
			parameters[1] = new BasicNameValuePair("password", "\"" + password + "\"");
			//update session id with result from call
			response = call("user.login", parameters); 
			JSONObject jso = new JSONObject(response);
			JSONObject data = jso.getJSONObject("#data");
			saveSession(data.toString());
			JSONObject user = data.getJSONObject("user");
			SharedPreferences auth = mCtx.getSharedPreferences(mPREFS_AUTH, 0);
			SharedPreferences.Editor editor = auth.edit();
			editor.putString("uid", user.getString("uid"));
			editor.putString("name", user.getString("name"));
			editor.commit();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void logout(String sessionID)
			throws ServiceNotAvailableException {
		if(sessionID == null){
			systemConnect();
			SharedPreferences auth = mCtx.getSharedPreferences(mPREFS_AUTH, 0);
			sessionID = auth.getString("sessionid", null);

		}
		BasicNameValuePair[] parameters = new BasicNameValuePair[1];
		parameters[0] = new BasicNameValuePair("sessid", "\"" + sessionID + "\"");
		String response = call("user.logout", parameters);
			SharedPreferences auth = mCtx.getSharedPreferences(mPREFS_AUTH, 0);
		SharedPreferences.Editor editor = auth.edit();
		editor.remove("uid");
		editor.remove("name");
		editor.remove("sessionid");
		editor.remove("sessionid_timestamp");
		editor.commit();

	}

	@Override
	public String countType(String type, String user) throws ServiceNotAvailableException {
		BasicNameValuePair[] parameters = new BasicNameValuePair[2];
		parameters[0] = new BasicNameValuePair("type", "\"" + type + "\"");
		parameters[1] = new BasicNameValuePair("user", "\"" + user + "\"");
		String result = call("count.type", parameters);
		//DrupalNode node = new DrupalNode();
		try {
			JSONObject jso = new JSONObject(result);						
			result = jso.getString("#data");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}			
	
	@Override
	public List<DrupalNode> myUserRelationships() throws ServiceNotAvailableException {
		List<DrupalNode> nodes = new ArrayList<DrupalNode>();
		BasicNameValuePair[] parameters = new BasicNameValuePair[1];
		parameters[0] = new BasicNameValuePair("version", "\"" + String.valueOf(1) + "\"");
		String result = call("user_relationships.mine", parameters);

		DrupalNode node = new DrupalNode();
		try {
			JSONObject jso = new JSONObject(result);
			Log.v("FRIENDS", result);
			JSONArray array = jso.getJSONArray("#data");
			for(int i = 0; i < array.length(); i++){
				node = buildNode(array.getJSONObject(i));
				nodes.add(node);
			}					
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodes;
	}
		
	@Override
	public DrupalNode userGet(int user) throws ServiceNotAvailableException {
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		DrupalNode node = new DrupalNode();
		
		parameters.add(new BasicNameValuePair("uid", "" + String.valueOf(user) + ""));
		String result = call("user.get", parameters.toArray(new BasicNameValuePair[0]));

		try {
			Log.v("USER", result);
			JSONObject jso = new JSONObject(result);
			node = buildNode(jso.getJSONObject("#data"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return node;
	}
	
	@Override
	public void nodeDelete(String nid) throws ServiceNotAvailableException {		
		BasicNameValuePair[] parameters = new BasicNameValuePair[1];
		parameters[0] = new BasicNameValuePair("nid", "\"" + nid + "\"");
		String response = call("node.delete", parameters);			
		Log.v("DEL", response);
	}
	
	@Override
	public DrupalNode nodeGet(DrupalNode node)
			throws ServiceNotAvailableException {
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		parameters.add(new BasicNameValuePair("nid", "" + node.getNid() + ""));
//		if(node.getFields() != null){
//			parameters.add(new BasicNameValuePair("fields", "\"" + node.getFields() + "\""));
//		}
		String result = call("node.get", parameters.toArray(new BasicNameValuePair[0]));

		/*
		 * try { JSONObject jso = new JSONObject(temp); jso = new
		 * JSONObject(jso.getString("#data")); JSONArray nameArray =
		 * jso.names(); JSONArray valArray = jso.toJSONArray(nameArray); for
		 * (int i=0;i<valArray.length();i++){
		 * Log.i("Testing","<jsonmae"+i+">\n"+
		 * nameArray.getString(i)+"\n</jsonname"+i+">\n"
		 * +"<jsonvalue"+i+">\n"+valArray.getString(i)+"\n</jsonvalue"+i+">"); }
		 * } catch (JSONException e) { e.printStackTrace(); }
		 */
		try {
			JSONObject jso = new JSONObject(result);
			node = buildNode(jso.getJSONObject("#data"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node;
	}

	@Override
	public List<DrupalNode> viewsGet(String view_name, String display_id, String args,
			int offset, int limit) throws ServiceNotAvailableException {
		List<DrupalNode> nodes = new ArrayList<DrupalNode>();
		List<BasicNameValuePair> paramList = new ArrayList<BasicNameValuePair>();
//		BasicNameValuePair[] parameters = new BasicNameValuePair[5];
		paramList.add(new BasicNameValuePair("view_name", "\"" + view_name + "\""));				
		//args = "ring";
		
		if(args != null){
			Log.v("ARGS", args);
			//paramList.add(new BasicNameValuePair("args", "\"" + args + "\""));
			paramList.add(new BasicNameValuePair("args", "[" + args + "]"));
		}
		if(display_id != null){
			paramList.add(new BasicNameValuePair("display_id", "\"" + display_id + "\""));
		}
		if(offset != -1){
			paramList.add(new BasicNameValuePair("offset", "\"" + offset + "" + "\""));
		}
		if(limit != -1){
			paramList.add(new BasicNameValuePair("limit", "\"" + limit + "" + "\""));
		}
		Log.v("views", paramList.toString());
		String result = call("views.get", paramList.toArray(new BasicNameValuePair[0]));
		DrupalNode node = new DrupalNode();
		try {
			JSONObject jso = new JSONObject(result);
			JSONArray array = jso.getJSONArray("#data");
			for(int i = 0; i < array.length(); i++){
				node = buildNode(array.getJSONObject(i));
				nodes.add(node);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodes;
	}	
	
	private DrupalNode buildNode(JSONObject obj) throws JSONException{
		DrupalNode node = new DrupalNode();
		if (obj.has("body")) node.setBody(obj.getString("body"));
		if (obj.has("nid")) node.setNid(Long.parseLong(obj.getString("nid")));
		if (obj.has("title")) node.setTitle(obj.getString("title"));
		if (obj.has("node_title")) node.setTitle(obj.getString("node_title"));
		if (obj.has("fields")) node.setFields(obj.getString("fields"));
		if (obj.has("uid"))node.setUID(obj.getString("uid"));
		if (obj.has("name")) node.setName(obj.getString("name"));
		if (obj.has("field_audiofile")) node.setAudio(obj.getString("field_audiofile"));
		if (obj.has("audio")) node.setAudioInfo(obj.getString("audio"));
		if (obj.has("picture")) node.setUserPic(obj.getString("picture"));
		if (obj.has("ss_wp_image")) node.setWallPath(obj.getString("ss_wp_image"));
		if (obj.has("field_image_upload")) node.setNodeWall(obj.getString("field_image_upload"));
		if (obj.has("sis_ratings")) node.setRating(Integer.parseInt(obj.getString("sis_ratings")));
		if (obj.has("term_data_name")) node.setRingCategory(obj.getString("term_data_name"));
		if (obj.has("field_uploaded_file")) {
			String filePath = obj.getString("field_uploaded_file");			
			filePath = filePath.replace("[", "").replace("]", "").replace("tags\":", "tags\":\"\"");			
			JSONObject fileU = (JSONObject) new JSONObject(filePath);			
			node.setDocSize(fileU.getString("filesize"));
			node.setDocPath(fileU.getString("filepath"));			
		}
		if (obj.has("node_data_field_uploaded_file_field_filesize_value")) node.setDocSize(obj.getString("node_data_field_uploaded_file_field_filesize_value"));
		if (obj.has("requestee_id")) node.setRequesteeId(obj.getString("requestee_id"));
		if (obj.has("requester_id")) node.setRequesterId(obj.getString("requester_id"));
		if (obj.has("rid")) node.setRid(obj.getString("rid"));
		if (obj.has("node_changed")) node.setChanged(obj.getString("node_changed"));
		if (obj.has("mail")) node.setEmail(obj.getString("mail"));		
		//node.setChanged(obj.getString("changed"));
		return node;
	}

	@Override
	public int commentSave(DrupalNode node) throws ServiceNotAvailableException {
		JSONObject array = new JSONObject();
		//{"type":"page","uid":"1","title":"TITLE","body":"BODY"}
//		BasicNameValuePair[] parameters = new BasicNameValuePair[4];
		//need uid, type, title, body in node
//		parameters[0] = new BasicNameValuePair("type", "\"" + node.getType() + "\"");
//		parameters[1] = new BasicNameValuePair("title", "\"" + node.getTitle() + "\"");
//		parameters[2] = new BasicNameValuePair("body", "\"" + node.getBody() + "\"");
//		parameters[3] = new BasicNameValuePair("uid", "\"" + node.getUID() + "\"");
		try {
		array.put("uid", node.getUID());
		array.put("subject", node.getTitle());
		array.put("comment", node.getBody());
		array.put("name", node.getName());
		array.put("pid", node.getCid());
		array.put("nid", node.getNid());
		BasicNameValuePair[] nvp = {new BasicNameValuePair("comment", array.toString())};
		String result = call("comment.save", nvp);

		JSONObject jso;
			jso = new JSONObject(result);
			int cid = jso.getInt("#data");
			return cid;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public List<DrupalNode>  commentLoadNodeComments(long nid, int count, int start)
			throws ServiceNotAvailableException {
		Map<String,DrupalNode> nodes = new TreeMap<String, DrupalNode>();
		BasicNameValuePair[] parameters = new BasicNameValuePair[3];
		parameters[0] = new BasicNameValuePair("nid", "\"" + String.valueOf(nid) + "\"");
		parameters[1] = new BasicNameValuePair("count", "\"" + String.valueOf(count) + "\"");
		parameters[2] = new BasicNameValuePair("start", "\"" + String.valueOf(start) + "\"");
		String result = call("comment.loadNodeComments", parameters);
		// Convert other line breaks to Unix line breaks
		result = result.replaceAll("(\\\\r\\\\n|\\\\r)", "\\\\n");
		try {
			JSONObject jso = new JSONObject(result);
			JSONArray array = jso.getJSONArray("#data");
			//comments ordered by thread. 01 < 01.00 < 02
			for(int i = 0; i < array.length(); i++){
				JSONObject obj = array.getJSONObject(i);
				DrupalNode node = buildComment(obj);
				nodes.put(node.getThread(), node);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<DrupalNode>(nodes.values());
	}
	
	private DrupalNode buildComment(JSONObject obj) throws JSONException {
		//need subject, comment, user, thread (parse "." for heirarchy, cid (comment id), nid, timestamp
		DrupalNode node = new DrupalNode();
		node.setTitle(obj.getString("subject"));
		node.setBody(obj.getString("comment"));
		node.setUID(obj.getString("uid"));
		node.setName(obj.getString("name"));
		node.setChanged(obj.getString("timestamp"));
		node.setNid(obj.getLong("nid"));
		node.setCid(obj.getLong("cid"));
		node.setThread(obj.getString("thread").replace("/", ""));
		return node;
	}

	@Override
	public DrupalNode commentLoad(long cid) throws ServiceNotAvailableException {
		BasicNameValuePair[] parameters = new BasicNameValuePair[1];
		parameters[0] = new BasicNameValuePair("cid", "\"" + String.valueOf(cid) + "\"");
		String result = call("comment.load", parameters);
		DrupalNode node = new DrupalNode();
		try {
			JSONObject jso = new JSONObject(result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node;
	}	

	@Override
	public void connect() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeSave(DrupalNode node) throws Exception {
		JSONObject array = new JSONObject();
		//{"type":"page","uid":"1","title":"TITLE","body":"BODY"}
//		BasicNameValuePair[] parameters = new BasicNameValuePair[4];
		//need uid, type, title, body in node
//		parameters[0] = new BasicNameValuePair("type", "\"" + node.getType() + "\"");
//		parameters[1] = new BasicNameValuePair("title", "\"" + node.getTitle() + "\"");
//		parameters[2] = new BasicNameValuePair("body", "\"" + node.getBody() + "\"");
//		parameters[3] = new BasicNameValuePair("uid", "\"" + node.getUID() + "\"");
		array.put("type", node.getType());
		array.put("uid", node.getUID());
		array.put("title", node.getTitle());
		array.put("body", node.getBody());
		array.put("name", node.getName());
		BasicNameValuePair[] nvp = {new BasicNameValuePair("node", array.toString())};
		String result = call("node.save", nvp);
		Log.v("RES", result);
		try {
			JSONObject jso = new JSONObject(result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void userSave(DrupalNode account) throws Exception {
		JSONObject array = new JSONObject();
		//array.put("uid", account.getUID());
		//array.put("name", account.getName());
		//array.put("mail", "robmil29@yahoo.com");
		//array.put("pass", "123");
		//BasicNameValuePair[] nvp = {new BasicNameValuePair("account", array.toString())};
		//Log.v("ARRAY", array.toString());
		//String result = call("user.save", nvp);
		//Log.v("RESULT", result);
		//try {
		//	JSONObject jso = new JSONObject(result);
		//} catch (JSONException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		
		String result2 = null;


		BasicNameValuePair[] bnvp3 = new BasicNameValuePair[1];
		bnvp3[0] = new BasicNameValuePair("account", "{\"uid\":\"3000\",\"pass\":\"132\",\"name\":\"Hello\",\"mail\":\"r@go.com\"}");

		try { 
		result2 = call("user.save", bnvp3);
		Log.d("result", "Here "+result2);
		} catch (ServiceNotAvailableException e) {
		e.printStackTrace();
		}				
		
		
		Vector params = new Vector ( );
		String response = null;
		try {
			BasicNameValuePair[] parameters = new BasicNameValuePair[4];
			parameters[0] = new BasicNameValuePair("uid", "\"" + "3000" + "\"");
			parameters[1] = new BasicNameValuePair("pass", "\"" + "123" + "\"");
			parameters[2] = new BasicNameValuePair("name", "\"" + "hello" + "\"");
			parameters[3] = new BasicNameValuePair("mail", "\"" + "r@go.com" + "\"");

		    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();       
		    nameValuePairs.add(new BasicNameValuePair("uid", "\"" + "3000" + "\""));
		    nameValuePairs.add(new BasicNameValuePair("pass", "\"" + "123" + "\""));
		    nameValuePairs.add(new BasicNameValuePair("name", "\"" + "hello" + "\""));
		    nameValuePairs.add(new BasicNameValuePair("mail", "\"" + "r@go.com" + "\""));
						
			array.put("uid", "3000");
			array.put("name", "blah");
			array.put("mail", "robm9@yoo.com");
			array.put("pass", "123");
		    
			BasicNameValuePair[] nvp = {new BasicNameValuePair("account", array.toString())};
			//BasicNameValuePair[] nvp = {new BasicNameValuePair("account", array.toString())};			
			//BasicNameValuePair[] nvp = {new BasicNameValuePair("account", parameters.toString())};
			//update session id with result from call
			//Log.v("NVP",array.toString());
			response = call("user.save", nvp); 
			
			Log.v("RESPONSE", response);
			//JSONObject jso = new JSONObject(response);
			//JSONObject data = jso.getJSONObject("#data");
			//saveSession(data.toString());
			//JSONObject user = data.getJSONObject("user");
			//SharedPreferences auth = mCtx.getSharedPreferences(mPREFS_AUTH, 0);
			//SharedPreferences.Editor editor = auth.edit();
			//editor.putString("uid", user.getString("uid"));
			//editor.putString("name", user.getString("name"));
			//editor.commit();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
	}	
	
	
	
}
