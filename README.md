# 💸 BankBoosta

---

## 🛝 In-depth Discussion on the Purpose of the App

Welcome to **BankBoosta** — your smart, user-friendly personal finance assistant, designed with real users and real challenges in mind.

In an era of fast digital transactions, contactless payments, online shopping, and automatic subscriptions, many people — especially students and young professionals — find it increasingly difficult to keep track of their money. The lack of visibility into where money goes leads to overspending, anxiety, and difficulty reaching savings goals. Traditional budgeting methods (spreadsheets, pen-and-paper, or complex software) are often too inconvenient or intimidating for everyday use.

---

### 🎯 What BankBoosta Aims to Solve

BankBoosta addresses these modern financial challenges by offering an intuitive mobile budgeting app tailored for day-to-day usability. It's not just another finance app; it’s a tool that actively promotes healthy financial behavior by making budgeting approachable, visual, and goal-oriented.

---

### 🧠 Core Philosophies Behind BankBoosta

* **Simplicity**
  BankBoosta is designed to be easy enough for anyone to use without training or financial knowledge. It features a clean interface, quick inputs, and immediate feedback on spending.

* **Transparency**
  By categorizing every transaction and showing summaries, the app removes the guesswork — users see where every cent goes.

* **Empowerment through Awareness**
  With visual tools like charts and summaries, users gain a clear understanding of their financial habits. Knowledge is power, and BankBoosta puts that power into their hands.

---

### 🧰 Key Capabilities

* ✅ **Track Daily Expenses Effortlessly**
  Just a few taps allow users to log expenses on-the-go. Add notes, categorize transactions (e.g., Food, Transport, Entertainment), and even attach receipts with images.

* 💰 **Set and Achieve Financial Goals**
  Whether you're saving for a new phone, a trip, or just building an emergency buffer — BankBoosta lets users define savings goals, allocate funds, and track progress visually.

* 📊 **Real-time Insights and Visual Analytics**
  The app displays clear charts showing spending over time, by category, or compared to budget targets.

* 🔔 **Smart Alerts and Budget Reminders**
  Friendly nudges notify users when nearing limits or forgetting to log expenses.

* 🗕️ **Monthly Budgets & Rollovers**
  Set flexible category budgets and carry unused budget into the next month to support continuity.

---

### 👤 Who Is It For?

BankBoosta is designed with inclusivity in mind. Whether you're:

* A **student** budgeting for food, data, and transport
* A **young adult** managing rent, savings, and debt
* A **family** organizing household expenses
* A **freelancer** or **small business owner** tracking basic operations

...BankBoosta adapts to fit your needs.

---

### 🧹 Why BankBoosta is Different

Unlike many finance apps that require bank integration or subscriptions, **BankBoosta is self-contained, private, and offline-friendly**. It’s lightweight, beginner-friendly, and respects user data.

And it’s built by students, for students — so every feature is relevant and relatable.

---

### 🌍 The Bigger Picture

BankBoosta promotes financial literacy in a digital, gamified format. Future updates may include:

* 📚 Educational quizzes and financial tips
* 📉 Debt tracking and payoff plans
* 📈 Investment simulation tools
* 🤝 Community saving features (group pots)

---

### 💡 Final Thought

**BankBoosta transforms budgeting from a boring chore into a motivating habit.**
It’s not just an app — it’s your financial partner for peace of mind, one transaction at a time.

---

## 🎨 UI/UX & Backend Design Considerations

> Great design isn’t just about how things look — it’s about how they work.

BankBoosta is built for both clarity and performance, combining clean UX with a powerful backend.

---

### 🎨 UI/UX Design Highlights

* **🎯 Material Design** — Intuitive layout with familiar Android design principles
* **🔹 Visual Feedback** — Uses graphs, animated goals, and dynamic chips
* **🌈 Accessibility** — Screen reader support, color contrast, large touch targets
* **📱 Responsive Layout** — Works across all screen sizes and resolutions

---

### 🏗️ Backend Architecture

* **🔥 Firebase Firestore**
  The app now uses **Cloud Firestore**, offering scalable, real-time data storage:

  * Offline-first design for low-data users
  * Live syncing across devices
  * Firestore rules for secure, per-user data access

* **🛠️ Kotlin + MVVM**
  Codebase structured around MVVM for maintainability and separation of concerns:

  * Models for transaction data, users, and goals
  * Repositories for Firestore access and abstraction
  * ViewModels for lifecycle-safe UI logic

* **🛠️ Modular & Scalable**
  Features are encapsulated into modules: transactions, goals, dashboard, etc.

* **🔒 Secure Auth & Rules**
  FirebaseAuth ensures isolated data per user. HTTPS + Firestore rules secure reads/writes.

* **📆 CI/CD with GitHub Actions**
  Automated tests and builds ensure quality with each push.

