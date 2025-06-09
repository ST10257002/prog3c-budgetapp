package vc.prog3c.poe.data.models

import com.google.firebase.Timestamp

data class Achievement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: AchievementCategory = AchievementCategory.USER_MILESTONES,
    val boosterBucksReward: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Timestamp? = null,
    val progress: Int = 0,
    val requiredProgress: Int = 1
)

enum class AchievementCategory {
    USER_MILESTONES,
    CONSISTENCY_HABITS,
    SAVINGS_ACHIEVEMENTS,
    BUDGET_MANAGEMENT,
    FINANCIAL_INSIGHT,
    LEARNING_GROWTH
}

//object AchievementDefinitions {
//    val achievements = listOf(
//        // User Milestones
//        Achievement(
//            id = "first_budget",
//            title = "First Budget Created",
//            description = "Successfully created your first monthly budget",
//            category = AchievementCategory.USER_MILESTONES,
//            boosterBucksReward = 50
//        ),
//        Achievement(
//            id = "first_expense",
//            title = "First Expense Logged",
//            description = "Tracked your first expense",
//            category = AchievementCategory.USER_MILESTONES,
//            boosterBucksReward = 25
//        ),
//        Achievement(
//            id = "first_income",
//            title = "First Income Added",
//            description = "Logged your first income source",
//            category = AchievementCategory.USER_MILESTONES,
//            boosterBucksReward = 25
//        ),
//        Achievement(
//            id = "first_savings_goal",
//            title = "First Savings Goal Set",
//            description = "Created your first savings goal",
//            category = AchievementCategory.USER_MILESTONES,
//            boosterBucksReward = 50
//        ),
//        Achievement(
//            id = "linked_bank",
//            title = "Linked Bank Account",
//            description = "Connected your bank account for automatic syncing",
//            category = AchievementCategory.USER_MILESTONES,
//            boosterBucksReward = 100
//        ),
//
//        // Consistency & Habits
//        Achievement(
//            id = "daily_tracker",
//            title = "Daily Tracker",
//            description = "Logged expenses for 7 days in a row",
//            category = AchievementCategory.CONSISTENCY_HABITS,
//            boosterBucksReward = 75,
//            requiredProgress = 7
//        ),
//        Achievement(
//            id = "weekly_warrior",
//            title = "Weekly Warrior",
//            description = "Used the app every week for a month",
//            category = AchievementCategory.CONSISTENCY_HABITS,
//            boosterBucksReward = 100,
//            requiredProgress = 4
//        ),
//        Achievement(
//            id = "budget_streak",
//            title = "Budget Streak",
//            description = "Maintained a budget for 3 consecutive months",
//            category = AchievementCategory.CONSISTENCY_HABITS,
//            boosterBucksReward = 150,
//            requiredProgress = 3
//        ),
//        Achievement(
//            id = "habitual_saver",
//            title = "Habitual Saver",
//            description = "Added to savings every week for a month",
//            category = AchievementCategory.CONSISTENCY_HABITS,
//            boosterBucksReward = 100,
//            requiredProgress = 4
//        ),
//        Achievement(
//            id = "on_time_logger",
//            title = "On-Time Logger",
//            description = "Logged expenses on the same day for 14 days in a row",
//            category = AchievementCategory.CONSISTENCY_HABITS,
//            boosterBucksReward = 100,
//            requiredProgress = 14
//        ),
//
//        // Savings Achievements
//        Achievement(
//            id = "first_goal_reached",
//            title = "First Goal Reached",
//            description = "Reached your first savings goal",
//            category = AchievementCategory.SAVINGS_ACHIEVEMENTS,
//            boosterBucksReward = 100
//        ),
//        Achievement(
//            id = "savings_streak",
//            title = "Savings Streak",
//            description = "Saved money consistently for 3 months",
//            category = AchievementCategory.SAVINGS_ACHIEVEMENTS,
//            boosterBucksReward = 150,
//            requiredProgress = 3
//        ),
//        Achievement(
//            id = "emergency_fund",
//            title = "Emergency Fund Builder",
//            description = "Reached an emergency fund target",
//            category = AchievementCategory.SAVINGS_ACHIEVEMENTS,
//            boosterBucksReward = 200
//        ),
//        Achievement(
//            id = "big_saver",
//            title = "Big Saver",
//            description = "Saved over $1,000 (or regional currency equivalent)",
//            category = AchievementCategory.SAVINGS_ACHIEVEMENTS,
//            boosterBucksReward = 250
//        ),
//        Achievement(
//            id = "debt_destroyer",
//            title = "Debt Destroyer",
//            description = "Paid off a tracked debt completely",
//            category = AchievementCategory.SAVINGS_ACHIEVEMENTS,
//            boosterBucksReward = 200
//        ),
//
//        // Budget Management
//        Achievement(
//            id = "under_budget",
//            title = "Under Budget",
//            description = "Stayed under budget for the month",
//            category = AchievementCategory.BUDGET_MANAGEMENT,
//            boosterBucksReward = 100
//        ),
//        Achievement(
//            id = "expense_cutter",
//            title = "Expense Cutter",
//            description = "Reduced a spending category by 20% month-over-month",
//            category = AchievementCategory.BUDGET_MANAGEMENT,
//            boosterBucksReward = 150
//        ),
//        Achievement(
//            id = "zero_based_budgeter",
//            title = "Zero-Based Budgeter",
//            description = "Assigned every dollar a job",
//            category = AchievementCategory.BUDGET_MANAGEMENT,
//            boosterBucksReward = 100
//        ),
//        Achievement(
//            id = "category_master",
//            title = "Category Master",
//            description = "Added custom categories to match spending style",
//            category = AchievementCategory.BUDGET_MANAGEMENT,
//            boosterBucksReward = 50
//        ),
//        Achievement(
//            id = "flexible_financier",
//            title = "Flexible Financier",
//            description = "Adjusted budget mid-month to reflect changes",
//            category = AchievementCategory.BUDGET_MANAGEMENT,
//            boosterBucksReward = 75
//        ),
//
//        // Financial Insight
//        Achievement(
//            id = "top_spender",
//            title = "Top Spender Revealed",
//            description = "Identified your biggest spending category",
//            category = AchievementCategory.FINANCIAL_INSIGHT,
//            boosterBucksReward = 50
//        ),
//        Achievement(
//            id = "trends_analyst",
//            title = "Trends Analyst",
//            description = "Viewed your spending trends over 3 months",
//            category = AchievementCategory.FINANCIAL_INSIGHT,
//            boosterBucksReward = 100
//        ),
//        Achievement(
//            id = "report_reader",
//            title = "Report Reader",
//            description = "Generated and reviewed a monthly report",
//            category = AchievementCategory.FINANCIAL_INSIGHT,
//            boosterBucksReward = 75
//        ),
//        Achievement(
//            id = "financial_forecaster",
//            title = "Financial Forecaster",
//            description = "Used projected income and expenses",
//            category = AchievementCategory.FINANCIAL_INSIGHT,
//            boosterBucksReward = 100
//        ),
//        Achievement(
//            id = "net_worth_tracker",
//            title = "Net Worth Tracker",
//            description = "Tracked your net worth in-app",
//            category = AchievementCategory.FINANCIAL_INSIGHT,
//            boosterBucksReward = 75
//        ),
//
//        // Learning & Growth
//        Achievement(
//            id = "budgeting_beginner",
//            title = "Budgeting Beginner",
//            description = "Completed onboarding or tutorial",
//            category = AchievementCategory.LEARNING_GROWTH,
//            boosterBucksReward = 50
//        ),
//        Achievement(
//            id = "finance_buff",
//            title = "Finance Buff",
//            description = "Completed all in-app learning modules",
//            category = AchievementCategory.LEARNING_GROWTH,
//            boosterBucksReward = 150
//        ),
//        Achievement(
//            id = "goal_setter",
//            title = "Goal Setter",
//            description = "Created 3 or more financial goals",
//            category = AchievementCategory.LEARNING_GROWTH,
//            boosterBucksReward = 100,
//            requiredProgress = 3
//        ),
//        Achievement(
//            id = "smart_spender",
//            title = "Smart Spender",
//            description = "Reallocated budget based on priorities",
//            category = AchievementCategory.LEARNING_GROWTH,
//            boosterBucksReward = 75
//        ),
//        Achievement(
//            id = "financially_fit",
//            title = "Financially Fit",
//            description = "Met all monthly budget goals",
//            category = AchievementCategory.LEARNING_GROWTH,
//            boosterBucksReward = 200
//        )
//    )
//}