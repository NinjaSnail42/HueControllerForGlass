package com.ninjasnailstudios.huecontollerforglass;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.ninjasnailstudios.huecontollerforglass.SliderView;

import com.google.android.glass.media.Sounds;
import com.ninjasnailstudios.huecontrollerforglass.data.AccessPointListAdapter;
import com.ninjasnailstudios.huecontrollerforglass.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;

public class Home extends Activity implements OnItemClickListener{
	
    private PHHueSDK phHueSDK;
    public static final String TAG = "QuickStart";
    private HueSharedPreferences prefs;
    private AccessPointListAdapter adapter;
    private AudioManager mAudioManager;
    private SliderView mSliderView;
	private SliderView mIndeterm;
    
    private boolean lastSearchWasIPScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bridgelistlinear);
        
        bridgeSearchButton();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        phHueSDK = PHHueSDK.create();
        phHueSDK.setDeviceName("HueControllerForGlass");
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        phHueSDK.getNotificationManager().registerSDKListener(listener);
        
        adapter = new AccessPointListAdapter(getApplicationContext(), phHueSDK.getAccessPointsFound());
        
	//	mIndeterm = (SliderView) findViewById(R.id.indeterminate_slider);
       
        
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
            	//setContentView(R.layout.slider);

        		//mIndeterm.startIndeterminate();
            	//PHWizardAlertDialog.getInstance().showProgressDialog(R.string.connecting, Home.this);
                phHueSDK.connect(lastAccessPoint);
             }
        }
        else {  // First time use, so perform a bridge search.
            doBridgeSearch();
        }
    }
  
	  // Local SDK Listener
	    private PHSDKListener listener = new PHSDKListener() {

	        @Override
	        public void onAccessPointsFound(List<PHAccessPoint> accessPoint) {
	            Log.w(TAG, "Access Points Found. " + accessPoint.size());

	            PHWizardAlertDialog.getInstance().closeProgressDialog();
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
		            	        finish();
	                        }
	                    });  
	                }
	                
	               
	            }
	        }
	    };

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        //case R.id.find_new_bridge:
	            //doBridgeSearch();
	            //break;
	        }
	        return true;
	    }

	    
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
	        PHWizardAlertDialog.getInstance().showProgressDialog(R.string.connecting, Home.this);
	        phHueSDK.connect(accessPoint);  
	    }
	    
	    
		private void bridgeSearchButton(){
			//Button bridgeSearch= (Button)findViewById(R.id.bridgeSearchButton);
			//bridgeSearch.setOnClickListener(new View.OnClickListener() {
				
				//@Override
				//public void onClick(View v) {
		            //doBridgeSearch();
		        //}
			//});
		}
	    
	    public void doBridgeSearch() {
	        PHWizardAlertDialog.getInstance().showProgressDialog(R.string.search_progress, Home.this);
	        PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
	        // Start the UPNP Searching of local bridges.
	        sm.search(true, true);
	    }
	    
	    // Starting the main activity this way, prevents the PushLink Activity being shown when pressing the back button.
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
