package com.example.agent.nexus.agent

/** Normalized provider failure that preserves the provider identity for debugging. */
class ProviderRequestException(
    providerId: String,
    cause: Throwable
) : IllegalStateException(
    "$providerId request failed: ${cause.message ?: cause::class.simpleName}",
    cause
) {
    constructor(providerId: String, message: String) : this(
        providerId,
        IllegalStateException(message)
    )
}
