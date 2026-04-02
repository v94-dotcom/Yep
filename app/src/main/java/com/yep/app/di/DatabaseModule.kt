package com.yep.app.di

import android.content.Context
import androidx.room.Room
import com.yep.app.data.YepDao
import com.yep.app.data.YepDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): YepDatabase =
        Room.databaseBuilder(context, YepDatabase::class.java, "yep.db").build()

    @Provides
    fun provideYepDao(db: YepDatabase): YepDao = db.yepDao()
}
