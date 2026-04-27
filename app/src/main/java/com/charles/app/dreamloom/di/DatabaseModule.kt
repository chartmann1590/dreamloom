package com.charles.app.dreamloom.di

import android.content.Context
import androidx.room.Room
import com.charles.app.dreamloom.data.db.AppDatabase
import com.charles.app.dreamloom.data.db.DreamDao
import com.charles.app.dreamloom.data.db.InsightDao
import com.charles.app.dreamloom.data.db.OracleDao
import com.charles.app.dreamloom.core.crypto.PassphraseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        pass: PassphraseProvider,
    ): AppDatabase {
        val factory = SupportFactory(pass.getOrCreateDbPassphrase())
        return Room.databaseBuilder(context, AppDatabase::class.java, "dreamloom.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDreamDao(db: AppDatabase): DreamDao = db.dreamDao()

    @Provides
    fun provideInsightDao(db: AppDatabase): InsightDao = db.insightDao()

    @Provides
    fun provideOracleDao(db: AppDatabase): OracleDao = db.oracleDao()
}
