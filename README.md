
# 📘 BankBoosta App Documentation

---

## 🧭 In-depth Discussion on the Purpose of the App

Welcome to **BankBoosta** — your personal finance buddy right in your pocket!

In today’s fast-paced world, it's easy to lose track of our money. Between impulse buys, random subscriptions, and daily expenses, budgeting often feels like trying to tame a wild beast. That's where BankBoosta steps in.

**BankBoosta is built to:**
- ✅ Track your daily expenses effortlessly.
- 💰 Help you set and achieve savings goals.
- 📊 Offer real-time insights into your spending habits.

Whether you're a student budgeting for snacks and data, a young adult saving for a car, or anyone who wants more financial control — BankBoosta is here to empower you. It transforms budgeting from a chore into a clear, simple, and even enjoyable experience.

---

## 🎨 UI/UX & Backend Design Considerations

> Great design isn’t just about how things look — it’s about how they work.

### **UI/UX Design Highlights**
- 🎯 Follows Material Design principles for clean, familiar layouts.
- 🧠 Icons, progress bars, and friendly prompts for intuitive interaction.
- 🌈 Bright color schemes and clear fonts for maximum readability.
- ♿ Accessibility-first: designed for all users, regardless of ability.

### **Backend Architecture**
- ☁️ Powered by **Firebase Realtime Database** — lightweight, cloud-based, and instantly syncs data.
- 🛠️ Built with **Kotlin** using the **MVVM** (Model-View-ViewModel) architecture.
- 🧩 Designed to be **modular, scalable, and testable** — perfect for a growing app with evolving features.

In short: our design balances user joy with developer sanity.

---

## 🔧 GitHub Actions: Purpose and Implementation

Imagine a robot butler that keeps your codebase clean, your builds stable, and your team stress-free. That’s **GitHub Actions**.

**Every time someone pushes code:**
1. 📥 GitHub checks out the latest code.
2. ⚙️ Sets up the Java Development Kit (JDK) environment.
3. 🧪 Runs Gradle to build the project and check for issues.

**Benefits of this CI/CD pipeline:**
- 🐞 Bugs are caught early.
- ✅ Builds stay green.
- 🤝 Everyone on the team works in sync.

_Extra Bonus_: It saves time, reduces human error, and maintains a professional development pipeline.

---

## ✨ Extra Feature 1: Savings Goals Tracker

Ever wanted to save for something big? A PlayStation? A new phone? Maybe just a rainy day fund?

With **Savings Goals**, BankBoosta transforms your savings journey into a structured and rewarding experience.

### 🔹 Key Capabilities:
- 🎯 **Set a target amount** (e.g., R3000) and assign a goal name and deadline.
- 📆 **Define saving frequency** — daily, weekly, or monthly contributions.
- 📈 **Visual progress bar** that fills as you get closer to your goal.
- 🚨 **Spending alerts** notify you if your daily expenses start to jeopardize your savings path.
- 🧠 **AI-driven suggestions** (future implementation) help adjust your target based on changing income or expenses.

### 🔹 Why It Matters:
Most people struggle with long-term savings because there’s no immediate reward. BankBoosta gamifies the process with visual feedback, milestone notifications, and even celebratory animations when goals are met. This keeps users motivated and engaged over time.

---

## 📊 Extra Feature 2: Category-Based Spending Insights

Where does your money go every month? Do you actually know?

BankBoosta’s **Category-Based Spending Insights** feature provides deep visibility into how and where you spend — helping you make smarter financial decisions.

### 🔹 Key Capabilities:
- 🗂️ **Auto-categorization** of expenses using tags like Food, Transport, Entertainment, Utilities, and more.
- 📤 **Manual override** lets users reassign or split transactions between multiple categories.
- 📊 **Interactive charts and pie graphs** show monthly and weekly spending breakdowns.
- 🔁 **Trend tracking** compares spending across different timeframes (e.g., this month vs. last month).
- 🚨 **Anomaly detection** highlights unusual spikes in spending or duplicate transactions.

### 🔹 Why It Matters:
Understanding your financial behavior is the first step to improvement. This feature allows users to identify:
- Leaky areas like too many small coffee purchases.
- Unused subscriptions silently draining money.
- Seasonal or emotional spending patterns.

> _“You can’t improve what you don’t measure.”_ — BankBoosta ensures you measure everything.

---

## 📋 Lecture Feedback

This section outlines how we addressed the feedback received during the lecture session:

### ✅ Implemented Features

- **Budget Goals**  
  Users can now set **minimum and maximum budgeting goals** for specific categories, improving tracking and financial planning.

- **Time-Filtered Entries**  
  We added functionality to **filter entries by custom date ranges**, allowing users to view transactions within a selected period.

- **Category Totals by Period**  
  Users can now view **category-wise spending totals** for a specified time frame. This data is dynamically calculated and displayed in the summary view.

- **Transaction Tab Fix**  
  The **Transaction tab now properly displays all user data**. The bug preventing data display was resolved, and full functionality has been restored and demonstrated in the updated video.

### 🎥 Updated Demo

The updated walkthrough video now **clearly demonstrates app navigation**, especially how users can move between key features such as:

- Setting goals  
- Viewing reports  
- Managing transactions

This ensures a smoother user experience and highlights the functionality effectively.

### 🛠 Data Handling

We have **removed all placeholder or dummy values** from the UI. All displayed amounts now reflect **actual user-entered or stored data**, ensuring the app presents realistic, meaningful insights and avoids confusion during demonstrations.

---

## 👨‍💻 Contributors

This project wouldn't be possible without our brilliant team:

- **ST10257002**  
- **ST10293362**  
- **ST10326084**  
- **ST10187287**
