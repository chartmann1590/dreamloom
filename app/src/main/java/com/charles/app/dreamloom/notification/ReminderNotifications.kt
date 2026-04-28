package com.charles.app.dreamloom.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.charles.app.dreamloom.MainActivity
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.navigation.Routes

object ReminderNotifications {
    const val CHANNEL_ID: String = "reminder_morning"
    private const val NOTIF_ID: Int = 0x4D4C /* ML */

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val ch = NotificationChannel(
            CHANNEL_ID,
            ctx.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = ctx.getString(R.string.reminder_channel_description)
        }
        (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(ch)
    }

    fun showReminder(ctx: Context, body: String) {
        ensureChannel(ctx)
        val open = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_ROUTE, Routes.HOME)
        }
        val pi = PendingIntent.getActivity(
            ctx,
            NOTIF_ID,
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_moon)
            .setContentTitle(ctx.getString(R.string.app_name))
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        NotificationManagerCompat.from(ctx).notify(NOTIF_ID, notif)
    }
}
