package com.yep.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.lifecycleScope
import com.yep.app.ui.navigation.YepNavigation
import com.yep.app.ui.theme.YepTheme
import com.yep.app.util.DateUtils
import com.yep.app.util.NotificationHelper
import com.yep.app.worker.DailyResetEntryPoint
import com.yep.app.worker.DailyResetWorker
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YepTheme(darkTheme = isSystemInDarkTheme()) {
                YepNavigation()
            }
        }
        triggerDailyResetIfNeeded()
    }

    private fun triggerDailyResetIfNeeded() {
        val prefs = getSharedPreferences("yep_prefs", Context.MODE_PRIVATE)
        val today = DateUtils.today()
        NotificationHelper.createChannel(applicationContext)
        lifecycleScope.launch {
            val repo = EntryPointAccessors.fromApplication(
                application,
                DailyResetEntryPoint::class.java
            ).repository()
            if (prefs.getString("last_reset_date", null) != today) {
                DailyResetWorker.doReset(repo, applicationContext)
                prefs.edit().putString("last_reset_date", today).apply()
            }
            DailyResetWorker.schedule(applicationContext)
            val settings = repo.getSettings().first()
            if (settings?.reminderEnabled == true) {
                NotificationHelper.scheduleReminder(applicationContext, settings.reminderTime)
            }
        }
    }
}
