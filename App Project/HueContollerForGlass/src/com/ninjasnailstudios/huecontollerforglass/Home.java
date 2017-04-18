package com.ninjasnailstudios.huecontollerforglass;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.ninjasnailstudios.huecontrollerforglass.data.AccessPointListAdapter;
import com.ninjasnailstudios.huecontrollerforglass.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHHueError;

public class Home extends Activity implements OnItemClickListener{
	
    private final DialogInterface.OnClickListener mOnClickListener =
            new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        // Open WiFi Settings
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
            };
	
    private PHHueSDK phHueSDK;
    public static final String TAG = "QuickStart";
    private HueSharedPreferences prefs;
    private AccessPointListAdapter adapter;
    private AudioManager mAudioManager;
    private boolean lastSearchWasIPScan = false;
    private PHBridgeConfiguration bridgeConfig;
    private PHBridge bridge;
    private List<CardBuilder> mCards;
    private CardScrollView mCardScrollView;
    //private CardScrollAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bridgelistlinear);
        //Keep Glass Awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);     
        phHueSDK = PHHueSDK.create();
        phHueSDK.setDeviceName("HueControllerForGlass");
        phHueSDK.getNotificationManager().registerSDKListener(listener);
        adapter = new AccessPointListAdapter(getApplicationContext(), phHueSDK.getAccessPointsFound());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        //createCards();

        //mCardScrollView accessPointList = new CardScrollView(this);
        //adapter = new CardScrollAdapter(getApplicationContext(), phHueSDK.getAccessPointsFound());
        //mCardScrollView.setAdapter(adapter);
        //mCardScrollView.activate();
        //setContentView(mCardScrollView);
        
        ListView accessPointList = (ListView) findViewById(R.id.bridge_list);
        accessPointList.setOnItemClickListener(this);
        accessPointList.setAdapter(adapter);
        
        // Try to automatically connect to the last known bridge.  For first time use this will be empty so a bridge search is automatically started.
        prefs = HueSharedPreferences.getInstance(getApplicationContext());
        String lastIpAddress   = prefs.getLastConnectedIPAddress();
        String lastUsername    = prefs.getUsername();

        // Automatically try to connect to the last connected IP Address.  For multiple bridge support a different implementation is required.
        if (lastIpAddress !=null && !lastIpAddress.equals("")) {
            PHAccessPoint lastAccessPoint = new PHAccessPoint();
            lastAccessPoint.setIpAddress(lastIpAddress);
            lastAccessPoint.setUsername(lastUsername);
           
            if (1+1==2) {
            	setContentView(R.layout.activity_connecting);
        
            	//PHWizardAlertDialog.getInstance().showProgressDialog(R.string.connecting, Home.this);
                phHueSDK.connect(lastAccessPoint);
             }
        }
        else {
            doBridgeSearch();
        }
    }

//Start of Alert
    public class AlertDialog extends Dialog {

        private final DialogInterface.OnClickListener mOnClickListener;
        private final AudioManager mAudioManager;
        private final GestureDetector mGestureDetector;

        /**
         * Handles the tap gesture to call the dialog's
         * onClickListener if one is provided.
         */
        private final GestureDetector.BaseListener mBaseListener =
            new GestureDetector.BaseListener() {

            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    mAudioManager.playSoundEffect(Sounds.TAP);
                    if (mOnClickListener != null) {
                        // Since Glass dialogs do not have buttons,
                        // the index passed to onClick is always 0.
                        mOnClickListener.onClick(AlertDialog.this, 0);
                    }
                    return true;
                }
                return false;
            }
        };

        public AlertDialog(Context context, int iconResId,
                           int textResId, int footnoteResId,
                           DialogInterface.OnClickListener onClickListener) {
            super(context);

            mOnClickListener = onClickListener;
            mAudioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mGestureDetector =
                new GestureDetector(context).setBaseListener(mBaseListener);

            setContentView(new CardBuilder(context, CardBuilder.Layout.ALERT)
                    .setIcon(iconResId)
                    .setText(textResId)
                    .setFootnote(footnoteResId)
                    .getView());
        }

        /** Overridden to let the gesture detector handle a possible tap event. */
        @Override
        public boolean onGenericMotionEvent(MotionEvent event) {
            return mGestureDetector.onMotionEvent(event)
                || super.onGenericMotionEvent(event);
        }
    }
