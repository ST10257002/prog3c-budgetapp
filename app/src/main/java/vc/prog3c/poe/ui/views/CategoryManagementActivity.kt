package vc.prog3c.poe.ui.views

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import android.app.AlertDialog
import android.widget.Toast
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.CategoryType
import vc.prog3c.poe.databinding.ActivityCategoryManagementBinding
import vc.prog3c.poe.ui.adapters.CategoryAdapter
import vc.prog3c.poe.ui.viewmodels.CategoryViewModel
import java.util.UUID
import androidx.core.content.ContextCompat

class CategoryManagementActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityCategoryManagementBinding
    private lateinit var model: CategoryViewModel
    private lateinit var adapter: CategoryAdapter
    private lateinit var iconChipGroup: com.google.android.material.chip.ChipGroup
    private lateinit var colorChipGroup: com.google.android.material.chip.ChipGroup


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
        setupRecyclerView()
        setupClickListeners()

        model = ViewModelProvider(this)[CategoryViewModel::class.java]
        
        observeViewModel()
    }


    // --- ViewModel


    private fun observeViewModel() {
        model.categories.observe(this) { categories ->
            adapter.submitList(categories)
        }

        model.error.observe(this) { error ->
            error?.let {
                Snackbar.make(binds.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }


    // --- Internals


    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Add Category")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val nameInput = dialogView.findViewById<TextInputLayout>(R.id.nameInput)
                val descriptionInput = dialogView.findViewById<TextInputLayout>(R.id.descriptionInput)
                val typeInput = dialogView.findViewById<TextInputLayout>(R.id.typeInput)
                val typeDropdown = typeInput.editText as? AutoCompleteTextView
                val minInput = dialogView.findViewById<TextInputLayout>(R.id.minBudgetInput)
                val maxInput = dialogView.findViewById<TextInputLayout>(R.id.maxBudgetInput)
                val iconChipGroup = dialogView.findViewById<ChipGroup>(R.id.iconChipGroup)
                val colorChipGroup = dialogView.findViewById<ChipGroup>(R.id.colorChipGroup)
                val activeSwitch = dialogView.findViewById<SwitchMaterial>(R.id.activeSwitch)

                // Validate inputs
                if (nameInput.editText?.text.isNullOrBlank()) {
                    nameInput.error = "Name is required"
                    return@setPositiveButton
                }

                if (typeDropdown?.text.isNullOrBlank()) {
                    typeInput.error = "Type is required"
                    return@setPositiveButton
                }

                if (minInput.editText?.text.isNullOrBlank()) {
                    minInput.error = "Minimum budget is required"
                    return@setPositiveButton
                }

                if (maxInput.editText?.text.isNullOrBlank()) {
                    maxInput.error = "Maximum budget is required"
                    return@setPositiveButton
                }

                val selectedIconId = iconChipGroup.checkedChipId
                if (selectedIconId == View.NO_ID) {
                    Snackbar.make(binds.root, "Please select an icon", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedColorId = colorChipGroup.checkedChipId
                if (selectedColorId == View.NO_ID) {
                    Snackbar.make(binds.root, "Please select a color", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Get selected icon and color
                val selectedIcon = when (selectedIconId) {
                    R.id.iconSavings -> "ic_savings"
                    R.id.iconUtilities -> "ic_utilities"
                    R.id.iconEmergency -> "ic_error"
                    R.id.iconIncome -> "ic_income"
                    R.id.iconExpense -> "ic_expense"
                    else -> "ic_category"
                }

                val selectedColor = when (selectedColorId) {
                    R.id.colorBlue -> "colorBlue"
                    R.id.colorRed -> "colorRed"
                    R.id.colorPurple -> "colorPurple"
                    R.id.colorOrange -> "colorOrange"
                    else -> "colorGreen"
                }

                // Create category
                val category = Category(
                    id = UUID.randomUUID().toString(),
                    name = nameInput.editText?.text.toString(),
                    description = descriptionInput.editText?.text.toString(),
                    type = CategoryType.valueOf(typeDropdown?.text.toString()),
                    minBudget = minInput.editText?.text.toString().toDoubleOrNull() ?: 0.0,
                    maxBudget = maxInput.editText?.text.toString().toDoubleOrNull() ?: 0.0,
                    icon = selectedIcon,
                    color = selectedColor,
                    isActive = activeSwitch.isChecked,
                    isEditable = true
                )

                model.addCategory(category)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Set up type dropdown
        val typeInput = dialogView.findViewById<TextInputLayout>(R.id.typeInput)
        val typeDropdown = typeInput.editText as? AutoCompleteTextView
        val types = CategoryType.values().filter { it != CategoryType.SAVINGS }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        typeDropdown?.setAdapter(adapter)

        // Set up icon chips with titles
        val iconChipGroup = dialogView.findViewById<ChipGroup>(R.id.iconChipGroup)
        iconChipGroup.removeAllViews()
        
        val icons = listOf(
            Triple(R.id.iconSavings, R.drawable.ic_savings, "Savings"),
            Triple(R.id.iconUtilities, R.drawable.ic_utilities, "Utilities"),
            Triple(R.id.iconEmergency, R.drawable.ic_error, "Emergency"),
            Triple(R.id.iconIncome, R.drawable.ic_income, "Income"),
            Triple(R.id.iconExpense, R.drawable.ic_expense, "Expense"),
            Triple(R.id.iconCategory, R.drawable.ic_category, "Category")
        )

        icons.forEach { (id, drawable, title) ->
            val chip = Chip(this).apply {
                this.id = id
                text = title
                isCheckable = true
                chipIcon = ContextCompat.getDrawable(context, drawable)
                chipIconTint = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_primary))
            }
            iconChipGroup.addView(chip)
        }

        // Set up color chips with titles
        val colorChipGroup = dialogView.findViewById<ChipGroup>(R.id.colorChipGroup)
        colorChipGroup.removeAllViews()
        
        val colors = listOf(
            Triple(R.id.colorGreen, R.color.colorGreen, "Green"),
            Triple(R.id.colorBlue, R.color.colorBlue, "Blue"),
            Triple(R.id.colorRed, R.color.colorRed, "Red"),
            Triple(R.id.colorPurple, R.color.colorPurple, "Purple"),
            Triple(R.id.colorOrange, R.color.colorOrange, "Orange")
        )

        colors.forEach { (id, color, title) ->
            val chip = Chip(this).apply {
                this.id = id
                text = title
                isCheckable = true
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, color))
                setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            colorChipGroup.addView(chip)
        }

        dialog.show()
    }



    private fun showEditCategoryDialog(category: Category) {
        if (!category.isEditable) {
            Snackbar.make(binds.root, "This category cannot be edited", Snackbar.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)

        val nameInput = dialogView.findViewById<TextInputLayout>(R.id.nameInput)
        val descriptionInput = dialogView.findViewById<TextInputLayout>(R.id.descriptionInput)
        val typeInput = dialogView.findViewById<TextInputLayout>(R.id.typeInput)
        val typeDropdown = typeInput.editText as? AutoCompleteTextView
        val minInput = dialogView.findViewById<TextInputLayout>(R.id.minBudgetInput)
        val maxInput = dialogView.findViewById<TextInputLayout>(R.id.maxBudgetInput)
        iconChipGroup = dialogView.findViewById(R.id.iconChipGroup)
        colorChipGroup = dialogView.findViewById(R.id.colorChipGroup)
        val activeSwitch = dialogView.findViewById<SwitchMaterial>(R.id.activeSwitch)

        // Populate fields
        nameInput.editText?.setText(category.name)
        descriptionInput.editText?.setText(category.description)
        typeDropdown?.setText(category.type.name, false)
        minInput.editText?.setText(category.minBudget.toString())
        maxInput.editText?.setText(category.maxBudget.toString())
        activeSwitch.isChecked = category.isActive

        // Set up type dropdown
        val types = CategoryType.values().filter { it != CategoryType.SAVINGS }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        typeDropdown?.setAdapter(adapter)

        // Set current selections
        iconChipGroup.check(
            when (category.icon) {
                "ic_savings" -> R.id.iconSavings
                "ic_utilities" -> R.id.iconUtilities
                "ic_error" -> R.id.iconEmergency
                "ic_income" -> R.id.iconIncome
                "ic_expense" -> R.id.iconExpense
                else -> R.id.iconCategory
            }
        )

        colorChipGroup.check(
            when (category.color) {
                "colorBlue" -> R.id.colorBlue
                "colorRed" -> R.id.colorRed
                "colorPurple" -> R.id.colorPurple
                "colorOrange" -> R.id.colorOrange
                else -> R.id.colorGreen
            }
        )

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Edit Category")
            .setView(dialogView)
            .setPositiveButton("Save", null) // Set to null initially
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = nameInput.editText?.text.toString()
                val description = descriptionInput.editText?.text.toString()
                val type = typeDropdown?.text.toString()
                val min = minInput.editText?.text.toString().toDoubleOrNull() ?: 0.0
                val max = maxInput.editText?.text.toString().toDoubleOrNull() ?: 0.0
                val isActive = activeSwitch.isChecked

                if (name.isBlank()) {
                    nameInput.error = "Name is required"
                    return@setOnClickListener
                }
                if (type.isBlank()) {
                    typeInput.error = "Type is required"
                    return@setOnClickListener
                }
                if (min > max) {
                    maxInput.error = "Max must be ≥ Min"
                    return@setOnClickListener
                }



                val updatedCategory = category.copy(
                    name = name,
                    type = CategoryType.valueOf(type),
                    icon = when {

                        else -> "ic_category"
                    },
                    color = when  {

                        else -> "colorGreen"
                    },
                    description = description,
                    minBudget = min,
                    maxBudget = max,
                    isActive = isActive
                )
                model.updateCategory(updatedCategory)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
    

    private fun showDeleteConfirmationDialog(category: Category) {
        if (!category.isEditable) {
            Snackbar.make(binds.root, "This category cannot be deleted", Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        MaterialAlertDialogBuilder(this).setTitle("Delete Category")
            .setMessage("Are you sure you want to delete ${category.name}?")
            .setPositiveButton("Delete") { _, _ ->
                model.deleteCategory(category.id)
            }.setNegativeButton("Cancel", null).show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    // --- Event Handlers


    override fun onClick(view: View?) {
        when (view?.id) {
            binds.addCategoryButton.id -> showAddCategoryDialog()
        }
    }
    
    
    private fun setupClickListeners() {
        binds.addCategoryButton.setOnClickListener(this)
    }


    // --- UI Configuration


    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Manage Categories"
        }
    }


    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onEditClick = { category -> showEditCategoryDialog(category) },
            onDeleteClick = { category -> showDeleteConfirmationDialog(category) }
        )
        binds.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategoryManagementActivity)
            adapter = this@CategoryManagementActivity.adapter
        }
    }


    private fun setupBindings() {
        binds = ActivityCategoryManagementBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            // Add top padding to the AppBarLayout (parent of toolbar)
            (binds.toolbar.parent as? View)?.let { appBar ->
                appBar.setPadding(
                    appBar.paddingLeft,
                    systemBars.top,
                    appBar.paddingRight,
                    appBar.paddingBottom
                )
            }
            insets
        }
        setupStatusBar()
        setupToolbar()
    }

    private fun setupStatusBar() {
        window.statusBarColor = getColor(R.color.primary)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and 
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }
} 