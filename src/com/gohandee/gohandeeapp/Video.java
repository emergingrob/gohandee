package com.gohandee.gohandeeapp;
 
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;
import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.MediaController;

 
//The StreamingVideoPlayer activity implements many of the available listener and
//callback abstract classes from MediaPlayer, SurfaceHolder, and MediaController. The
//OnBufferingUpdateListener is particularly useful when dealing with network delivered
//media. This class specifies an onBufferingUpdate method that is repeatedly called while
//the media is buffering, allowing us to keep track of how full the buffer is.

public class Video extends Activity implements
		OnCompletionListener, OnErrorListener, OnInfoListener,
		OnBufferingUpdateListener, OnPreparedListener, OnSeekCompleteListener,
		OnVideoSizeChangedListener, SurfaceHolder.Callback,
		MediaController.MediaPlayerControl {
	
	MediaController controller;
	Display currentDisplay;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	MediaPlayer mediaPlayer;
	View mainView;

//In this version, we’ll use a TextView called statusView to display status messages to the
//user. The reason we’ll do so is that loading a video for playback via the Internet can take
//quite a bit of time, and without some sort of status message, the user may think the
//application has hung.

	TextView statusView;
	int videoWidth = 0;
	int videoHeight = 0;
	boolean readyToPlay = false;

	public final static String LOGTAG = "STREAMING_VIDEO_PLAYER";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);
		mainView = this.findViewById(R.id.myvideoview);
		statusView = (TextView) this.findViewById(R.id.StatusTextView);
		surfaceView = (SurfaceView) this.findViewById(R.id.SurfaceView);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mediaPlayer = new MediaPlayer();
		statusView.setText("MediaPlayer Created");
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnInfoListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnSeekCompleteListener(this);
		mediaPlayer.setOnVideoSizeChangedListener(this);

//Among the list of MediaPlayer event listeners, our activity implements and is registered
//to be the OnBufferingUpdateListener.
//mediaPlayer.setOnBufferingUpdateListener(this);
//Instead of playing back a file from the SD card, we’ll be playing a file served from an
//RTSP server. The URL to the file is specified in the following String, filePath. We’ll then
//use the MediaPlayer’s setDataSource method, passing in the filePath String. The
//MediaPlayer knows how to handle loading and playing data from an RTSP server, so we
//don’t have to do anything else different to handle it.
		String filePath = "https://s3.amazonaws.com/gohandee/sites/default/files/5980AKnightsTale_.3gp";
				try {
					mediaPlayer.setDataSource(filePath);
				} catch (IllegalArgumentException e) {
					Log.v(LOGTAG, e.getMessage());
					finish();
				} catch (IllegalStateException e) {
					Log.v(LOGTAG, e.getMessage());
					finish();
				} catch (IOException e) {
					Log.v(LOGTAG, e.getMessage());
					finish();
				}
		statusView.setText("MediaPlayer DataSource Set");
		currentDisplay = getWindowManager().getDefaultDisplay();
		controller = new MediaController(this);
	}
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(LOGTAG, "surfaceCreated Called");
		mediaPlayer.setDisplay(holder);
		statusView.setText("MediaPlayer Display Surface Set");


//We’ll use the MediaPlayer’s prepareAsync method instead of prepare. The prepareAsync
//method does the preparation in the background on a separate thread. This makes it so
//that the user interface doesn’t hang. This would allow the user to perform other actions
//or allow us as the developer to display a loading animation or something similar.

		try {
			mediaPlayer.prepareAsync();
		} catch (IllegalStateException e) {
			Log.v(LOGTAG, "IllegalStateException " + e.getMessage());
			finish();
		}
		
