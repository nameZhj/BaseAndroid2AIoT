package com.base.iot.core.network.impl

import com.base.iot.core.network.HttpManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HttpStubBindingModule {
    @Binds
    @Singleton
    abstract fun bindHttpManager(stub: HttpManagerStubImpl): HttpManager
}
