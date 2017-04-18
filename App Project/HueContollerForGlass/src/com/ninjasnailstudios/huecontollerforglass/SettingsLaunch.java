package com.ninjasnailstudios.huecontollerforglass;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.ninjasnailstudios.huecontrollerforglass.data.HueSharedPreferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

public class SettingsLaunch extends Activity {
    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_launch);
	    mGestureDetector = createGestureDetector(this);
	    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}
	
    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
            //Create a base listener for generic gestures
            gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
                @Override
                public boolean onGesture(Gesture gesture) {
                    if (gesture == Gesture.TAP) {
                    	goToBridgeSettings();
                    	mAudioManager.playSoundEffect(Sounds.TAP);
                        return true;
                    } else if (gesture == Gesture.TWO_TAP) {
                        return true;
                    } else if (gesture == Gesture.SWIPE_RIGHT) {
                        return true;
                    } else if (gesture == Gesture.SWIPE_LEFT) {
                    	randomActivity();
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
            gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
                @Override
                public boolean onScroll(float displacement, float distanceX, float distanceY) {
                    if (distanceX < -1) {
                        //Scroll Right or Scroll Up Command Here
                    } else if (distanceX > 1) {
                        //Scroll left or Scroll Down Command Here
                    }
                    return true;
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
        
 	   public void goToBridgeSettings(){
		   startActivity(new Intent(SettingsLaunch.this, BridgeSettingsMenu.class));
	   }
 	   
 	   public void randomActivity(){
		   startActivity(new Intent(SettingsLaunch.this, RandomActivity.class));
			
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			finish();
	   }
 	   
 	    @Override
 	    public void onBackPressed() {
 	    	finish();
 	    	mAudioManager.playSoundEffect(Sounds.DISMISSED);
 	    }
}
