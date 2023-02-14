package dev.wasteix.commandlib.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DefaultDialogState(
    /**
     * Следующая стадия диалога, после дефолтной
     */
    val nextState: String
)
