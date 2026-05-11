package io.github.martinjelinek.sportactivitiesdemo.data.di

import android.content.Context
import com.google.firebase.FirebaseApp
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.martinjelinek.sportactivitiesdemo.data.remote.FakeRemoteDataSource
import io.github.martinjelinek.sportactivitiesdemo.data.remote.FirestoreRemoteDataSource
import io.github.martinjelinek.sportactivitiesdemo.data.remote.RemoteDataSource
import io.github.martinjelinek.sportactivitiesdemo.data.repository.SportActivityRepositoryImpl
import io.github.martinjelinek.sportactivitiesdemo.domain.repository.SportActivityRepository
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRepository(impl: SportActivityRepositoryImpl): SportActivityRepository

    companion object {
        @Provides
        @Singleton
        fun provideRemoteDataSource(
            @ApplicationContext context: Context,
            firestore: Provider<FirestoreRemoteDataSource>,
            fake: Provider<FakeRemoteDataSource>,
        ): RemoteDataSource =
            if (FirebaseApp.getApps(context).isNotEmpty()) firestore.get() else fake.get()
    }
}
