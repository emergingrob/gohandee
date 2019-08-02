package com.gohandee.gohandeeapp;
/**
 * 
 * Copyright (c) 2011 Mujtaba Hassanpur.
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.os.Environment;
import android.os.Message;
import android.util.Log;
/**
 * Downloads a file in a thread. Will send messages to the
 * AndroidFileDownloader activity to update the progress bar.
 */
public class DownloaderThread extends Thread
{
	// constants
	private static final int DOWNLOAD_BUFFER_SIZE = 4096;
	
	// instance variables
	private MyDocuments parentActivity;
	private String downloadUrl;
	private String romDirectory;
	private String fileName;
	private String outName;
	
	/**
	 * Instantiates a new DownloaderThread object.
	 * @param parentActivity Reference to AndroidFileDownloader activity.
	 * @param inUrl String representing the URL of the file to be downloaded.
	 */
	public DownloaderThread(MyDocuments inParentActivity, String inUrl, String romDir)
	{
		downloadUrl = "";
		if(inUrl != null)
		{
			downloadUrl = inUrl.replaceAll(" ", "%20");
		}
		romDirectory = romDir;
		parentActivity = inParentActivity;
		fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
	}
	
	
	/**
	 * Connects to the URL of the file, begins the download, and notifies the
	 * AndroidFileDownloader activity of changes in state. Writes the file to
	 * the root of the SD card.
	 */
	@Override
	public void run() {
		URL url;
		URLConnection conn;
		int fileSize, lastSlash;
		String replaceName;
		BufferedInputStream inStream;
		BufferedOutputStream outStream;
		File outFile;
		FileOutputStream fileStream;
		Message msg;
		
		// we're going to connect now
		msg = Message.obtain(parentActivity.activityHandler,
				MyDocuments.MESSAGE_CONNECTING_STARTED,
				0, 0, downloadUrl);
		parentActivity.activityHandler.sendMessage(msg);
		
		try
		{
			url = new URL(downloadUrl);
			conn = url.openConnection();
			conn.setUseCaches(false);
			fileSize = conn.getContentLength();			
			Log.v("SIZE", String.valueOf(fileSize));
			

			
			// get the filename
			lastSlash = url.toString().lastIndexOf('/');
			fileName = "file.bin";
			replaceName = "file.bin";
			
			if(lastSlash >=0) {
				fileName = url.toString().substring(lastSlash + 1);				
			}
			if(fileName.equals("")) {
				fileName = "file.bin";
				replaceName = "file.bin";
			}			
			
			outName = fileName.replaceAll("%20", " ");
			
			// notify download start
			int fileSizeInKB = fileSize / 1024;
			msg = Message.obtain(parentActivity.activityHandler,
					MyDocuments.MESSAGE_DOWNLOAD_STARTED,
					fileSizeInKB, 0, fileName);
			parentActivity.activityHandler.sendMessage(msg);
			
			// start download
			inStream = new BufferedInputStream(conn.getInputStream());
			outFile = new File(Environment.getExternalStorageDirectory() + "/" + romDirectory + outName);
						
			fileStream = new FileOutputStream(outFile);
			outStream = new BufferedOutputStream(fileStream, DOWNLOAD_BUFFER_SIZE);
			byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
			int bytesRead = 0, totalRead = 0;
			
			while(!isInterrupted() && (bytesRead = inStream.read(data, 0, data.length)) >= 0)
			{
				outStream.write(data, 0, bytesRead);
				
				// update progress bar
				totalRead += bytesRead;
				int totalReadInKB = totalRead / 1024;
				msg = Message.obtain(parentActivity.activityHandler,
						MyDocuments.MESSAGE_UPDATE_PROGRESS_BAR,
						totalReadInKB, 0);
				parentActivity.activityHandler.sendMessage(msg);
			}
			
			outStream.close();
			fileStream.close();
			inStream.close();
			
			if(isInterrupted()) {
				// the download was canceled, so let's delete the partially downloaded file
				outFile.delete();
			} else {
				// notify completion
				msg = Message.obtain(parentActivity.activityHandler,
						MyDocuments.MESSAGE_DOWNLOAD_COMPLETE);
				parentActivity.activityHandler.sendMessage(msg);
				File file = new File(Environment.getExternalStorageDirectory() + "/" + romDirectory + outName);
				parentActivity.openFile(file);
			}
		}
		
		catch(MalformedURLException e) {
			String errMsg = parentActivity.getString(R.string.error_message_bad_url);
			msg = Message.obtain(parentActivity.activityHandler,
					MyDocuments.MESSAGE_ENCOUNTERED_ERROR,
					0, 0, errMsg);
			parentActivity.activityHandler.sendMessage(msg);
		}
		catch(FileNotFoundException e)
		{
			String errMsg = parentActivity.getString(R.string.error_message_file_not_found);
			msg = Message.obtain(parentActivity.activityHandler,
					MyDocuments.MESSAGE_ENCOUNTERED_ERROR,
					0, 0, errMsg);
			parentActivity.activityHandler.sendMessage(msg); 
		}
		catch(Exception e)
		{
			String errMsg = parentActivity.getString(R.string.error_message_general);
			msg = Message.obtain(parentActivity.activityHandler,
					MyDocuments.MESSAGE_ENCOUNTERED_ERROR,
					0, 0, errMsg);
			parentActivity.activityHandler.sendMessage(msg); 
		}
	}
	
}