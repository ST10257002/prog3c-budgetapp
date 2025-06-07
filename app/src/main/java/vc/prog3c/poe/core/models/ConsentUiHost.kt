package vc.prog3c.poe.core.models

import com.permissionx.guolindev.request.ExplainScope
import com.permissionx.guolindev.request.ForwardScope
import vc.prog3c.poe.core.coordinators.ConsentCoordinator

/**
 * Interface contract for handling user interaction UI during the permission
 * request lifecycle.
 *
 * @author ST10257002
 */
interface ConsentUiHost {

    /**
     * Callback for when some permissions have been refused temporarily.
     *
     * **Note:** This function should be used by the contract owner only and
     * should not be called directly by the contract recipient.
     *
     * **Examples:**
     *
     * ```
     * override fun onShowInitialConsentUi(
     *     scope: ExplainScope, declinedTemporarily: List<String>
     * ) {
     *     scope.showRequestReasonDialog(
     *         permissions = declinedTemporarily,
     *         getString(R.string.permx_explain_scope_description),
     *         getString(R.string.permx_explain_scope_on_positive),
     *         getString(R.string.permx_explain_scope_on_negative)
     *     )
     * }
     * ```
     *
     * @param scope See [ExplainScope] for more details.
     * @param declinedTemporarily [List] of permissions that have been refused
     *        temporarily throughout the consent request pipeline. Can be used
     *        by the UI host to perform further conditional logic.
     *
     * @author ST10257002
     */
    fun onShowInitialConsentUi(scope: ExplainScope, declinedTemporarily: List<String>)


    /**
     * Callback for when some permissions have been refused permanently.
     *
     * **Note:** This function should be used by the contract owner only and
     * should not be called directly by the contract recipient.
     *
     * **Examples:**
     *
     * ```
     * override fun onShowWarningConsentUi(
     *     scope: ForwardScope, declinedPermanently: List<String>
     * ) {
     *     scope.showForwardToSettingsDialog(
     *         permissions = declinedPermanently,
     *         getString(R.string.permx_forward_scope_description),
     *         getString(R.string.permx_forward_scope_on_positive),
     *         getString(R.string.permx_forward_scope_on_negative)
     *     )
     * }
     * ```
     *
     * @param scope See [ForwardScope] for more details.
     * @param declinedPermanently [List] of permissions that have been refused
     *        permanently throughout the consent request pipeline. Can be used
     *        by the UI host to perform further conditional logic.
     *
     * @author ST10257002
     */
    fun onShowWarningConsentUi(scope: ForwardScope, declinedPermanently: List<String>)


    /**
     * Callback for when all permissions have been accepted.
     *
     * **Note:** This function should be used by the contract owner only and
     * should not be called directly by the contract recipient.
     *
     * @param accepted List of permissions that have been granted.
     *        This list is provided by the permissions framework that is calling
     *        this contracted function.
     *
     * @see ConsentCoordinator
     * @author ST10257002
     */
    fun onConsentsAccepted(accepted: List<String>)


    /**
     * Callback for when some permissions have been refused.
     *
     * **Note:** This function should be used by the contract owner only and
     * should not be called directly by the contract recipient.
     *
     * @param declined List of permissions that have been refused.
     *        This list is provided by the permissions framework that is calling
     *        this contracted function.
     *
     * @see ConsentCoordinator
     * @author ST10257002
     */
    fun onConsentsDeclined(declined: List<String>)
}