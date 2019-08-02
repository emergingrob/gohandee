/**
 *
 */
package com.gohandee.services;

import java.util.HashMap;
import java.util.Map;

import android.text.format.Time;

/**
 * A drupal Node. It sets and gets all its variables as MAP entries.
 * 
 * @author sauermann
 * 
 */
public class DrupalNode extends HashMap<String, Object> {

	/**
	 * types
	 */
	public static final String CID = "cid";

	public static final String THREAD = "THREAD";

	public static final String NID = "nid";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String LANGUAGE = "language";
	public static final String UID = "uid";
	public static final String STATUS = "status";
	public static final String CREATED = "created";
	public static final String CHANGED = "changed";
	public static final String TITLE = "thitle";
	public static final String BODY = "body";
	public static final String TEASER = "teaser";
	public static final String FIELDS = "fields";

	public static final String AUDIO = "field_audiofile";
	public static final String WALLPATH = "ss_wp_image";
	public static final String NODEWALL = "field_image_upload";
	public static final String RATING = "sis_ratings";
	public static final String AUDIOINFO = "audio";
	public static final String USERPIC = "picture";
	public static final String RINGCAT = "term_data_name";
	public static final String DOCPATH = "field_uploaded_file";
	public static final String DOCSIZE = "docsize";
	
	// User Relationship Object
	public static final String REQUESTEE = "requestee_id";
	public static final String REQUESTER = "requester_id";
	public static final String RID = "rid";	
	public static final String EMAIL = "email";
	
	/* 
	 * more for future public static String NID = "comment"; public static
	 * String NID = "promote"; public static String NID = "moderate"; public
	 * static String NID = "sticky"; public static String NID = "tnid"; public
	 * static String NID = "translate"; public static String NID = "vid"; public
	 * static String NID = "revision_uid"; public static String NID = "teaser";
	 * public static String NID = "log"; public static String NID =
	 * "revision_timestamp"; public static String NID = "format"; public static
	 * String NID = "name"; public static String NID = "picture"; public static
	 * String NID = "data"; public static String NID = "rdf"; public static
	 * String NID = "last_comment_timestamp"; public static String NID =
	 * "last_comment_name"; public static String NID = "comment_count"; public
	 * static String NID = "taxonomy"; public static String NID = "build_mode";
	 * public static String NID = "readmore"; public static String NID =
	 * "content";
	 */

	public DrupalNode() {
		super();
	}

