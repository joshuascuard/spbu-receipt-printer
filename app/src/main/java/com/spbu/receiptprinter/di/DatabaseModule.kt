package com.spbu.receiptprinter.di

import android.content.Context
import androidx.room.Room
import com.spbu.receiptprinter.data.dao.ProdukBbmDao
import com.spbu.receiptprinter.data.dao.SettingDao
import com.spbu.receiptprinter.data.dao.TransaksiDao
import com.spbu.receiptprinter.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module untuk dependency injection database.
 * Menyediakan instance database dan DAO.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Menyediakan instance Room Database.
     * Singleton - hanya satu instance di seluruh aplikasi.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Untuk development, ganti dengan migration di production
            .build()
    }

    @Provides
    @Singleton
    fun provideTransaksiDao(db: AppDatabase): TransaksiDao = db.transaksiDao()

    @Provides
    @Singleton
    fun provideProdukBbmDao(db: AppDatabase): ProdukBbmDao = db.produkBbmDao()

    @Provides
    @Singleton
    fun provideSettingDao(db: AppDatabase): SettingDao = db.settingDao()
}
