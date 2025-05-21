package vc.prog3c.poe.core.coordinators

import androidx.fragment.app.FragmentActivity
import vc.prog3c.poe.core.coordinators.ConsentCoordinator.requestConsent
import vc.prog3c.poe.core.models.ConsentBundle
import vc.prog3c.poe.core.models.ConsentTransactionBuilder
import vc.prog3c.poe.core.models.ConsentUiHost

/**
 * Coordinates the contracted permission request lifecycle.
 *
 * This class serves as an easy, service-like wrapper around the permission
 * request pipeline. It removes the code overhead and complexity of calling the
 * use-case directly (not recommended) or of using the builder's Fluent API.
 *
 * See [requestConsent] for implementations.
 *
 * @author ST10257002
 */
object ConsentCoordinator {

    /**
     * Execute the permission request with the coordinated contracts.
     *
     * **Examples:**
     *
     * ```
     * // Trigger the system permission requests
     * ConsentCoordinator.requestConsent(
     *     this, this, consentBundles = arrayOf(
     *         ConsentBundle.CameraAccess, ConsentBundle.ImageLibraryAccess
     *     )
     * )
     * ```
     *
     * @param caller The [FragmentActivity] that is requesting the permissions.
     * @param uiHost The [ConsentUiHost] delegate responsible for implementing
     *        logic to display the contracted UI components and handle the
     *        request events.
     * @param consentBundles The spread array of [ConsentBundle] to request.
     *
     * @author ST10257002
     */
    fun requestConsent(
        caller: FragmentActivity, uiHost: ConsentUiHost, vararg consentBundles: ConsentBundle
    ) {
        val transaction = ConsentTransactionBuilder(caller).apply {
            requestBundles(*consentBundles)
            delegateUiHost(uiHost)
        }.build()

        transaction.execute()
    }
}