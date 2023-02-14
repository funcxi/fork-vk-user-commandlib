package dev.wasteix.commandlib.model.sender

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vk.api.sdk.objects.messages.Forward
import com.vk.api.sdk.objects.messages.Message
import com.vk.api.sdk.queries.messages.MessagesSendQuery
import dev.wasteix.commandlib.BaseCommand
import dev.wasteix.commandlib.model.BaseCommandEntity
import dev.wasteix.commandlib.service.CommandService
import java.util.*
import kotlin.random.Random

val GSON = Gson()

open class CommandSender(
    open val message: Message,
    open val args: Array<String>,
    open val commandService: CommandService,
    open val command: BaseCommand,
    open val commandEntity: BaseCommandEntity
) {
    private val forward = (Forward())
        .setIsReply(true)
        .setPeerId(message.peerId)
        .setConversationMessageIds(listOf(message.conversationMessageId))

    /**
     * Отправляет сообщение
     *
     * @param message сообщение
     * @param objects объекты для форматирования сообщения
     */
    fun sendMessage(message: String, vararg objects: Any) {
        createSendMessageQuery(message, objects)
            .execute()
    }

    /**
     * Отправляет сообщение с ответом на сообщение написанное отправителем команды
     *
     * @param message сообщение
     * @param objects объекты для форматирования сообщения
     */
    fun sendReplyMessage(message: String, vararg objects: Any) {
        createSendMessageQuery(message, objects)
            .forward(forward)
            .execute()
    }

    /**
     * Отправка голосового сообщения
     *
     * @param message сообщение
     * @param objects объекты для форматирования сообщения
     */
    fun sendAudioMessage(message: String, vararg objects: Any) {
        createSendMessageQuery(message, objects)
            .attachment("audio")
            .execute()
    }

    /**
     * Отправка голосового сообщения с ответом на сообщение написанное отправителем команды
     *
     * @param message сообщение
     * @param objects объекты для форматирования сообщения
     */
    fun sendReplyAudioMessage(message: String, vararg objects: Any) {
        createSendMessageQuery(message, objects)
            .forward(forward)
            .attachment("audio")
            .execute()
    }

    /**
     * Создает запрос отправки сообщения
     *
     * @param message сообщение
     * @param objects объекты для форматирования сообщения
     */
    fun createSendMessageQuery(message: String, vararg objects: Any): MessagesSendQuery {
        val sendQuery = commandService.send()
            .peerId(this.message.peerId)
            .message(String.format(message, objects))
            .randomId(Random.nextInt(10000))

        if (command.dialogStates.isNotEmpty() && commandEntity.isDialogState()) {
            sendQuery.payload(kotlin.run {
                val jsonObject = JsonObject()
                val jsonArray = JsonArray().apply {
                    for (arg in args) this.add(GSON.toJsonTree(arg))
                }

                val uniqueId = UUID.randomUUID()
                command.messageIds[this.message.fromId] = uniqueId

                jsonObject.addProperty("unique_id", uniqueId.toString())
                jsonObject.addProperty("user_id", this.message.fromId)
                jsonObject.addProperty("command", command.commandNames[0])
                jsonObject.add("args", jsonArray)

                return@run GSON.toJson(jsonObject)
            })
        }

        return sendQuery
    }
}