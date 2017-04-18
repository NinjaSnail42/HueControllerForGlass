package com.ninjasnailstudios.huecontollerforglass;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;

public class Error extends Activity {
    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_error);
		
	    mGestureDetector = createGestureDetector(this);
	    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
            //Create a base listener for generic gestures
            gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
                @Override
                public boolean onGesture(Gesture gesture) {
                    if (gesture == Gesture.TAP) {
                    	mAudioManager.playSoundEffect(Sounds.DISALLOWED);
                        return true;
                    } else if (gesture == Gesture.TWO_TAP) {
                        // do something on two finger tap
                        return true;
                    } else if (gesture == Gesture.SWIPE_RIGHT) {
                        return true;
                    } else if (gesture == Gesture.SWIPE_LEFT) {
                        // do something on left (backwards) swipe
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
        
   public void searchActivity(){
	   startActivity(new Intent(Error.this, Home.class));
		finish();
   }
}
