package com.gohandee.services;

import java.util.List;

import com.insready.drupalcloud.ServiceNotAvailableException;

public class RESTServerClient implements DrupalService {

	@Override
	public int commentSave(DrupalNode node) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public List<DrupalNode> commentLoadNodeComments(long nid, int count, int start) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public DrupalNode commentLoad(long cid) throws ServiceNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void connect() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void login(String username, String password) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void logout(String sessionID) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DrupalNode nodeGet(DrupalNode node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DrupalNode> viewsGet(String view_name, String display_id,
			String args, int offset, int limit) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void nodeSave(DrupalNode node) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void userSave(DrupalNode node) throws Exception {
		// TODO Auto-generated method stub
		
	}	
	
	@Override
	public String countType(String type, String user) throws Exception {
		return null;
	}
	
	@Override
	public List<DrupalNode> myUserRelationships() {
		// TODO Auto-generated method stub
		return null;
	}	
	
	@Override
	public DrupalNode userGet(int user) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}	

	@Override
	public void nodeDelete(String nid) throws Exception {
		
	}
}
