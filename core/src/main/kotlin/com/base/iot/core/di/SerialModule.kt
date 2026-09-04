package com.base.iot.core.di

import com.base.iot.core.iot.SerialManager
import com.base.iot.core.iot.SerialManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SerialModule {

    @Binds
    @Singleton
    abstract fun bindSerialManager(impl: SerialManagerImpl): SerialManager
}