	public DrupalNode(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public DrupalNode(int initialCapacity) {
		super(initialCapacity);
	}

	public DrupalNode(Map m) {
		super(m);
	}

	public long getNid() {
		Object nid = get(NID);
		if (nid == null)
			throw new RuntimeException("nid not set (null)");
		if (nid instanceof String) {
			long result = Long.parseLong((String) nid);
			put(NID, result); // replace, for future
			return result;
		} else
			return (Long) nid;
	}

	public void setNid(long nid) {
		put(NID, nid);
	}
	
	public long getRating() {
		Object rating = get(RATING);
		if (rating == null)
			rating = 0;
		if (rating instanceof String) {
			int result = Integer.parseInt((String) rating);			
			put(RATING, result); // replace, for future
			return result;
		} else
			return (Integer) rating;
	}

	public void setRating(int rating) {
		put(RATING, rating);
	}
	
	public String getName() {
		Object o = get(NAME);
		if (o == null)
			return null;
		else
			return o.toString();
	}

	public void setName(String o) {
		put(NAME, o);
	}	
	
	public String getEmail() {
		Object o = get(EMAIL);
		if (o == null)
			return null;
		else
			return o.toString();
	}

	public void setEmail(String o) {
		put(EMAIL, o);
	}	
	
	public void setRequesterId(String o) {
		put(REQUESTER, o);
	}

	public String getRequesterId() {
		Object o = get(REQUESTER);
		if (o == null)
			return null;
		else
			return o.toString();
	}	

	public void setRequesteeId(String o) {
		put(REQUESTEE, o);
	}

	public String getRequesteeId() {
		Object o = get(REQUESTEE);
		if (o == null)
			return null;
		else
			return o.toString();
	}	
	
	public void setRid(String o) {
		put(RID, o);
	}

	public String getRid() {
		Object o = get(RID);
		if (o == null)
			return null;
		else
			return o.toString();
	}		
	
	public String getUserPic() {
		Object o = get(USERPIC);
		if (o == null)
			return null;
		else
			return o.toString();
	}

	public void setUserPic(String o) {
		put(USERPIC, o);
	}	

	public String getDocPath() {
		Object o = get(DOCPATH);
		if (o == null)
			return "DocPath";
		else
			return o.toString();
	}

	public void setDocPath(String o) {
		put(DOCPATH, o);
	}
	
	public String getDocSize() {
		Object o = get(DOCSIZE);
		if (o == null)
			return "DocSize";
		else
			return o.toString();
	}

	public void setDocSize(String o) {
		put(DOCSIZE, o);
	}		

	public String getTitle() {
		Object o = get(TITLE);
		if (o == null)
			return null;
		else
			return o.toString();
	}

	public void setTitle(String o) {
		put(TITLE, o);
	}

	public String getBody() {
		Object o = get(BODY);
		if (o == null)
			return null;
		else
			return o.toString();
	}

	public void setBody(String o) {
		put(BODY, o);
	}
	
	public String getAudio() {
		Object o = get(AUDIO);
		if (o == null)
			return null;
		else
			return o.toString();
	}
	
	public void setAudio(String o) {
		put(AUDIO, o);
	}
	
	public String getAudioInfo() {
		Object o = get(AUDIOINFO);
		if (o == null)
			return null;
		else
			return o.toString();
	}
	
	public void setAudioInfo(String o) {
		put(AUDIOINFO, o);
	}
	
	public String getRingCategory() {
		Object o = get(RINGCAT);
		if (o == null)
			return "RingCat is Null";
		else
			return o.toString();
	}
	
	public void setRingCategory(String o) {
		put(RINGCAT, o);
	}		
	
	public String getWallPath() {
		Object o = get(WALLPATH);
		if (o == null)
			return null;
		else
			return o.toString();
	}
	
	public void setWallPath(String o) {
		put(WALLPATH, o);
	}	

	public String getNodeWall() {
		Object o = get(NODEWALL);
		if (o == null)
			return null;
		else
			return o.toString();
	}
	
	public void setNodeWall(String o) {
		put(NODEWALL, o);
	}		
	
	public String getType(){
		Object o = get(TYPE);
		if (o == null)
			return null;
		else
			return o.toString();
	}

	public void setType(String o) {
		put(TYPE, o);
	}
	
	public String getTeaser(){
		Object o = get(TEASER);
		if (o == null)
			return null;
		else
			return o.toString();
	}
	
	public void setTeaser(String o){
		put(TEASER, o);
	}
	
	public String getUID() {
		Object o = get(UID);
		if (o == null)
			return null;
		else
			return o.toString();
	}

	public void setUID(String o) {
		put(UID, o);
	}

	public String getCreated() {
		Object o = get(CREATED);
		if (o == null)
			return null;
		else{
			return formatTime(o.toString());
		}
	}

	private String formatTime(String string) {
		Time t = new Time();
		t.set(Long.parseLong(string) * 1000);
		return t.format("%B %d, %Y %l:%M %p");
	}

	public void setCreated(String o) {
		put(CREATED, o);
	}

	public String getChanged() {
		Object o = get(CHANGED);
		if (o == null)
			return null;
		else
			return formatTime(o.toString());
	}

	public void setChanged(String o) {
		put(CHANGED, o);
	}

	public String getFields(){
		Object o = get(FIELDS);
		if (o == null)
			return null;
		else
			return o.toString();
	}
	
	public void setFields(String o){
		put(FIELDS, o);
	}
	
	public long getCid(){
		Object cid = get(CID);
		if (cid == null)
			throw new RuntimeException("cid not set (null)");
		if (cid instanceof String) {
			long result = Long.parseLong((String) cid);
			put(CID, result); // replace, for future
			return result;
		} else
			return (Long) cid;
	}

	public void setCid(long o) {
		put(CID, o);
	}

	public String getThread() {
		Object o = get(THREAD);
		if (o == null)
			return null;
		else
			return o.toString();
	}

	public void setThread(String o) {
		put(THREAD, o);
	}

}
