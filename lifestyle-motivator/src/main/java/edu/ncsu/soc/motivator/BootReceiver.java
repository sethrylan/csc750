package edu.ncsu.soc.motivator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {  
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // TODO: Register your reporting alarms here.            
        }
    }
}
