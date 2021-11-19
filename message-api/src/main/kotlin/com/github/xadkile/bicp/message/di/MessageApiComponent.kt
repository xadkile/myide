package com.github.xadkile.bicp.message.di

import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonConfig
import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonContext
import dagger.BindsInstance
import dagger.Component
import org.zeromq.ZContext
import javax.inject.Singleton


@Singleton
@Component(modules = [ContextModule::class, OtherModule::class])
interface MessageApiComponent {
    fun zContext(): ZContext
    fun ipythonContext(): IPythonContext

    @Component.Builder
    interface Builder{
        fun build():MessageApiComponent

        @BindsInstance
        fun ipythonConfig(config: IPythonConfig):Builder
    }
}
