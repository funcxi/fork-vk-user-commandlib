package dev.wasteix.commandlib.utility

import com.vk.api.sdk.objects.messages.Message
import dev.wasteix.commandlib.BaseCommand
import dev.wasteix.commandlib.model.BaseCommandEntity
import dev.wasteix.commandlib.model.data.CommandContentData
import dev.wasteix.commandlib.model.sender.CommandSender
import dev.wasteix.commandlib.service.CommandService
import java.lang.reflect.Method
import java.util.*

fun Method.execute(vararg parameters: Any) {
    val command = parameters[0] as BaseCommand
    val message = parameters[1] as Message
    val args = parameters[2] as Array<String>
    val commandService = parameters[3] as CommandService
    val commandEntity = parameters[4] as BaseCommandEntity
 
    val commandSender = commandService.commandSender.getConstructor(
        Message::class.java,
        Array<String>::class.java,
        CommandService::class.java,
        BaseCommand::class.java,
        BaseCommandEntity::class.java
    ).newInstance(message, args, commandService, command, commandEntity)
 
    try {
        if (!isValidCheck(command.commandContent, commandSender, args)
            || !isValidCheck(commandEntity.commandContent, commandSender, args)) return
 
        for (commandHandler in commandSender.commandService.commandHandlers)
            commandHandler.invoke(commandSender, command, args)
 
        invoke(command, *normalizeParameters(this, message, args, commandService, command, commandEntity))
    } catch (exception: Exception) {
        exception.printStackTrace()
 
        command.exceptionHandler?.invoke(commandSender, exception)
    }
}
 
private fun isValidCheck(commandContent: CommandContentData, commandSender: CommandSender, args: Array<String>): Boolean {
    commandContent.minArg?.apply {
        if (value < args.size) {
            commandSender.sendMessage(messageNoArgs)
 
            return false
        }
    }
 
    commandContent.cooldown?.apply {
        val fromId = commandSender.message.fromId
 
        if (hasCooldown(fromId, key)) {
            commandSender.sendMessage(messageHasCooldown.replace(
                "<cooldown>",
                getCooldown(commandSender.message.fromId, key, unit).toString(),
                true)
            )
 
            return false
        }
 
        addCooldown(fromId, key, value, unit)
    }
 
    return true
}
 
private fun normalizeParameters(
    method: Method,
    message: Message,
    args: Array<String>,
    commandService: CommandService,
    command: BaseCommand,
    commandEntity: BaseCommandEntity
): Array<Any> {
    val parameters: MutableList<Any> = LinkedList()
 
    for (parameter in method.parameters) {
        if (CommandSender::class.java.isAssignableFrom(parameter.type)) {
            parameters.add(parameter.type.getConstructor(
                Message::class.java,
                Array<String>::class.java,
                CommandService::class.java,
                BaseCommand::class.java,
                BaseCommandEntity::class.java
            ).newInstance(message, args, commandService, command, commandEntity))
 
            continue
        }
 
        when (parameter.type) {
            Message::class.java -> parameters.add(message)
            Array<String>::class.java -> parameters.add(args)
            else -> throw RuntimeException("В методе ${method.name} найден неизвестный параметр: ${parameter.type}")
        }
    }
 
    return parameters.toTypedArray()
}
