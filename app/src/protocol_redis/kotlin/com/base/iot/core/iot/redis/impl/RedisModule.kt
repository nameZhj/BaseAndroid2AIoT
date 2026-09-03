package com.base.iot.core.iot.redis.impl

import com.base.iot.core.iot.RedisManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RedisBindingModule {
    @Binds
    @Singleton
    abstract fun bindRedisManager(impl: RedisManagerJedisImpl): RedisManager
}
