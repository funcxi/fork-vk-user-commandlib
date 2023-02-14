package dev.wasteix.commandlib.model.data

import dev.wasteix.commandlib.model.sender.CommandSender

data class UserDialogStateData(
    var commandSender: CommandSender,
    var state: String,
    var value: Any?,
    val timeout: Long
)