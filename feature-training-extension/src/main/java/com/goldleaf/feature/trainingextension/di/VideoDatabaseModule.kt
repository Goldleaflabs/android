package com.goldleaf.feature.trainingextension.di

import android.content.Context
import androidx.room.Room
import com.goldleaf.feature.trainingextension.data.local.VideoDao
import com.goldleaf.feature.trainingextension.data.local.VideoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VideoDatabaseModule {

    @Provides
    @Singleton
    fun provideVideoDatabase(
        @ApplicationContext context: Context
    ): VideoDatabase {
        return Room.databaseBuilder(
            context,
            VideoDatabase::class.java,
            VideoDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideVideoDao(database: VideoDatabase): VideoDao {
        return database.videoDao()
    }
}
