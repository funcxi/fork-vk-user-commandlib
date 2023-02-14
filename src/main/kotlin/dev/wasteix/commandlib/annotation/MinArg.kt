package dev.wasteix.commandlib.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MinArg(
    /**
     * Минимальное кол-во аргументов
     */
    val value: Int = 1,

    /**
     * Сообщение, когда аргументов недостаточно
     */
    val messageNoArgs: String = "Недостаточно аргументов."
)