package com.sportlinktv.di

import android.content.Context
import androidx.room.Room
import com.sportlinktv.data.local.AppDatabase
import com.sportlinktv.data.local.dao.ChannelDao
import com.sportlinktv.data.local.dao.PlaylistDao
import com.sportlinktv.data.repository.ChannelRepositoryImpl
import com.sportlinktv.data.repository.PlaylistRepositoryImpl
import com.sportlinktv.domain.repository.ChannelRepository
import com.sportlinktv.domain.repository.PlaylistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "sportlink_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChannelDao(db: AppDatabase): ChannelDao = db.channelDao()

    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()

    @Provides
    @Singleton
    fun provideChannelRepository(dao: ChannelDao): ChannelRepository = ChannelRepositoryImpl(dao)

    @Provides
    @Singleton
    fun providePlaylistRepository(dao: PlaylistDao): PlaylistRepository = PlaylistRepositoryImpl(dao)
}
