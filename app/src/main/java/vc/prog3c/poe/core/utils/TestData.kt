package vc.prog3c.poe.utils

import android.util.Log
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import vc.prog3c.poe.data.models.IncomeExpenseData
import vc.prog3c.poe.data.models.SavingsGoal
import vc.prog3c.poe.data.models.User
import vc.prog3c.poe.data.models.Budget
import vc.prog3c.poe.data.services.FirestoreService
import java.util.Date
import kotlin.random.Random

object TestData {

    fun runFirestoreTest() {
        val fakeUser = User(
            id = "testUser1234",
            email = "testdata@gmail.com",
            name = "Test Dev User",
            profilePictureUrl = ""
        )

        // Write test
        FirestoreService.users.addUser(fakeUser) { success ->
            Log.d("FIRESTORE_WRITE", "Write success? $success")
        }

        // Read test
        FirestoreService.users.getUser { user ->
            Log.d("FIRESTORE_READ", "Fetched user: $user")
        }
    }

    fun createAndInsertRandomUser() {
        // Generate a random username
        val randomSuffix = Random.nextInt(10000, 99999) // Random number between 10000 and 99999
        val randomUsername = "testUser$randomSuffix"
        val randomEmail = "test$randomSuffix@example.com"

        val newUser = User(
            id = randomUsername,
            email = randomEmail,
            name = "Random Test User",
            profilePictureUrl = ""
        )

        // Write the new user to Firestore
        FirestoreService.users.addUser(newUser) { success ->
            if (success) {
                Log.d("FIRESTORE_WRITE", "Random user created with ID: $randomUsername, Email: $randomEmail")
            } else {
                Log.e("FIRESTORE_WRITE", "Failed to create random user with ID: $randomUsername")
            }
        }
    }

    fun getTestIncomeExpenseData(): IncomeExpenseData {
        val totalIncome = 5000.0
        val totalExpenses = 3000.0

        val entries = listOf(
            PieEntry(totalIncome.toFloat(), "Income"),
            PieEntry(totalExpenses.toFloat(), "Expenses")
        )

        val dataSet = PieDataSet(entries, "Income vs Expenses")
        dataSet.colors = listOf(
            ColorTemplate.rgb("#4CAF50"),
            ColorTemplate.rgb("#F44336")
        )

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(ColorTemplate.rgb("#FFFFFF"))

        return IncomeExpenseData(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            pieData = pieData
        )
    }

    fun getTestSavingsGoals(): List<SavingsGoal> {
        return listOf(
            SavingsGoal(
                id = "test123",
                userId = "testUser1234",
                name = "My Goal",
                targetAmount = 10000.0,
                savedAmount = 3500.0,
                targetDate = Date(),
                minMonthlyGoal = 100.0,
                maxMonthlyGoal = 200.0,
                monthlyBudget = 150.0
            )
        )
    }

    fun getTestBudget(): Budget {
        return Budget(
            id = "budget1",
            month = "October",
            amount = 10000.0,
            spent = 1500.0
        )
    }
}