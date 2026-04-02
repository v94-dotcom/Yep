package com.yep.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.yep.app.data.entities.StreakData
import com.yep.app.data.repository.YepRepository
import com.yep.app.util.DateUtils
import com.yep.app.util.PhotoManager
import android.content.Context.MODE_PRIVATE
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DailyResetEntryPoint {
    fun repository(): YepRepository
}

class DailyResetWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repo = EntryPointAccessors.fromApplication(appContext, DailyResetEntryPoint::class.java)
            .repository()
        doReset(repo, appContext)
        appContext.getSharedPreferences("yep_prefs", MODE_PRIVATE)
            .edit().putString("last_reset_date", DateUtils.today()).apply()
        return Result.success()
    }

    companion object {
        suspend fun doReset(repo: YepRepository, context: Context) {
            val yesterday = DateUtils.yesterday()
            val dayBeforeYesterday = DateUtils.daysAgo(2)

            val items = repo.getAllItems().first()
            val yesterdayConfs = repo.getConfirmationsForDate(yesterday).first()

            val confirmedIds = yesterdayConfs.map { it.itemId }.toSet()
            val wasComplete = items.isNotEmpty() && confirmedIds.size >= items.size

            val existing = repo.getStreak().first() ?: StreakData()
            val newCurrent = if (wasComplete) {
                if (existing.lastCompleteDate == dayBeforeYesterday) existing.currentStreak + 1
                else 1
            } else 0
            val newBest = maxOf(existing.bestStreak, newCurrent)

            repo.saveStreak(
                StreakData(
                    currentStreak = newCurrent,
                    bestStreak = newBest,
                    lastCompleteDate = if (wasComplete) yesterday else existing.lastCompleteDate
                )
            )

            repo.deleteConfirmationsBeforeDate(DateUtils.daysAgo(30))
            PhotoManager.cleanupOldPhotos(context)
        }

        fun schedule(context: Context) {
            val delay = DateUtils.millisUntilMidnight()
            val request = PeriodicWorkRequestBuilder<DailyResetWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_reset",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
