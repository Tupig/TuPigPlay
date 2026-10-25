package com.shilapi.xcertplay.update

internal object UpdateVersion {
    private val TRIPLE = Regex("(\\d+)\\.(\\d+)\\.(\\d+)")
    /** Official channel: tag/versionName is exactly MAJOR.MINOR.PATCH, optional leading v. */
    private val STABLE = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)$")

    internal fun parse(value: String): Triple<Int, Int, Int>? =
        TRIPLE.find(value)?.let { match ->
            Triple(match.groupValues[1].toInt(), match.groupValues[2].toInt(), match.groupValues[3].toInt())
        }

    /** True for release tags/names users should be offered (no -rc / -beta suffix). */
    internal fun isStableLabel(value: String): Boolean = STABLE.matches(value.trim())

    internal fun isNewer(remote: String, installed: String): Boolean {
        val target = parse(remote) ?: return false
        val current = parse(installed) ?: return true
        if (target.first != current.first) return target.first > current.first
        if (target.second != current.second) return target.second > current.second
        return target.third > current.third
    }
}