//So the user knows what’s happening while the prepareAsync method is running, we’ll
//update the status message displayed by our statusView TextView.
		
		statusView.setText("MediaPlayer Preparing");
	}
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.v(LOGTAG, "surfaceChanged Called");
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(LOGTAG, "surfaceDestroyed Called");
	}
	public void onCompletion(MediaPlayer mp) {
		Log.v(LOGTAG, "onCompletion Called");
		statusView.setText("MediaPlayer Playback Completed");
	}
	public boolean onError(MediaPlayer mp, int whatError, int extra) {
		Log.v(LOGTAG, "onError Called");
		statusView.setText("MediaPlayer Error");
		if (whatError == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
			Log.v(LOGTAG, "Media Error, Server Died " + extra);
		} else if (whatError == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
			Log.v(LOGTAG, "Media Error, Error Unknown " + extra);
		}
		return false;
	}
	public boolean onInfo(MediaPlayer mp, int whatInfo, int extra) {
		statusView.setText("MediaPlayer onInfo Called");
		if (whatInfo == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING) {
			Log.v(LOGTAG, "Media Info, Media Info Bad Interleaving " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
			Log.v(LOGTAG, "Media Info, Media Info Not Seekable " + extra);			
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_UNKNOWN) {
			Log.v(LOGTAG, "Media Info, Media Info Unknown " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
			Log.v(LOGTAG, "MediaInfo, Media Info Video Track Lagging " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) {
			Log.v(LOGTAG, "MediaInfo, Media Info Metadata Update " + extra);
		}
		return false;
	}
	public void onPrepared(MediaPlayer mp) {
		Log.v(LOGTAG, "onPrepared Called");
		statusView.setText("MediaPlayer Prepared");
		videoWidth = mp.getVideoWidth();
		videoHeight = mp.getVideoHeight();
		Log.v(LOGTAG, "Width: " + videoWidth);
		Log.v(LOGTAG, "Height: " + videoHeight);
		if (videoWidth > currentDisplay.getWidth()
				|| videoHeight > currentDisplay.getHeight()) {
			float heightRatio = (float) videoHeight
					/ (float) currentDisplay.getHeight();
			float widthRatio = (float) videoWidth
					/ (float) currentDisplay.getWidth();
			if (heightRatio > 1 || widthRatio > 1) {
				if (heightRatio > widthRatio) {
					videoHeight = (int) Math.ceil((float) videoHeight
							/ (float) heightRatio);
					videoWidth = (int) Math.ceil((float) videoWidth
							/ (float) heightRatio);
				} else {
					videoHeight = (int) Math.ceil((float) videoHeight
							/ (float) widthRatio);
					videoWidth = (int) Math.ceil((float) videoWidth
							/ (float) widthRatio);
				}
			}
		}
		surfaceView.setLayoutParams(
				new LinearLayout.LayoutParams(videoWidth, videoHeight));
		controller.setMediaPlayer(this);
		controller.setAnchorView(this.findViewById(R.id.myvideoview));
		controller.setEnabled(true);
		controller.show();
		mp.start();
		statusView.setText("MediaPlayer Started");
	}
	public void onSeekComplete(MediaPlayer mp) {
		Log.v(LOGTAG, "onSeekComplete Called");
	}
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.v(LOGTAG, "onVideoSizeChanged Called");
		videoWidth = mp.getVideoWidth();
		videoHeight = mp.getVideoHeight();
		Log.v(LOGTAG, "Width: " + videoWidth);
		Log.v(LOGTAG, "Height: " + videoHeight);
		if (videoWidth > currentDisplay.getWidth()
				|| videoHeight > currentDisplay.getHeight()) {
			float heightRatio = (float) videoHeight
					/ (float) currentDisplay.getHeight();
			float widthRatio = (float) videoWidth
					/ (float) currentDisplay.getWidth();
			if (heightRatio > 1 || widthRatio > 1) {
				if (heightRatio > widthRatio) {
					videoHeight = (int) Math.ceil((float) videoHeight
							/ (float) heightRatio);
					videoWidth = (int) Math.ceil((float) videoWidth
							/ (float) heightRatio);
				} else {
					videoHeight = (int) Math.ceil((float) videoHeight
							/ (float) widthRatio);
					videoWidth = (int) Math.ceil((float) videoWidth
							/ (float) widthRatio);
				}
			}
		}
		surfaceView.setLayoutParams(
				new LinearLayout.LayoutParams(videoWidth, videoHeight));
	}
//Since our activity implements the OnBufferingUpdateListener and is registered to be the
//listener for the MediaPlayer, the following method will be called periodically as media is
//downloaded and buffered. The buffering will occur during the preparation stage (after
//onPrepareAsync or onPrepare is called).
	public void onBufferingUpdate(MediaPlayer mp, int bufferedPercent) {
		statusView.setText("MediaPlayer Buffering: " + bufferedPercent + "%");
		Log.v(LOGTAG, "MediaPlayer Buffering: " + bufferedPercent + "%");
	}
	public boolean canPause() {
		return true;
	}
	public boolean canSeekBackward() {
		return true;
	}
	public boolean canSeekForward() {
		return true;
	}
	public int getBufferPercentage() {
		return 0;
	}
	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}
	public int getDuration() {
		return mediaPlayer.getDuration();
	}
	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}
	public void pause() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}
	public void seekTo(int pos) {
		mediaPlayer.seekTo(pos);
	}
	public void start() {
		mediaPlayer.start();
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (controller.isShowing()) {
			controller.hide();
		} else {
			controller.show();
		}
		return false;
	}
}