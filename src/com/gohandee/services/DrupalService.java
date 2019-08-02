package com.gohandee.services;

import java.util.List;

import com.insready.drupalcloud.ServiceNotAvailableException;

public interface DrupalService {

	public static final String MethodNodeSave = "node.save";
	public static final String MethodNodeGet = "node.get";
	public static final String MethodSystemConnect = "system.connect";
	public static final String MethodUserLogout = "user.logout";
	public static final String MethodUserLogin = "user.login";
	public static final String MethodFileSave = "file.save";
	public static final String MethodTestCount = "test.count";

	/**
	 * Connect to the remote service
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract void connect() throws Exception;

	/**
	 * Call user.login
	 * 
	 * @return
	 */
	public abstract void login(String username, String password)
			throws Exception;

	/**
	 * Call user.logout
	 */
	public abstract void logout(String sessionID) throws Exception;

	/**
	 * Call node.save
	 * 
	 * @param node
	 *            the node to save
	 */	
	
	public abstract void userSave(DrupalNode node) throws Exception;
	
	public abstract void nodeSave(DrupalNode node) throws Exception;

	public abstract DrupalNode nodeGet(DrupalNode node) throws Exception;
	
	public abstract int commentSave(DrupalNode node) throws Exception;

	public abstract List<DrupalNode> commentLoadNodeComments(long nid, int count, int start) throws Exception;
	
	public abstract DrupalNode commentLoad(long cid) throws Exception;
	
	public abstract List<DrupalNode> viewsGet(String view_name, String display_id, String args,
			int offset, int limit) throws Exception;
	
	public abstract String countType(String type, String user) throws Exception;
	
	public abstract List<DrupalNode> myUserRelationships() throws Exception;
	
	public abstract DrupalNode userGet(int user) throws Exception;
	
	public abstract void nodeDelete(String nid) throws Exception;
}