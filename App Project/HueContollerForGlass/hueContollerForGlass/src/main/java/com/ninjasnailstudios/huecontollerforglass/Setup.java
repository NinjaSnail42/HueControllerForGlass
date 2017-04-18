package com.ninjasnailstudios.huecontollerforglass;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;


public class Setup extends Activity implements OnClickListener {

    private final Handler mHandler = new Handler();

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setupmenu, menu);
        return true;
    }
    
    
    private void post(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the Activity.
        finish();
    }

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
}