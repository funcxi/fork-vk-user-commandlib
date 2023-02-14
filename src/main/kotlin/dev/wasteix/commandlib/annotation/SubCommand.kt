package dev.wasteix.commandlib.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubCommand(
    /**
     * Названия подкоманды
     */
    vararg val commandNames: String
)