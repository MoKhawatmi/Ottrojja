package com.ottrojja.screens.prayerTimesScreen.notifications

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager

/**
 * Invisible target of a tap on the adhan notification's body: stops the adhan, and opens the app
 * only when the device is already unlocked.
 *
 * It is an activity (not a service or receiver) because Android 12+ blocks activity starts from
 * notification trampolines, so only an activity can still open the app. It shows over the lock
 * screen, so on a locked phone the tap stops the adhan without asking to unlock first.
 */
class PrayerAdhanStopActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }

        startService(PrayerAdhanService.stopIntent(this))

        val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguard.isKeyguardLocked) {
            packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
                launch.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(launch)
            }
        }
        // Theme.NoDisplay requires finishing before onResume.
        finish()
    }
}
