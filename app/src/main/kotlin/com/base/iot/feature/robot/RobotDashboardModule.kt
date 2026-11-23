package com.base.iot.feature.robot

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RobotDashboardModule {
    @Binds
    @Singleton
    abstract fun bindRobotDashboardRepository(
        repository: SampleRobotDashboardRepository
    ): RobotDashboardRepository
}
