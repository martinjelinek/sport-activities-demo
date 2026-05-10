package io.github.martinjelinek.sportactivitiesdemo.data.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.martinjelinek.sportactivitiesdemo.data.local.SportActivityDao
import io.github.martinjelinek.sportactivitiesdemo.data.local.SportActivityDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): SportActivityDatabase =
        Room.databaseBuilder(ctx, SportActivityDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideDao(db: SportActivityDatabase): SportActivityDao = db.sportActivityDao()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    private const val DATABASE_NAME = "sport_activities.db"
}
