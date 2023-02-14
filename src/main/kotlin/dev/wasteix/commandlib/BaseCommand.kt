package dev.wasteix.commandlib

import dev.wasteix.commandlib.annotation.*
import dev.wasteix.commandlib.model.CommandEntity
import dev.wasteix.commandlib.model.DialogStateEntity
import dev.wasteix.commandlib.model.SubCommandEntity
import dev.wasteix.commandlib.model.data.CommandContentData
import dev.wasteix.commandlib.model.data.UserDialogStateData
import dev.wasteix.commandlib.model.sender.CommandSender
import java.util.*

abstract class BaseCommand(open vararg val commandNames: String) {
    var defaultMean: CommandEntity? = null
    var defaultDialogState: DialogStateEntity? = null

    val dialogStates: MutableMap<String, DialogStateEntity> = HashMap()
    val subCommands: MutableMap<String, SubCommandEntity> = HashMap()

    val userDialogStates: MutableMap<Int, UserDialogStateData> = HashMap()

    val commandContent: CommandContentData = CommandContentData(this)

    var exceptionHandler: ((CommandSender, Exception) -> Unit)? = null
    var dialogTimeout: DialogTimeout? = null

    val messageIds: MutableMap<Int, UUID> = HashMap()

    init {
        javaClass.apply {
            dialogTimeout = if (isAnnotationPresent(DialogTimeout::class.java)) getAnnotation(DialogTimeout::class.java) else null

            for (method in declaredMethods) {
                method.isAccessible = true

                if (method.isAnnotationPresent(Default::class.java))
                    defaultMean = CommandEntity(
                        method,
                        CommandContentData(method)
                    )

                if (method.isAnnotationPresent(DefaultDialogState::class.java))
                    defaultDialogState = DialogStateEntity(
                        method,
                        "default",
                        method.getAnnotation(DefaultDialogState::class.java).nextState,
                        commandContent
                    )

                if (method.isAnnotationPresent(ExceptionHandler::class.java))
                    exceptionHandler = method.invoke(this@BaseCommand) as (CommandSender, Exception) -> Unit

                val commandContent = CommandContentData(method)

                if (method.isAnnotationPresent(DialogState::class.java)) {
                    val dialogState = method.getAnnotation(DialogState::class.java)

                    dialogStates[dialogState.state] = DialogStateEntity(
                        method,
                        dialogState.state,
                        dialogState.nextState,
                        commandContent
                    )
                }

                if (method.isAnnotationPresent(SubCommand::class.java)) {
                    val subCommand = method.getAnnotation(SubCommand::class.java)

                    for (commandName in subCommand.commandNames)
                        subCommands[commandName] = SubCommandEntity(
                            method,
                            subCommand.commandNames,
                            commandContent
                        )
                }
            }

            if (dialogStates.isNotEmpty() && dialogTimeout == null)
                throw RuntimeException("В классе $name не найдена аннотация DialogTimeout")
        }
    }

    fun setState(commandSender: CommandSender, state: String, value: Any? = null) {
        userDialogStates[commandSender.message.fromId] = UserDialogStateData(
                commandSender, state, value,
                System.currentTimeMillis() + dialogTimeout!!.unit.toMillis(dialogTimeout!!.value)
            )
    }

    fun setStateDefault(commandSender: CommandSender, value: Any? = null) = setState(commandSender, "default", value)

    fun <T> getStateValue(commandSender: CommandSender): T {
        val fromId = commandSender.message.fromId
        val dialogStateEntity = userDialogStates[fromId]

        return dialogStateEntity?.value as T
    }

    fun removeState(fromId: Int) {
        userDialogStates.remove(fromId)
        messageIds.remove(fromId)
    }

    fun removeState(commandSender: CommandSender) = removeState(commandSender.message.fromId)
}