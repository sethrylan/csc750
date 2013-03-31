package edu.ncsu.soc.motivator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class MotivatorService extends Service {
	
	public static int SERVICE_ID = 1234567;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startService();
		return(START_NOT_STICKY);
	}

	@Override
	public void onDestroy() {
		stopService();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return (null);
	}

	private void startService() {
		Notification note = new Notification(R.drawable.ic_launcher, "Starting Lifestyle Motivator Service", System.currentTimeMillis());
		Intent i = new Intent(this, MotivatorMapActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		note.setLatestEventInfo(this, "Lifestyle Motivator - srgainey", "Checking for opportunities...", pi);
		note.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(SERVICE_ID, note);
		
		
		while (true) {
			synchronized (this) {
				try {
					wait(1000 * 5);
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
					        .setSmallIcon(R.drawable.ic_launcher)
					        .setContentTitle("My notification")
					        .setContentText("Hello World!");
					// Creates an explicit intent for an Activity in your app
					Intent resultIntent = new Intent(this, MotivatorMapActivity.class);

					// The stack builder object will contain an artificial back stack for the
					// started Activity.
					// This ensures that navigating backward from the Activity leads out of
					// your application to the Home screen.
					TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
					// Adds the back stack for the Intent (but not the Intent itself)
					stackBuilder.addParentStack(MotivatorMapActivity.class);
					// Adds the Intent that starts the Activity to the top of the stack
					stackBuilder.addNextIntent(resultIntent);
					PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
					mBuilder.setContentIntent(resultPendingIntent);
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					// mId allows you to update the notification later on.
					
					int mId = 1234567890;
					mNotificationManager.notify(mId, mBuilder.build());
					
				} catch (Exception e) {
					
				}
			}
		}

	}

	private void stopService() {
		stopForeground(true);
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns,
	 * IntentService stops the service, as appropriate.
	 */
	/**
	protected void onHandleIntent(Intent intent) {
		// Normally we would do some work here, like download a file.
		// For our sample, we just sleep for 5 seconds.
		long endTime = System.currentTimeMillis() + 5 * 1000;
		while (System.currentTimeMillis() < endTime) {
			synchronized (this) {
				try {
					wait(endTime - System.currentTimeMillis());
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
					        .setSmallIcon(R.drawable.ic_launcher)
					        .setContentTitle("My notification")
					        .setContentText("Hello World!");
					// Creates an explicit intent for an Activity in your app
					Intent resultIntent = new Intent(this, MotivatorMapActivity.class);

					// The stack builder object will contain an artificial back stack for the
					// started Activity.
					// This ensures that navigating backward from the Activity leads out of
					// your application to the Home screen.
					TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
					// Adds the back stack for the Intent (but not the Intent itself)
					stackBuilder.addParentStack(MotivatorMapActivity.class);
					// Adds the Intent that starts the Activity to the top of the stack
					stackBuilder.addNextIntent(resultIntent);
					PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
					mBuilder.setContentIntent(resultPendingIntent);
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					// mId allows you to update the notification later on.
					
					int mId = 1234567;
					mNotificationManager.notify(mId, mBuilder.build());

					
				} catch (Exception e) {
					
				}
			}
		}
	} */

}
