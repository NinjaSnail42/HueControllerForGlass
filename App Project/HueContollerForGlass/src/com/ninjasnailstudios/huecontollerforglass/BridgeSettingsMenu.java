package com.ninjasnailstudios.huecontollerforglass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.ninjasnailstudios.huecontrollerforglass.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;

import java.util.List;

public class BridgeSettingsMenu extends Activity {
    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;
    private HueSharedPreferences prefs;
    private final Handler mHandler = new Handler();
    private PHHueSDK phHueSDK;
    private PHBridgeConfiguration bridgeConfig;
    private PHBridge bridge;
    private static final int SPEECH_REQUEST = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bridge_settings_menu);
	    mGestureDetector = createGestureDetector(this);
	    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        phHueSDK = PHHueSDK.getInstance();
        bridge = phHueSDK.getSelectedBridge();
        phHueSDK.disableHeartbeat(bridge);
        bridgeConfig = bridge.getResourceCache().getBridgeConfiguration();

        prefs = HueSharedPreferences.getInstance(getApplicationContext());
        String lastIpAddress   = prefs.getLastConnectedIPAddress();
        
        TextView bridgeName = (TextView) findViewById(R.id.nameText);
        bridgeName.setText(bridgeConfig.getName());
        
        TextView macText = (TextView) findViewById(R.id.macText);
        macText.setText("MAC: " + bridgeConfig.getMacAddress());
        
        TextView versionText = (TextView) findViewById(R.id.versionText);
        versionText.setText("Software Version: " + bridgeConfig.getSoftwareVersion());
        
        TextView ipText = (TextView) findViewById(R.id.ipText);       
        ipText.setText("IP: " + lastIpAddress);
	}
	
	   private GestureDetector createGestureDetector(Context context) {
	        GestureDetector gestureDetector = new GestureDetector(context);
	            //Create a base listener for generic gestures
	            gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
	                @Override
	                public boolean onGesture(Gesture gesture) {
	                    if (gesture == Gesture.TAP) {
	                    	openOptionsMenu();
	                    	mAudioManager.playSoundEffect(Sounds.TAP);
	                        return true;
	                    } else if (gesture == Gesture.TWO_TAP) {
	                        return true;
	                    } else if (gesture == Gesture.SWIPE_RIGHT) {
	                        return true;
	                    } else if (gesture == Gesture.SWIPE_LEFT) {

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bridge_settings, menu);
        return true;
    }

@Override
public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    
        case R.id.rename_bridge:
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    renameBridge();
                }
            });
            return true;
            
        case R.id.delete_bridge:
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    deleteBridge();
                }
            });
            return true;

        default:
            return false;
    }
}

private void renameBridge() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    startActivityForResult(intent, SPEECH_REQUEST);
}

private void deleteBridge() {
    prefs.setLastConnectedIPAddress("");
    prefs.setUsername("");
    startActivity(new Intent(this, Home.class));
    finish();
}


@Override
protected void onActivityResult(int requestCode, int resultCode,
        Intent data) {
    if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
        List<String> results = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS);
        String spokenText = results.get(0);
        bridgeConfig.setName(spokenText);
        bridge.updateBridgeConfigurations(bridgeConfig, null);
        TextView bridgeName = (TextView) findViewById(R.id.nameText);
        bridgeName.setText(bridgeConfig.getName());
    }
    super.onActivityResult(requestCode, resultCode, data);
}
}