---

## 🔧 GitHub Actions: Purpose and Implementation

* Runs tests and builds via Gradle on every code push
* Prevents regressions and bad merges
* Helps maintain a clean, stable codebase

---

## ✨ Extra Feature 1: Savings Goals Tracker

* Set a name, amount, and deadline
* Track saving frequency and progress visually
* Receive alerts when expenses threaten progress
* Future: AI suggestions and shared group savings

---

## 📊 Extra Feature 2: Category Insights

* View expenses by tag (e.g., Transport, Emergency, Groceries)
* Pie/bar graphs show category spending visually
* Detect trends and overspending patterns across time

---

## 🧩 Extra Feature 3: Additional Enhancements

* 🧾 Accounts Management — Support for multiple accounts (e.g., Wallet, Bank, Savings) with isolated transaction history and totals.
* 📊 Graphs Everywhere — Most views include real-time graphs and visual breakdowns to improve user understanding and engagement.
* 🔐 Biometric Authentication — Optional fingerprint login for faster, secure access.
* 👤 Profile Page — Centralized view for account settings, budget goals, and user information.
* 📸 Transaction Attachments — Ability to attach receipt or photo proof to each transaction.
* 📂 Dynamic Filtering — Filter transactions by account, category, date, or type.
* Seed Data - Click a button to recieve testing data without manually entering any information

---

## 🔧 Setup Instructions

1. Clone the repo  
2. Open in Android Studio Iguana or newer  
3. Make sure to sync Gradle  
4. Run on an emulator or physical device

---

## 📋 Lecture Feedback Summary

* ✅ Min/max budgets per category added
* ✅ Transaction filtering by date range
* ✅ Totals per category shown in graphs
* ✅ Bug fixed: Transactions tab now loads properly
* ✅ All fake data removed; live Firestore now used throughout

---

## 👨‍💻 Contributors

This project wouldn't be possible without our brilliant team:

* **ST10257002**
* **ST10293362**
* **ST10326084**
* **ST10187287**

---

## 📺 Youtube Walkthrough and Github Link

-add here

* https://github.com/ST10293362/PROG7313_POE_BudgetApp.git

--- 
# 📚 References

This section includes all the external resources, libraries, tools, and platforms used in the development of the **BankBoosta – prog3c-budgetapp** Android application, formatted in Harvard style.

1. **ST10257002, ST10293362, ST10326084, and ST10187287** (2025) *BankBoosta – prog3c-budgetapp*. GitHub. Available at: [https://github.com/ST10257002/prog3c-budgetapp](https://github.com/ST10257002/prog3c-budgetapp) (Accessed: 14 May 2025).

2. **JetBrains** (n.d.) *Kotlin programming language*. Available at: [https://kotlinlang.org/](https://kotlinlang.org/) (Accessed: 5 April 2025).  
   → Kotlin is the primary programming language used for building Android apps in this project.

3. **Android Developers** (n.d.) *Android Studio and SDK tools*. Google. Available at: [https://developer.android.com/studio](https://developer.android.com/studio) (Accessed: 13 May 2025).  
   → The app is built using Android Studio with Gradle and the Android SDK.

4. **Gradle Inc.** (n.d.) *Gradle Build Tool*. Available at: [https://gradle.org/](https://gradle.org/) (Accessed: 9 June 2025).  
   → Used for build automation and dependency management (build.gradle.kts, settings.gradle.kts).

5. **Google** (n.d.) *Firebase Crashlytics*. Firebase. Available at: [https://firebase.google.com/products/crashlytics](https://firebase.google.com/products/crashlytics) (Accessed: 1 June 2025).  
   → Integrated for real-time crash reporting and debugging (com.google.firebase.crashlytics plugin).

6. **Google** (n.d.) *Firebase Performance Monitoring*. Firebase. Available at: [https://firebase.google.com/products/performance](https://firebase.google.com/products/performance) (Accessed: 2 June 2025).  
   → Monitors app performance using the com.google.firebase.firebase-perf plugin.

7. **Google** (n.d.) *Firebase for Android*. Firebase. Available at: [https://firebase.google.com/docs/android/setup](https://firebase.google.com/docs/android/setup) (Accessed: 2 June 2025).  
   → Indicates overall Firebase integration via google-services.json and related plugins.

8. **GitHub** (n.d.) *JitPack Repository*. Available at: [https://jitpack.io/](https://jitpack.io/) (Accessed: 7 June 2025).  
   → Declared in settings.gradle.kts for third-party library integration.

9. **IntelliJ IDEA** (n.d.) *AndroidProjectSystem*. JetBrains. Available at: [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/) (Accessed: 6 June 2025).  
   → The project structure shows use of IntelliJ-based IDE features through .idea config files.
