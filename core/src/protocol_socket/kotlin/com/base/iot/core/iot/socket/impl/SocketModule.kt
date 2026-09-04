package com.base.iot.core.iot.socket.impl

import com.base.iot.core.iot.SocketManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SocketBindingModule {
    @Binds
    @Singleton
    abstract fun bindSocketManager(impl: SocketManagerImpl): SocketManager
}
