// GraphView.kt
package vc.prog3c.poe.ui.views

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vc.prog3c.poe.R
import vc.prog3c.poe.data.models.GraphBarEntry
import vc.prog3c.poe.databinding.ActivityGraphBinding
import vc.prog3c.poe.ui.viewmodels.GraphViewModel

class GraphView : AppCompatActivity() {

    private lateinit var binds: ActivityGraphBinding
    private lateinit var model: GraphViewModel
    private var selectedAccountId: String? = null

    private val rangeOptions = listOf("24 Hours" to 1, "7 Days" to 7, "14 Days" to 14, "30 Days" to 30)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binds = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binds.root)

        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        model = ViewModelProvider(this)[GraphViewModel::class.java]

        setupToolbar()
        setupTimeRangeSpinner()
        setupAccountSpinner()
        setupBarChart()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binds.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Spending by Category"
    }

    private fun setupTimeRangeSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rangeOptions.map { it.first })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binds.timeRangeSpinner.adapter = adapter
        binds.timeRangeSpinner.setSelection(1)

        binds.timeRangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedAccountId?.let {
                    model.loadGraphData(it, rangeOptions[position].second)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupAccountSpinner() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("accounts")
            .get()
            .addOnSuccessListener { documents ->
                val accountList = documents.mapNotNull {
                    val name = it.getString("name") ?: return@mapNotNull null
                    it.id to name
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountList.map { it.second })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binds.accountSpinner.adapter = adapter

                binds.accountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedAccountId = accountList[position].first
                        val days = rangeOptions[binds.timeRangeSpinner.selectedItemPosition].second
                        model.loadGraphData(selectedAccountId!!, days)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                if (accountList.isNotEmpty()) {
                    selectedAccountId = accountList.first().first
                    model.loadGraphData(selectedAccountId!!, 7)
                }
            }
    }

    private fun setupBarChart() {
        binds.barChart.apply {
            description.isEnabled = false
            axisRight.isEnabled = false
            setDrawGridBackground(false)
            setFitBars(true)
            animateY(1000)
            legend.isEnabled = true
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
        }
    }

    private fun observeViewModel() {
        model.graphData.observe(this) { updateChart(it) }
        model.isLoading.observe(this) {
            binds.swipeRefreshLayout.isRefreshing = it
            binds.loadingProgressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
        model.error.observe(this) {
            it?.let { msg -> Snackbar.make(binds.root, msg, Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun updateChart(entries: List<GraphBarEntry>) {
        if (entries.isEmpty()) {
            binds.barChart.clear()
            binds.barChart.invalidate()
            return
        }

        val barEntries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val colors = mutableListOf<Int>()

        var maxY = 0f

        entries.forEachIndexed { index, entry -> // ✅ fixed name
            val value = entry.totalSpent.toFloat()
            barEntries.add(BarEntry(index.toFloat(), value))
            labels.add(entry.category)

            maxY = maxOf(maxY, value, entry.maxGoal.toFloat())

            val color = when {
                value > entry.maxGoal -> getColor(R.color.red)
                value > entry.minGoal -> getColor(R.color.yellow)
                else -> getColor(R.color.green)
            }
            colors.add(color)
        }

        val dataSet = BarDataSet(barEntries, "Spent per Category").apply {
            setColors(colors)
            valueTextSize = 14f
            valueTextColor = getColor(R.color.black)
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.4f
        }

        binds.barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelRotationAngle = -30f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.textSize = 12f

            axisLeft.apply {
                removeAllLimitLines()
                textSize = 12f
                setDrawGridLines(true)
                axisMaximum = maxY * 1.2f

                entries.forEachIndexed { index, entry -> // ✅ use renamed param
                    if (entry.minGoal > 0) {
                        val minLine = LimitLine(entry.minGoal.toFloat(), "Min: ${entry.minGoal}").apply {
                            lineColor = getColor(R.color.blue)
                            lineWidth = 1.5f
                            enableDashedLine(10f, 5f, 0f)
                            textColor = getColor(R.color.blue)
                            textSize = 10f
                        }
                        addLimitLine(minLine)
                    }

                    if (entry.maxGoal > 0) {
                        val maxLine = LimitLine(entry.maxGoal.toFloat(), "Max: ${entry.maxGoal}").apply {
                            lineColor = getColor(R.color.red)
                            lineWidth = 1.5f
                            enableDashedLine(10f, 5f, 0f)
                            textColor = getColor(R.color.red)
                            textSize = 10f
                        }
                        addLimitLine(maxLine)
                    }
                }
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false

            animateY(1000)
            invalidate()
        }
    }





    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
