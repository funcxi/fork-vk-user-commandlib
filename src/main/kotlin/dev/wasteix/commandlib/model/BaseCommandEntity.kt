package dev.wasteix.commandlib.model

import dev.wasteix.commandlib.annotation.DefaultDialogState
import dev.wasteix.commandlib.annotation.DialogState
import dev.wasteix.commandlib.model.data.CommandContentData
import dev.wasteix.commandlib.utility.execute
import java.lang.reflect.Method

sealed class BaseCommandEntity(
    open val method: Method,
    open val commandContent: CommandContentData
) {
    fun execute(vararg parameters: Any) = method.execute(*(mutableListOf(*parameters, this).toTypedArray()))

    fun isDialogState() = method.isAnnotationPresent(DialogState::class.java)
            || method.isAnnotationPresent(DefaultDialogState::class.java)
}

data class CommandEntity(
    override val method: Method,
    override val commandContent: CommandContentData
) : BaseCommandEntity(method, commandContent)

data class DialogStateEntity(
    override val method: Method,
    val state: String,
    val nextState: String,
    override val commandContent: CommandContentData
) : BaseCommandEntity(method, commandContent)

data class SubCommandEntity(
    override val method: Method,
    val commandNames: Array<out String>,
    override val commandContent: CommandContentData
) : BaseCommandEntity(method, commandContent)