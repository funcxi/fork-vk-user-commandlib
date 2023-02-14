package dev.wasteix.commandlib.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DialogState(
    /**
     * Текущая стадия диалога
     */
    val state: String,

    /**
     * Следущая стадия диалога
     */
    val nextState: String = ""
)