//End of Error
  
	    private PHSDKListener listener = new PHSDKListener() {

	        @Override
	        public void onAccessPointsFound(List<PHAccessPoint> accessPoint) {
	            Log.w(TAG, "Access Points Found. " + accessPoint.size());

                if (accessPoint != null && accessPoint.size() > 0) {
	                    phHueSDK.getAccessPointsFound().clear();
	                    phHueSDK.getAccessPointsFound().addAll(accessPoint);

	                    runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                            adapter.updateData(phHueSDK.getAccessPointsFound());
	                       }
	                   });

	            }
	            
	        }
	        
	        @Override
	        public void onCacheUpdated(int flags, PHBridge bridge) {
	            Log.w(TAG, "On CacheUpdated");

	        }

	        @Override
	        public void onBridgeConnected(PHBridge b) {
	            phHueSDK.setSelectedBridge(b);
	            phHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
	            phHueSDK.getLastHeartbeat().put(b.getResourceCache().getBridgeConfiguration() .getIpAddress(), System.currentTimeMillis());
	            prefs.setLastConnectedIPAddress(b.getResourceCache().getBridgeConfiguration().getIpAddress());
	            prefs.setUsername(prefs.getUsername());
	            PHWizardAlertDialog.getInstance().closeProgressDialog();  
	            startMainActivity();
	        }

	        @Override
	        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
	            Log.w(TAG, "Authentication Required.");
	            phHueSDK.startPushlinkAuthentication(accessPoint);
	            startActivity(new Intent(Home.this, Pushlink.class));
	           
	        }

	        @Override
	        public void onConnectionResumed(PHBridge bridge) {
	            if (Home.this.isFinishing())
	                return;
	            
	            Log.v(TAG, "onConnectionResumed" + bridge.getResourceCache().getBridgeConfiguration().getIpAddress());
	            phHueSDK.getLastHeartbeat().put(bridge.getResourceCache().getBridgeConfiguration().getIpAddress(),  System.currentTimeMillis());
	            for (int i = 0; i < phHueSDK.getDisconnectedAccessPoint().size(); i++) {

	                if (phHueSDK.getDisconnectedAccessPoint().get(i).getIpAddress().equals(bridge.getResourceCache().getBridgeConfiguration().getIpAddress())) {
	                    phHueSDK.getDisconnectedAccessPoint().remove(i);
	                }
	            }

	        }

	        @Override
	        public void onConnectionLost(PHAccessPoint accessPoint) {
	            Log.v(TAG, "onConnectionLost : " + accessPoint.getIpAddress());
	            if (!phHueSDK.getDisconnectedAccessPoint().contains(accessPoint)) {
	                phHueSDK.getDisconnectedAccessPoint().add(accessPoint);
	            }
	        }
	        
	        @Override
	        public void onError(int code, final String message) {
	            Log.e(TAG, "on Error Called : " + code + ":" + message);

	            if (code == PHHueError.NO_CONNECTION) {
	                Log.w(TAG, "On No Connection");
	            } 
	            else if (code == PHHueError.AUTHENTICATION_FAILED || code==1158) {  
	                PHWizardAlertDialog.getInstance().closeProgressDialog();
	            } 
	            else if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
	                Log.w(TAG, "Bridge Not Responding . . . ");
	                PHWizardAlertDialog.getInstance().closeProgressDialog();
	                Home.this.runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
                            //View view = new CardBuilder(getBaseContext(), CardBuilder.Layout.ALERT)
                                    //.setText("A stack indicator can be added to the corner of a card...")
                                    //.setIcon(R.drawable.ic_cloud_sad_150);
	                        //new AlertDialog(getBaseContext(), R.drawable.ic_cloud_sad_150, R.string.alert_text,
	                       //         R.string.alert_footnote_text, mOnClickListener).show();
	            	        Intent intent = new Intent(getApplicationContext(), Error.class);
	            	        startActivity(intent);
	            	        mAudioManager.playSoundEffect(Sounds.ERROR);
	            	        finish();
	                    }
	                }); 

	            } 
	            else if (code == PHMessageType.BRIDGE_NOT_FOUND) {

	                if (!lastSearchWasIPScan) {  // Perform an IP Scan (backup mechanism) if UPNP and Portal Search fails.
	                    phHueSDK = PHHueSDK.getInstance();
	                    PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
	                    sm.search(false, false, true);               
	                    lastSearchWasIPScan=true;
	                }
	                else {
	                    PHWizardAlertDialog.getInstance().closeProgressDialog();
	                    Home.this.runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
		            	        Intent intent = new Intent(getApplicationContext(), Error.class);
		            	        startActivity(intent);
		                        //new AlertDialog(getBaseContext(), R.drawable.ic_cloud_sad_150, R.string.alert_text,
		                                //R.string.alert_footnote_text, mOnClickListener).show();
		            	        mAudioManager.playSoundEffect(Sounds.ERROR);
		            	        finish();
	                        }
	                    });  
	                }
	                
	               
	            }
	        }
	    };

	    
	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	        if (listener !=null) {
	            phHueSDK.getNotificationManager().unregisterSDKListener(listener);
	        }
	        phHueSDK.disableAllHeartbeat();
	    }
	        
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	        HueSharedPreferences prefs = HueSharedPreferences.getInstance(getApplicationContext());
	        PHAccessPoint accessPoint = (PHAccessPoint) adapter.getItem(position);
	        accessPoint.setUsername(prefs.getUsername());

	        PHBridge connectedBridge = phHueSDK.getSelectedBridge();

	        if (connectedBridge != null) {
	            String connectedIP = connectedBridge.getResourceCache().getBridgeConfiguration().getIpAddress();
	            if (connectedIP != null) {   // We are already connected here:-
	                phHueSDK.disableHeartbeat(connectedBridge);
	                phHueSDK.disconnect(connectedBridge);
	            }
	        }
	        setContentView(R.layout.activity_connecting);

	        //PHWizardAlertDialog.getInstance().showProgressDialog(R.string.connecting, Home.this);
	        phHueSDK.connect(accessPoint);
	    }
	    
	    public void doBridgeSearch() {
	    	//setContentView(R.layout.activity_searching);
	        //PHWizardAlertDialog.getInstance().showProgressDialog(R.string.search_progress, Home.this);
	        PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
	        // Start the UPNP Searching of local bridges.
	        sm.search(true, true);
	    }
	    
	    public void startMainActivity() {   
	        Intent intent = new Intent(getApplicationContext(), OnActivity.class);
	        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
	            intent.addFlags(0x8000); // equal to Intent.FLAG_ACTIVITY_CLEAR_TASK which is only available from API level 11
	        startActivity(intent);
	        finish();
	    }
	    
}
