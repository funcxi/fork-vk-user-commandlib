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
            && !isValidCheck(commandEntity.commandContent, commandSender, args)) return

        for (commandHandler in commandSender.commandService.commandHandlers)
            commandHandler.invoke(commandSender, command, args)

        invoke(command, *normalizeParameters(this, commandSender, message, args))
    } catch (exception: Exception) {
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

private inline fun <reified T : CommandSender> normalizeParameters(
    method: Method,
    commandSender: T,
    message: Message,
    args: Array<String>
): Array<Any> {
    val parameters: MutableList<Any> = LinkedList()

    for (parameter in method.parameters) {
        when (parameter.type) {
            T::class.java -> parameters.add(commandSender)
            Message::class.java -> parameters.add(message)
            Array<String>::class.java -> parameters.add(args)
            else -> throw RuntimeException("В методе ${method.name} найден неизвестный параметр: ${parameter.type}")
        }
    }

    return parameters.toTypedArray()
}