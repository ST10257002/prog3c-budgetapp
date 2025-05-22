package vc.prog3c.poe.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import vc.prog3c.poe.databinding.ViewProfileBinding
import vc.prog3c.poe.ui.views.LoginView
import vc.prog3c.poe.ui.views.ManageGoalsView

class ProfileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewProfileBinding.inflate(LayoutInflater.from(context), this, true)

    fun initialize(lifecycleOwner: LifecycleOwner) {
        // TODO: Backend Implementation Required
        // 1. Create Firestore collection 'users' with structure:
        //    - userId: string
        //    - name: string
        //    - email: string
        //    - profileImageUrl: string
        //    - createdAt: timestamp
        //    - lastLogin: timestamp
        // 2. Implement user authentication using Firebase Auth
        // 3. Add profile image upload to Firebase Storage
        // 4. Implement real-time profile updates
        // 5. Add offline persistence support

        // Set up click listeners
        binding.manageGoalsButton.setOnClickListener {
            // Navigate to ManageGoalsView
            val manageGoalsView = ManageGoalsView(context)
            (parent as? LinearLayout)?.apply {
                removeAllViews()
                addView(manageGoalsView)
            }
        }

        binding.logoutButton.setOnClickListener {
            // TODO: Backend Implementation Required
            // 1. Sign out user from Firebase Auth
            // 2. Clear local user data
            // 3. Update lastLogin timestamp in Firestore
            // 4. Handle offline state during logout

            // Navigate to LoginView
            /*val loginView = LoginView(context)
            (parent as? LinearLayout)?.apply {
                removeAllViews()
                addView(loginView)
            }*/
        }
    }
} 