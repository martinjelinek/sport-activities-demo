package io.github.martinjelinek.sportactivitiesdemo.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.martinjelinek.sportactivitiesdemo.data.location.AndroidLocationProvider
import io.github.martinjelinek.sportactivitiesdemo.domain.location.LocationProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationProvider(impl: AndroidLocationProvider): LocationProvider
}
