package vc.prog3c.poe.core.models

import vc.prog3c.poe.core.utils.ConsentBundleTranslator

/**
 * Logical version-agnostic aliases for permissions and permission groups.
 *
 * Designed for large, complicated systems, these bundles are tightly coupled to
 * the [ConsentBundleTranslator] where these aliases are converted from abstract
 * ideas into logical permission groups.
 *
 * @author ST10257002
 */
enum class ConsentBundle {
    CameraAccess, ImageLibraryAccess
}