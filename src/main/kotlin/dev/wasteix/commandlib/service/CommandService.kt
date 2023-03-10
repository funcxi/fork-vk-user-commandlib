package dev.wasteix.commandlib.service

import com.vk.api.sdk.actions.Messages
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import dev.wasteix.commandlib.BaseCommand
import dev.wasteix.commandlib.model.sender.CommandSender
import dev.wasteix.commandlib.handler.MessageHandler
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CommandService(
    id: Int,
    token: String,
    commandPrefixes: Array<out Char>,
    val commandSender: Class<out CommandSender>,
    val commandHandlers: List<(CommandSender, BaseCommand, Array<String>) -> Unit>,
    val exceptionHandler: ((CommandSender, Exception) -> Unit)?
) {
    private val commands: MutableMap<String, BaseCommand> = HashMap()
    private val messageHandler = MessageHandler(this, commandPrefixes)
    private val userActor: UserActor = UserActor(id, token)

    private val vk = VkApiClient(HttpTransportClient.getInstance())
    val vkMessages: Messages = vk.messages()

    init {
        Executors.newSingleThreadScheduledExecutor().apply {
            val longPollServer = vkMessages.getLongPollServer(userActor)
            var ts = longPollServer.execute().ts
            var maxMsgId = -1

            scheduleAtFixedRate({
                val eventQuery = vkMessages
                    .getLongPollHistory(userActor)
                    .ts(ts)

                if (maxMsgId > 0)
                    eventQuery.maxMsgId(maxMsgId)

                val message = eventQuery.execute()
                    .messages.items

                if (!message.isEmpty()) {
                    ts = longPollServer.execute().ts

                    if (!message[0].isOut) {
                        val messageId = message[0].id

                        if (messageId > maxMsgId)
                            maxMsgId = messageId
                    }
                    messageHandler.handleMessage(message[0])
                }
                
            }, 100, 120, TimeUnit.MILLISECONDS)

            scheduleAtFixedRate({
                for (command in commands.values) {
                    val dialogTimeout = command.dialogTimeout ?: continue

                    for (dialogState in command.userDialogStates.values) {
                        if (System.currentTimeMillis() < dialogState.timeout) continue

                        val commandSender = dialogState.commandSender

                        command.removeState(dialogState.commandSender)
                        if (dialogTimeout.messageTimeExpired.isNotEmpty())
                            commandSender.sendReplyMessage(dialogTimeout.messageTimeExpired)
                    }
                }
            }, 1, 1, TimeUnit.SECONDS)
        }
    }

    fun send() = vkMessages.send(userActor)

    fun registerCommands(vararg commands: BaseCommand) {
        for (command in commands) {
            if (exceptionHandler != null) command.exceptionHandler = exceptionHandler

            for (commandName in command.commandNames) this.commands[commandName] = command
        }
    }

    fun unregisterCommands(vararg commandNames: String) {
        for (commandName in commandNames) commands.remove(commandName)
    }

    fun unregisterCommands(vararg commands: BaseCommand) {
        for (command in commands)
            this.commands.remove(this.commands.entries
                .stream()
                .filter { it.value == command }
                .map { it.key }
                .findFirst()
                .orElse(null)
            )
    }

    fun getCommand(commandName: String?): BaseCommand? {
        for (command in commands.values) {
            if (command.commandNames.contains(commandName)) return command
        }

        return null
    }
}
