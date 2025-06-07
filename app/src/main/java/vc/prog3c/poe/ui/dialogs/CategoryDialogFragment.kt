package vc.prog3c.poe.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.CategoryType
import java.util.UUID

class CategoryDialogFragment : BottomSheetDialogFragment() {

    private var category: Category? = null
    private var onSaveListener: ((Category) -> Unit)? = null

    private lateinit var nameInput: TextInputLayout
    private lateinit var descriptionInput: TextInputLayout
    private lateinit var typeInput: TextInputLayout

    // private lateinit var budgetLimitInput: TextInputLayout
    private lateinit var minBudgetInput: TextInputLayout
    private lateinit var maxBudgetInput: TextInputLayout

    private lateinit var iconChipGroup: com.google.android.material.chip.ChipGroup
    private lateinit var colorChipGroup: com.google.android.material.chip.ChipGroup
    private lateinit var activeSwitch: com.google.android.material.switchmaterial.SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_add_category, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupTypeDropdown()
        setupValidation()
        category?.let { populateFields(it) }
    }

    private fun initializeViews(view: View) {
        nameInput = view.findViewById(R.id.nameInput)
        descriptionInput = view.findViewById(R.id.descriptionInput)
        typeInput = view.findViewById(R.id.typeInput)
        // budgetLimitInput = view.findViewById(R.id.budgetLimitInput)
        minBudgetInput = view.findViewById(R.id.minBudgetInput)
        maxBudgetInput = view.findViewById(R.id.maxBudgetInput)
        iconChipGroup = view.findViewById(R.id.iconChipGroup)
        colorChipGroup = view.findViewById(R.id.colorChipGroup)
        activeSwitch = view.findViewById(R.id.activeSwitch)

        view.findViewById<View>(R.id.saveButton).setOnClickListener { saveCategory() }
        view.findViewById<View>(R.id.cancelButton).setOnClickListener { dismiss() }
    }

    private fun setupTypeDropdown() {
        val types = CategoryType.values().map { it.name }
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            types
        )
        (typeInput.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setupValidation() {
        nameInput.editText?.doAfterTextChanged {
            nameInput.error = if (it.isNullOrBlank()) "Name is required" else null
        }

        typeInput.editText?.doAfterTextChanged {
            typeInput.error = if (it.isNullOrBlank()) "Type is required" else null
        }

        minBudgetInput.editText?.doAfterTextChanged {
            val value = it.toString().toDoubleOrNull()
            minBudgetInput.error = if (value != null && value < 0) "Min cannot be negative" else null
        }

        maxBudgetInput.editText?.doAfterTextChanged {
            val value = it.toString().toDoubleOrNull()
            maxBudgetInput.error = if (value != null && value < 0) "Max cannot be negative" else null
        }
    }

    private fun populateFields(category: Category) {
        nameInput.editText?.setText(category.name)
        descriptionInput.editText?.setText(category.description)
        typeInput.editText?.setText(category.type.name)
        // budgetLimitInput.editText?.setText(category.budgetLimit.toString())
        minBudgetInput.editText?.setText(category.minBudget.toString())
        maxBudgetInput.editText?.setText(category.maxBudget.toString())
        activeSwitch.isChecked = category.isActive

        val iconChip = when (category.icon) {
            "ic_category" -> R.id.iconCategory
            "ic_savings" -> R.id.iconSavings
            "ic_utilities" -> R.id.iconUtilities
            "ic_error" -> R.id.iconEmergency
            "ic_income" -> R.id.iconIncome
            "ic_expense" -> R.id.iconExpense
            else -> R.id.iconCategory
        }
        iconChipGroup.check(iconChip)

        val colorChip = when (category.color) {
            "colorGreen" -> R.id.colorGreen
            "colorBlue" -> R.id.colorBlue
            "colorRed" -> R.id.colorRed
            "colorPurple" -> R.id.colorPurple
            "colorOrange" -> R.id.colorOrange
            else -> R.id.colorGreen
        }
        colorChipGroup.check(colorChip)
    }

    private fun saveCategory() {
        val name = nameInput.editText?.text?.toString()
        val description = descriptionInput.editText?.text?.toString()
        val typeStr = typeInput.editText?.text?.toString()
        // val budgetLimit = budgetLimitInput.editText?.text?.toString()?.toDoubleOrNull() ?: 0.0
        val minBudget = minBudgetInput.editText?.text?.toString()?.toDoubleOrNull() ?: 0.0
        val maxBudget = maxBudgetInput.editText?.text?.toString()?.toDoubleOrNull() ?: 0.0
        val isActive = activeSwitch.isChecked

        if (name.isNullOrBlank() || typeStr.isNullOrBlank()) {
            nameInput.error = if (name.isNullOrBlank()) "Name is required" else null
            typeInput.error = if (typeStr.isNullOrBlank()) "Type is required" else null
            return
        }

        if (minBudget > maxBudget) {
            maxBudgetInput.error = "Max must be ≥ Min"
            return
        }

        val type = try {
            CategoryType.valueOf(typeStr)
        } catch (e: IllegalArgumentException) {
            typeInput.error = "Invalid category type"
            return
        }

        val selectedIconChip = iconChipGroup.findViewById<Chip>(iconChipGroup.checkedChipId)
        val selectedColorChip = colorChipGroup.findViewById<Chip>(colorChipGroup.checkedChipId)

        val icon = when (selectedIconChip?.id) {
            R.id.iconCategory -> "ic_category"
            R.id.iconSavings -> "ic_savings"
            R.id.iconUtilities -> "ic_utilities"
            R.id.iconEmergency -> "ic_error"
            R.id.iconIncome -> "ic_income"
            R.id.iconExpense -> "ic_expense"
            else -> "ic_category"
        }

        val color = when (selectedColorChip?.id) {
            R.id.colorGreen -> "colorGreen"
            R.id.colorBlue -> "colorBlue"
            R.id.colorRed -> "colorRed"
            R.id.colorPurple -> "colorPurple"
            R.id.colorOrange -> "colorOrange"
            else -> "colorGreen"
        }

        val updatedCategory = category?.copy(
            name = name,
            description = description ?: "",
            type = type,
            icon = icon,
            color = color,
            // budgetLimit = budgetLimit,
            minBudget = minBudget,
            maxBudget = maxBudget,
            isActive = isActive
        ) ?: Category(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description ?: "",
            type = type,
            icon = icon,
            color = color,
            // budgetLimit = budgetLimit,
            minBudget = minBudget,
            maxBudget = maxBudget,
            isActive = isActive,
            isEditable = true
        )

        onSaveListener?.invoke(updatedCategory)
        dismiss()
    }

    companion object {
        fun newInstance(
            category: Category? = null,
            onSave: (Category) -> Unit
        ): CategoryDialogFragment {
            return CategoryDialogFragment().apply {
                this.category = category
                this.onSaveListener = onSave
            }
        }
    }
}
