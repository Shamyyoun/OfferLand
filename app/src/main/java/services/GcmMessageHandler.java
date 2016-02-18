package services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.offerland.app.OfferActivity;
import com.offerland.app.R;

import datamodels.Offer;
import receivers.GcmBroadcastReceiver;

public class GcmMessageHandler extends IntentService {
    private Handler handler;

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();

        // get notification key
        String key = extras.getString("key");

        // check notification key
        if ("new_offer".equals(key)) {
            // get values
            int id = Integer.parseInt(extras.getString("id"));
            String title = extras.getString("title");
            String desc = extras.getString("desc");
            int originalPrice = Integer.parseInt(extras.getString("originalPrice"));
            int newPrice = Integer.parseInt(extras.getString("newPrice"));
            String image = extras.getString("image");
            String date = extras.getString("date");
            String storeName = extras.getString("storeName");
            String storePhoto = extras.getString("storePhoto");
            double lat = Double.parseDouble(extras.getString("lat"));
            double lng = Double.parseDouble(extras.getString("lng"));

            Offer offer = new Offer(id);
            offer.setTitle(title);
            offer.setDesc(desc);
            offer.setOriginalPrice(originalPrice);
            offer.setNewPrice(newPrice);
            offer.setImage(image);
            offer.setDate(date);
            offer.setStorePhoto(storePhoto);
            offer.setStoreName(storeName);
            offer.setLat(lat);
            offer.setLng(lng);

            // show notification
            Intent notificationIntent = new Intent(getApplicationContext(), OfferActivity.class);
            showNotification(1, title, desc, notificationIntent);
        } else if ("accept_request".equals(key)) {
            String storeName = extras.getString("store_name");

            // show notification
            Intent notificationIntent = new Intent();
            showNotification(1, "OfferLand", storeName + " accepted your request", notificationIntent);
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void showNotification(final int id, final String title, final String desc, final Intent notificationIntent) {
        handler.post(new Runnable() {
            public void run() {
                NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().
                        getSystemService(Context.NOTIFICATION_SERVICE);

                int icon = R.drawable.ic_launcher;
                long when = System.currentTimeMillis();
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                        id, notificationIntent, 0);
                Notification notification = new Notification(icon, title, when);
                notification.sound = soundUri;
                notification.setLatestEventInfo(getApplicationContext(), title, desc, contentIntent);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                mNotificationManager.notify(id, notification);
            }
        });

    }
}
