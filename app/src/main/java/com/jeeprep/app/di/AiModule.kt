package com.jeeprep.app.di

import com.jeeprep.app.ai.AiEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// AiEngine uses @Inject constructor() + @Singleton, so Hilt provides it
// automatically. No @Provides needed. This module is kept for future AI bindings.
@Module
@InstallIn(SingletonComponent::class)
object AiModule
