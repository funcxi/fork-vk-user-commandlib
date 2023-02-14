package dev.wasteix.commandlib.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cooldown(
    /**
     * Название задержки
     */
    val key: String,

    /**
     * Время задержки
     */
    val value: Long,

    /**
     * В чем измеряется время
     */
    val unit: TimeUnit,

    /**
     * Сообщение, если игрок имеет задержку
     */
    val messageHasCooldown: String
)