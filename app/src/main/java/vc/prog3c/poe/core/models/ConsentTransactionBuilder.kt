package vc.prog3c.poe.core.models

import androidx.fragment.app.FragmentActivity
import vc.prog3c.poe.core.usecases.ConsentTransactionUseCase
import vc.prog3c.poe.core.utils.ConsentBundleTranslator

/**
 * Builds and configures a [ConsentTransactionUseCase].
 *
 * Configuration options are provided using the chained builder functions
 * directly on the builder object or via [apply].
 *
 * **Examples:**
 *
 * ```
 * // Trigger the system permission requests
 * val request = ConsentTransactionBuilder(_).apply {
 *      delegateUiHost(_)
 *      requestBundles(_)
 * }.build()
 *
 * request.execute()
 * ```
 * 
 * @reference Builder Pattern - https://medium.com/@dheerubhadoria/simplifying-object-creation-with-the-builder-design-pattern-in-kotlin-6fb282c0143e
 *
 * @throws IllegalArgumentException
 * @throws IllegalStateException
 * @author ST10257002
 */
class ConsentTransactionBuilder(
    private val caller: FragmentActivity
) {
    private var configuredHandler: ConsentUiHost? = null
    private var configuredBundles: List<ConsentBundle>? = null


    // --- FluentAPI


    /**
     * Configures the UI delegate for the permission request.
     *
     * **Examples:**
     *
     * ```
     * ConsentTransactionBuilder().delegateUiHost(_)
     * ```
     *
     * ```
     * ConsentTransactionBuilder().apply {
     *      delegateUiHost(_)
     * }
     * ```
     *
     * @return [ConsentTransactionBuilder]
     * @author ST10257002
     */
    fun delegateUiHost(uiHost: ConsentUiHost): ConsentTransactionBuilder = apply {
        configuredHandler = uiHost
    }


    /**
     * Configures the bundles to be translated and requested.
     *
     * **Examples:**
     *
     * ```
     * ConsentTransactionBuilder().requestBundles(_)
     * ```
     *
     * ```
     * ConsentTransactionBuilder().apply {
     *      requestBundles(_)
     * }
     * ```
     *
     * @return [ConsentTransactionBuilder]
     * @author ST10257002
     */
    fun requestBundles(vararg values: ConsentBundle): ConsentTransactionBuilder = apply {
        configuredBundles = values.toList()
    }


    /**
     * Constructs and returns the [ConsentTransactionUseCase].
     *
     * **Note:** This method is guarded from misconfigurations. Ensure that all
     * of the required configurations have been provided and that the necessary
     * exception handling has been implemented.
     *
     * @return [ConsentTransactionUseCase]
     *
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     * @author ST10257002
     */
    fun build(): ConsentTransactionUseCase {
        requireBundles()
        requireHandler()

        checkCallerIsAlive()

        return ConsentTransactionUseCase(
            caller, configuredHandler!!, permissions = configuredBundles!!.flatMap {
                ConsentBundleTranslator.toAndroid(it).toList()
            }.toTypedArray()
        )
    }


    // --- Internals


    /**
     * Throws if the caller is not in a valid state for the permission request.
     *
     * **Fail conditions:**
     *
     * - [FragmentActivity] caller lifecycle is `Finishing`.
     * - [FragmentActivity] caller lifecycle is `Destroyed`.
     *
     * @see FragmentActivity.isFinishing
     * @see FragmentActivity.isDestroyed
     * @see check
     *
     * @throws IllegalStateException
     * @author ST10257002
     */
    private fun checkCallerIsAlive() {
        check(!(caller.isFinishing || caller.isDestroyed)) {
            "Caller is not in a valid state for the permission request."
        }
    }


    /**
     * Throws if the required bundles are not provided or null.
     *
     * If this exception has been thrown, ensure that you have provided a
     * value to the builder using the chained [requestBundles] function during
     * its construction.
     *
     * **Fail conditions:**
     *
     * - [List] of bundles is null and/or has not been provided.
     * - [List] of bundles is empty.
     *
     * @see require
     *
     * @throws IllegalArgumentException
     * @author ST10257002
     */
    private fun requireBundles() = require(configuredBundles?.isNotEmpty() == true) {
        "The configuration for bundles was not provided or poorly constructed."
    }


    /**
     * Throws if the required handler is null.
     *
     * If this exception has been thrown, ensure that you have provided a
     * value to the builder using the chained [delegateUiHost] function during
     * its construction.
     *
     * **Fail conditions:**
     *
     * - [ConsentUiHost] delegate is null and/or has not been provided.
     *
     * @see require
     *
     * @throws IllegalArgumentException
     * @author ST10257002
     */
    private fun requireHandler() = require(configuredHandler != null) {
        "The configuration for handler was not provided or poorly constructed."
    }
}