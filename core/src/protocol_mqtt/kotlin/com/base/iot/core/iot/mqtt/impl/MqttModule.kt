package com.base.iot.core.iot.mqtt.impl

import com.base.iot.core.iot.MqttManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MqttBindingModule {
    @Binds
    @Singleton
    abstract fun bindMqttManager(impl: MqttManagerHiveMqImpl): MqttManager
}
