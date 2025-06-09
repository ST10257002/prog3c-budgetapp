package vc.prog3c.poe.core.usecases

import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX
import vc.prog3c.poe.core.models.ConsentUiHost

/**
 * Use-case to request permissions on the device.
 *
 * **Note:** This class should not be instantiated and invoked directly. Use the
 * coordinator class or the builder's Fluent API instead. This ensures that code
 * misconfigurations are guarded against correctly.
 * 
 * @reference - PermissionX Library Docs - https://github.com/guolindev/PermissionX
 *
 * @param caller The [FragmentActivity] that is requesting the permissions.
 * @param uiHost The [ConsentUiHost] delegate responsible for implementing logic
 *        to display the contracted UI components and handle the request events.
 * @param permissions The [Array] of permission strings to request.
 *
 * @author ST10257002
 */
class ConsentTransactionUseCase(
    private val caller: FragmentActivity,
    private val uiHost: ConsentUiHost,
    private val permissions: Array<String>
) {

    /**
     * Executes the permission request.
     *
     * [PermissionX] is used as a delegate to handle the complexity of lifecycle
     * management and permission requests. The consent framework here allows the
     * respective views to hook into UI and result events while maintaining SOD.
     *
     * @param showInitialUi It is recommended to leave this as the default value
     *        of `true`. Essentially this will determine whether the UI delegate
     *        should confirm with the user before the transaction begins. Useful
     *        for granular control over access to permission-bound features.
     *
     * @author ST10257002
     */
    fun execute(showInitialUi: Boolean = true) {
        PermissionX.init(caller).permissions(*permissions).apply {
            if (showInitialUi) {
                explainReasonBeforeRequest()
            }

            onExplainRequestReason { scope, declinedTemporarily ->
                uiHost.onShowInitialConsentUi(scope, declinedTemporarily)
            }

            onForwardToSettings { scope, declinedPermanently ->
                uiHost.onShowWarningConsentUi(scope, declinedPermanently)
            }

            request { fullConsentGiven, accepted, declined ->
                when (fullConsentGiven) {
                    true -> uiHost.onConsentsAccepted(accepted)
                    else -> uiHost.onConsentsDeclined(declined)
                }
            }
        }
    }
}