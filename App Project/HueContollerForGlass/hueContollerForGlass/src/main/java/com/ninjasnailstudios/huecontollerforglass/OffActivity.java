package com.ninjasnailstudios.huecontollerforglass;

import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class OffActivity extends Activity {

	   	private PHHueSDK phHueSDK;
	    public static final String TAG = "QuickStart";
	    
	    private GestureDetector mGestureDetector;
	    private AudioManager mAudioManager;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_off);
			
		    phHueSDK = PHHueSDK.create();
		        
		    mGestureDetector = createGestureDetector(this);
		    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		}
		
		  public void lightsOff() {
		        PHBridge bridge = phHueSDK.getSelectedBridge();

		        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
		        Random rand = new Random();
		        
		        for (PHLight light : allLights) {
		            PHLightState lightState = new PHLightState();
		            lightState.setOn(false);
		            bridge.updateLightState(light, lightState, listener);
		            }
		    }
		    // If you want to handle the response from the bridge, create a PHLightListener object.
		    PHLightListener listener = new PHLightListener() {
		        
		        @Override
		        public void onSuccess() {  
		        }
		        
		        @Override
		        public void onStateUpdate(Hashtable<String, String> arg0, List<PHHueError> arg1) {
		           Log.w(TAG, "Light has updated");
		        }
		        
		        @Override
		        public void onError(int arg0, String arg1) {  
		        }
		    };
		    
		    @Override
		    protected void onDestroy() {
		        PHBridge bridge = phHueSDK.getSelectedBridge();
		        if (bridge != null) {
		            
		            if (phHueSDK.isHeartbeatEnabled(bridge)) {
		                phHueSDK.disableHeartbeat(bridge);
		            }
		            
		            phHueSDK.disconnect(bridge);
		            super.onDestroy();
		        }
		    }
		    
		    private GestureDetector createGestureDetector(Context context) {
		        GestureDetector gestureDetector = new GestureDetector(context);
		            //Create a base listener for generic gestures
		            gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
		                @Override
		                public boolean onGesture(Gesture gesture) {
		                    if (gesture == Gesture.TAP) {
		                    	lightsOff();
		                    	mAudioManager.playSoundEffect(Sounds.TAP);
		                        return true;
		                    } else if (gesture == Gesture.TWO_TAP) {
		                        // do something on two finger tap
		                        return true;
		                    } else if (gesture == Gesture.SWIPE_RIGHT) {
		                    	randomActivity();
		                    	mAudioManager.playSoundEffect(Sounds.SELECTED);
		                        return true;
		                    } else if (gesture == Gesture.SWIPE_LEFT) {
		                    	onActivity();
		                    	mAudioManager.playSoundEffect(Sounds.SELECTED);
		                    	return true;
		                    }
		                    return false;
		                }
		            });
		            gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
		                @Override
		                public void onFingerCountChanged(int previousCount, int currentCount) {
		                  // do something on finger count changes
		                }
		            });
		            
		            return gestureDetector;
		        }

		        /*
		         * Send generic motion events to the gesture detector
		         */
		        @Override
		        public boolean onGenericMotionEvent(MotionEvent event) {
		            if (mGestureDetector != null) {
		                return mGestureDetector.onMotionEvent(event);
		            }
		            return false;
		        }
		 	   public void onActivity(){
				   startActivity(new Intent(OffActivity.this, OnActivity.class));
					
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
					finish();
			   }
		 	   
		 	   public void randomActivity(){
				   startActivity(new Intent(OffActivity.this, RandomActivity.class));
					
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
					finish();
			   }
		    
		    @Override
		    public void onBackPressed() {
		    	finish();
		    	mAudioManager.playSoundEffect(Sounds.DISMISSED);
		    }
	}
