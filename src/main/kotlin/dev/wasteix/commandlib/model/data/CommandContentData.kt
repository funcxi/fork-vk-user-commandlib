package dev.wasteix.commandlib.model.data

import dev.wasteix.commandlib.BaseCommand
import dev.wasteix.commandlib.annotation.Cooldown
import dev.wasteix.commandlib.annotation.MinArg
import java.lang.reflect.Method

class CommandContentData {
    var cooldown: Cooldown? = null
    var minArg: MinArg? = null

    constructor(command: BaseCommand) {
        command.javaClass.apply {
            cooldown = if (this.isAnnotationPresent(Cooldown::class.java)) this.getAnnotation(Cooldown::class.java) else null
            minArg = if (this.isAnnotationPresent(MinArg::class.java)) this.getAnnotation(MinArg::class.java) else null
        }
    }

    constructor(method: Method) {
        cooldown = if (method.isAnnotationPresent(Cooldown::class.java)) method.getAnnotation(Cooldown::class.java) else null
        minArg = if (method.isAnnotationPresent(MinArg::class.java)) method.getAnnotation(MinArg::class.java) else null
    }
}