package com.base.iot.core.iot.mqtt.impl

import com.base.iot.core.iot.MqttManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MqttStubBindingModule {
    @Binds
    @Singleton
    abstract fun bindMqttManager(stub: MqttManagerStubImpl): MqttManager
}
