package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
        val nameInput =
            dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.nameInput)
        val typeInput =
            dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.typeInput)
        val typeDropdown =
            typeInput.editText as? com.google.android.material.textfield.MaterialAutoCompleteTextView

        val types = CategoryType.values().filter { it != CategoryType.SAVINGS }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        typeDropdown?.setAdapter(adapter)

        MaterialAlertDialogBuilder(this).setTitle("Add Category").setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.editText?.text.toString()
                val type = typeDropdown?.text.toString()
                if (name.isNotBlank() && type.isNotBlank()) {
                    val newCategory = Category(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        type = CategoryType.valueOf(type),
                        icon = "ic_category",
                        color = "#FF000000",
                        isEditable = true
                    )
                    model.addCategory(newCategory)
                }
            }.setNegativeButton("Cancel", null).show()
    }
    

    private fun showEditCategoryDialog(category: Category) {
        if (!category.isEditable) {
            Snackbar.make(binds.root, "This category cannot be edited", Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val nameInput =
            dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.nameInput)
        val typeInput =
            dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.typeInput)
        val typeDropdown =
            typeInput.editText as? com.google.android.material.textfield.MaterialAutoCompleteTextView

        nameInput.editText?.setText(category.name)
        val types = CategoryType.values().filter { it != CategoryType.SAVINGS }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types)
        typeDropdown?.setAdapter(adapter)
        typeDropdown?.setText(category.type.toString(), false)

        MaterialAlertDialogBuilder(this).setTitle("Edit Category").setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.editText?.text.toString()
                val type = typeDropdown?.text.toString()
                if (name.isNotBlank() && type.isNotBlank()) {
                    val updatedCategory = category.copy(
                        name = name, type = CategoryType.valueOf(type)
                    )
                    model.updateCategory(updatedCategory)
                }
            }.setNegativeButton("Cancel", null).show()
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
            onDeleteClick = { category -> showDeleteConfirmationDialog(category) })
        binds.recyclerView.adapter = adapter
    }


    // --- UI


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
        
        setupRecyclerView()
        setupToolbar()
    }
} 