
package dev.wasteix.commandlib.utility

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import java.util.concurrent.TimeUnit

private val COOLDOWNS: Table<Int, String, Long> = HashBasedTable.create()

fun addCooldown(fromId: Int, key: String, value: Long, unit: TimeUnit) {
    if (COOLDOWNS.contains(fromId, key)) return

    COOLDOWNS.put(fromId, key, System.currentTimeMillis() + unit.toMillis(value))
}

fun removeCooldown(fromId: Int, key: String) {
    COOLDOWNS.remove(fromId, key)
}

fun hasCooldown(fromId: Int, key: String): Boolean {
    if (!COOLDOWNS.contains(fromId, key)) return false

    val cooldown = COOLDOWNS.get(fromId, key)
    val hasCooldown = cooldown?.minus(System.currentTimeMillis())!! > 0

    if (!hasCooldown) COOLDOWNS.remove(fromId, key)

    return hasCooldown
}

fun getCooldown(fromId: Int, key: String, unit: TimeUnit): Long {
    if (!hasCooldown(fromId, key)) return 0

    val currentCooldown = System.currentTimeMillis() - COOLDOWNS.get(fromId, key)!!

    return if ((System.currentTimeMillis() - currentCooldown) < 0) unit.convert(currentCooldown, unit) else 0
}