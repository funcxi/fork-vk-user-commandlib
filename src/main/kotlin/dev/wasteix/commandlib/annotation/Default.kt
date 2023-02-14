package dev.wasteix.commandlib.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Default(
    /**
     * Вызывать ли метод помеченный данной аннотацией при ненайденной подкоманды
     */
    val subCommandNotFoundInvoke: Boolean = false
)