package dev.wasteix.commandlib.handler

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.vk.api.sdk.objects.messages.Message
import dev.wasteix.commandlib.model.sender.GSON
import dev.wasteix.commandlib.service.CommandService
import java.util.*

class MessageHandler(private val commandService: CommandService, private val commandPrefixes: Array<out Char>) {
    fun handleMessage(message: Message) {
        val commandArgs = message.text.split(" ").toTypedArray()

        val payload = if (message.replyMessage != null) {
            fromJson(message.replyMessage.payload)
        } else if (message.fwdMessages.isNotEmpty()) {
            fromJson(message.fwdMessages[0].payload)
        } else null

        val fromId = message.fromId

        if (payload != null && payload.has("user_id") && payload.get("user_id").asInt != fromId) return

        val command = commandService.getCommand(
            if (payload == null) {
                if (commandPrefixes.any { commandArgs[0].startsWith(it) }) {
                    commandArgs[0].substring(1)
                } else null
            } else {
                payload.get("command").asString
            }) ?: return

        if (payload != null && !command.messageIds.containsValue(UUID.fromString(payload.get("unique_id")?.asString))) return

        val hasDialogState = command.userDialogStates.contains(fromId)

        val modifyArgs = commandArgs.copyOfRange(1, commandArgs.size)
        val args = run {
            var args = modifyArgs

            if (args.isEmpty() || (payload != null && payload.has("args"))) {
                if (message.replyMessage != null) {
                    val replyMessage = message.replyMessage

                    args = if (hasDialogState) argsFromPayload(replyMessage.payload) else args
                }

                if (message.fwdMessages.isNotEmpty()) {
                    val fwdMessage = message.fwdMessages[0]

                     args = if (hasDialogState) argsFromPayload(fwdMessage.payload) else args
                }
            }

            if (hasDialogState) commandArgs else args
        }

        val defaultMean = command.defaultMean

        if (command.dialogStates.isNotEmpty() && modifyArgs.isEmpty()) {
            val userDialogState = command.userDialogStates[fromId]

            if (userDialogState != null) {
                val dialogState = command.dialogStates[userDialogState.state]!!

                dialogState.execute(command, message, args, commandService)
            } else {
                command.defaultDialogState?.execute(command, message, args, commandService)
            }

            return
        }

        val subCommand = command.subCommands[modifyArgs[0]]

        if (subCommand == null) {
            defaultMean?.execute(command, message, modifyArgs, commandService)

            if (hasDialogState) command.removeState(fromId)

            return
        }

        subCommand.execute(command, message, modifyArgs.copyOfRange(1, args.size), commandService)
    }
}

fun <T> fromJson(json: JsonElement, clazz: Class<T>): T = GSON.fromJson(json, clazz)

fun <T> fromJson(json: String, clazz: Class<T>): T = GSON.fromJson(json, clazz)

fun fromJson(json: String) = fromJson(json, JsonObject::class.java)

fun argsFromPayload(json: String) = fromJson(fromJson(json, JsonObject::class.java)
    .get("args")
    .asJsonArray, Array<String>::class.java
)