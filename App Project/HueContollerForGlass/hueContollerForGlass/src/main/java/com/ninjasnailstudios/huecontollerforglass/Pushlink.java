package com.ninjasnailstudios.huecontollerforglass;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import com.google.android.glass.media.Sounds;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;

import com.google.glass.widget.SliderView;

public class Pushlink extends Activity {
    private PHHueSDK phHueSDK;
    private boolean isDialogShowing;  
    private AudioManager mAudioManager;
	private SliderView mProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pushlink);
		
	       isDialogShowing=false;
	        phHueSDK = PHHueSDK.getInstance();
	        phHueSDK.getNotificationManager().registerSDKListener(listener);
			
	        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	        
			mProgress = (SliderView) findViewById(R.id.progress_slider_view);
			mProgress.startProgress(30 * 1000);
	}
	
	  @Override
	    protected void onStop(){
	        super.onStop();
	        phHueSDK.getNotificationManager().unregisterSDKListener(listener);
	    }

	    public void incrementProgress() {
	        //pbar.incrementProgressBy(1);
	    }
	    
	    private PHSDKListener listener = new PHSDKListener() {

	        @Override
	        public void onAccessPointsFound(List<PHAccessPoint> arg0) {}

	        @Override
	        public void onAuthenticationRequired(PHAccessPoint arg0) {}

	        @Override
	        public void onBridgeConnected(PHBridge arg0) {}

	        @Override
	        public void onCacheUpdated(int arg0, PHBridge arg1) {}

	        @Override
	        public void onConnectionLost(PHAccessPoint arg0) {}

	        @Override
	        public void onConnectionResumed(PHBridge arg0) {}

	        @Override
	        public void onError(int code, final String message) {
	            if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED) {
	                incrementProgress();
	            }
	            else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
	                incrementProgress();
		            mAudioManager.playSoundEffect(Sounds.ERROR);

	                if (!isDialogShowing) {
	                    isDialogShowing=true;
	                    Pushlink.this.runOnUiThread(new Runnable() {

	                        @Override
	                        public void run() {
	                            //AlertDialog.Builder builder = new AlertDialog.Builder(Pushlink.this);
	                            //builder.setMessage(message).setNeutralButton(R.string.btn_ok,
	                                    //new DialogInterface.OnClickListener() {
	                                        //public void onClick(DialogInterface dialog, int id) {
	                                            //finish();
	                                        //}
	                                    //});

	                            //builder.create();
	                            //builder.show();
	                        }
	                    });
	                }
	                
	            }

	        } // End of On Error
	    };
	    
	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	        if (listener !=null) {
	            phHueSDK.getNotificationManager().unregisterSDKListener(listener);
	        }
            mAudioManager.playSoundEffect(Sounds.SUCCESS);
	        finish();
	    }
	    
}
