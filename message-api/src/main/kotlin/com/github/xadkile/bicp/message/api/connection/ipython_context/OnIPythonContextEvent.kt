package com.github.xadkile.bicp.message.api.connection.ipython_context

fun interface OnIPythonContextEvent {
    fun run(context: KernelContext)
    companion object {
        val Nothing = OnIPythonContextEvent {
            // do nothing
        }
    }
}
