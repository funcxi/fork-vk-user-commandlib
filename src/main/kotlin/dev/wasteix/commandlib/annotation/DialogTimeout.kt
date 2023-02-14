package dev.wasteix.commandlib.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DialogTimeout(
    /**
     * Время через сколько сбросится диалог
     */
    val value: Long,

    /**
     * В чем измеряется времяя
     */
    val unit: TimeUnit,

    /**
     * Пишет сообщение отправителю, когда время истекло
     */
    val messageTimeExpired: String = ""
)