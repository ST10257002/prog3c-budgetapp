package vc.prog3c.poe.ui.views

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
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.Category
import vc.prog3c.poe.data.models.CategoryType
import vc.prog3c.poe.databinding.ActivityCategoryManagementBinding
import vc.prog3c.poe.ui.adapters.CategoryAdapter
import vc.prog3c.poe.ui.viewmodels.CategoryViewModel
import java.util.UUID

class CategoryManagementActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binds: ActivityCategoryManagementBinding
    private lateinit var model: CategoryViewModel
    private lateinit var adapter: CategoryAdapter


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

        val nameInput = dialogView.findViewById<TextInputLayout>(R.id.nameInput)
        val typeInput = dialogView.findViewById<TextInputLayout>(R.id.typeInput)
        val typeDropdown = typeInput.editText as? AutoCompleteTextView
        val minInput = dialogView.findViewById<TextInputLayout>(R.id.minBudgetInput)
        val maxInput = dialogView.findViewById<TextInputLayout>(R.id.maxBudgetInput)
        val iconChipGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.iconChipGroup)
        val colorChipGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.colorChipGroup)

        val types = CategoryType.values().filter { it != CategoryType.SAVINGS }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        typeDropdown?.setAdapter(adapter)

        iconChipGroup.check(R.id.iconCategory)
        colorChipGroup.check(R.id.colorGreen)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.editText?.text.toString()
                val type = typeDropdown?.text.toString()
                val min = minInput.editText?.text.toString().toDoubleOrNull() ?: 0.0
                val max = maxInput.editText?.text.toString().toDoubleOrNull() ?: 0.0

                val selectedIconChip = dialogView.findViewById<com.google.android.material.chip.Chip>(iconChipGroup.checkedChipId)
                val selectedColorChip = dialogView.findViewById<com.google.android.material.chip.Chip>(colorChipGroup.checkedChipId)

                if (name.isNotBlank() && type.isNotBlank()) {
                    val newCategory = Category(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        type = CategoryType.valueOf(type),
                        icon = when (selectedIconChip.id) {
                            R.id.iconSavings -> "ic_savings"
                            R.id.iconUtilities -> "ic_utilities"
                            R.id.iconEmergency -> "ic_error"
                            R.id.iconIncome -> "ic_income"
                            R.id.iconExpense -> "ic_expense"
                            else -> "ic_category"
                        },
                        color = when (selectedColorChip.id) {
                            R.id.colorBlue -> "#2196F3"
                            R.id.colorRed -> "#F44336"
                            R.id.colorPurple -> "#9C27B0"
                            R.id.colorOrange -> "#FF9800"
                            else -> "#4CAF50"
                        },
                        minBudget = min,
                        maxBudget = max,
                        isEditable = true
                    )
                    model.addCategory(newCategory)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    private fun showEditCategoryDialog(category: Category) {
        if (!category.isEditable) {
            Snackbar.make(binds.root, "This category cannot be edited", Snackbar.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val nameInput = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.nameInput)
        val typeInput = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.typeInput)
        val typeDropdown = typeInput.editText as? com.google.android.material.textfield.MaterialAutoCompleteTextView
        val iconChipGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.iconChipGroup)
        val colorChipGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.colorChipGroup)

        nameInput.editText?.setText(category.name)
        val types = CategoryType.values().filter { it != CategoryType.SAVINGS }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        typeDropdown?.setAdapter(adapter)
        typeDropdown?.setText(category.type.toString(), false)

        // Set current selections
        iconChipGroup.check(
            when (category.icon) {
                "ic_savings" -> R.id.iconSavings
                "ic_utilities" -> R.id.iconUtilities
                "ic_error" -> R.id.iconEmergency
                else -> R.id.iconCategory
            }
        )

        colorChipGroup.check(
            when (category.color) {
                "#2196F3" -> R.id.colorBlue
                "#F44336" -> R.id.colorRed
                "#9C27B0" -> R.id.colorPurple
                "#FF9800" -> R.id.colorOrange
                else -> R.id.colorGreen
            }
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Category")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.editText?.text.toString()
                val type = typeDropdown?.text.toString()
                val selectedIconChip = dialogView.findViewById<com.google.android.material.chip.Chip>(iconChipGroup.checkedChipId)
                val selectedColorChip = dialogView.findViewById<com.google.android.material.chip.Chip>(colorChipGroup.checkedChipId)

                if (name.isNotBlank() && type.isNotBlank()) {
                    val updatedCategory = category.copy(
                        name = name,
                        type = CategoryType.valueOf(type),
                        icon = when (selectedIconChip.id) {
                            R.id.iconSavings -> "ic_savings"
                            R.id.iconUtilities -> "ic_utilities"
                            R.id.iconEmergency -> "ic_error"
                            else -> "ic_category"
                        },
                        color = when (selectedColorChip.id) {
                            R.id.colorBlue -> "#2196F3"
                            R.id.colorRed -> "#F44336"
                            R.id.colorPurple -> "#9C27B0"
                            R.id.colorOrange -> "#FF9800"
                            else -> "#4CAF50"
                        }
                    )
                    model.updateCategory(updatedCategory)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupToolbar()
    }
} 