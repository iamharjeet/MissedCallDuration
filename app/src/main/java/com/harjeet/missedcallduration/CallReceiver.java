package com.harjeet.missedcallduration;

/**
 * Created by HARJEET on 05-May-17.
 */

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.harjeet.missedcallduration.MainActivity.MISSED_CALL;
import static com.harjeet.missedcallduration.MainActivity.PREFS_NAME;


public class CallReceiver extends PhonecallReceiver {

    private MissedCallDataSource datasource;

    public static final String ID = "id";
    public static final String NUMBER = "number";
    public static final String START = "start";
    public static final String DURATION = "duration";
    public static final int NOTIFICATION_ID = 5;
    public static final String NOTIFICATION_CHANNEL_ID = "channel_id";

    @Override
    protected void onMissedCall(Context ctx, String number, Date start, Date end) {
        datasource = new MissedCallDataSource(ctx);
        datasource.open();

        String timeOnly = new SimpleDateFormat("hh:mm a").format(start);

        Format dateFormat = android.text.format.DateFormat.getDateFormat(ctx);
        String pattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern(); // pattern to get local date format
        String dateOnly = new SimpleDateFormat(pattern).format(start);
        String startTime = dateOnly + "=" + timeOnly;   //    Here = is a date time separator
        double diffInMs = end.getTime() - start.getTime();
        double duration = diffInMs / 1000;
        BigDecimal bd = new BigDecimal(duration);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        duration = bd.doubleValue();

        long id;
        id = datasource.insertData(number, startTime, String.valueOf(duration));

        Intent intent = new Intent(MISSED_CALL);
        intent.putExtra(ID, id);
        intent.putExtra(NUMBER, number);
        intent.putExtra(START, startTime);
        intent.putExtra(DURATION, String.valueOf(duration));
        ctx.sendBroadcast(intent);

        LogsFragment.newListData.add(new MissedData(id, number, startTime, duration));
        LogsFragment.databaseChanged = true;

        datasource.close();

        SharedPreferences sharedPreferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isAppActive = sharedPreferences.getBoolean("active", false);
        boolean notificationRequired = sharedPreferences.getBoolean("KEY_NOTIFICATION", false);

        if (!isAppActive) {
            int count = sharedPreferences.getInt("notificationCount", 0);
            count++;

            if (notificationRequired) {
                sendStatusBarNotification(ctx, count);
            }
            setBadge(ctx, count);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("notificationCount", count);
            editor.apply();

        }
    }

    private void sendStatusBarNotification(Context context, int count) {

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        String text;
        if (count == 1) {
            text = "1 New Missed Call";
        } else {
            text = count + " New Missed Calls";
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                R.mipmap.ic_launcher))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Missed Calls")
                        .setContentText(text)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setContentIntent(contentIntent);
//        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "missed"; // context.getResources().getString(R.string.channel_name);
            String description = "missed call duration"; // context.getResources().getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            mNotificationManager.createNotificationChannel(channel);
        }


        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    public static void setBadge(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        //
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end)
    {
        //
    }

}
