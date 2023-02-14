package dev.wasteix.commandlib.service

import dev.wasteix.commandlib.BaseCommand
import dev.wasteix.commandlib.model.sender.CommandSender
import java.util.*

class CommandServiceBuilder {
    private var id: Int = -1
    private lateinit var token: String

    private var commandPrefixes: Array<out Char> = arrayOf('!', '/')
    private val commandHandlers: LinkedList<(CommandSender, BaseCommand, Array<String>) -> Unit> = LinkedList()
    private var commandSender: Class<out CommandSender> = CommandSender::class.java
    private lateinit var exceptionHandler: (CommandSender, Exception) -> Unit

    fun id(id: Int): CommandServiceBuilder {
        this.id = id

        return this
    }

    fun token(token: String): CommandServiceBuilder {
        this.token = token

        return this
    }

    fun commandPrefixes(vararg commandPrefixes: Char): CommandServiceBuilder {
        this.commandPrefixes = commandPrefixes.toTypedArray()

        return this
    }

    fun <T : CommandSender> commandHandler(commandHandler: (T, BaseCommand, Array<String>) -> Unit): CommandServiceBuilder {
        commandHandlers.add(commandHandler as (CommandSender, BaseCommand, Array<String>) -> Unit)

        return this
    }

    fun commandSender(commandSender: Class<CommandSender>): CommandServiceBuilder {
        this.commandSender = commandSender

        return this
    }

    fun <T : CommandSender> exceptionHandler(
        exceptionHandler: (T, Exception) -> Unit
    ): CommandServiceBuilder {
        this.exceptionHandler = exceptionHandler as (CommandSender, Exception) -> Unit

        return this
    }

    fun build(): CommandService {
        return CommandService(id, token, commandPrefixes, commandSender, commandHandlers, exceptionHandler)
    }

    companion object {
        fun create() = CommandServiceBuilder()
    }
}