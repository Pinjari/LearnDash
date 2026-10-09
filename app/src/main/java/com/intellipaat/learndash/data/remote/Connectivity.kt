package com.intellipaat.learndash.data.remote

/**
 * Answers "are we online?" so the repository can fail fast to cache instead
 * of burning a doomed fetch. Interface (not the concrete gate) so tests can
 * answer without a Context.
 */
interface Connectivity {
    fun isOnline(): Boolean
}
