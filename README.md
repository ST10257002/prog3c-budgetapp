
# 📘 BankBoosta App Documentation

---

## 🧭 In-depth Discussion on the Purpose of the App

Welcome to **BankBoosta** — your smart, user-friendly personal finance assistant, designed with real users and real challenges in mind.

In an era of fast digital transactions, contactless payments, online shopping, and automatic subscriptions, many people — especially students and young professionals — find it increasingly difficult to keep track of their money. The lack of visibility into where money goes leads to overspending, anxiety, and difficulty reaching savings goals. Traditional budgeting methods (spreadsheets, pen-and-paper, or complex software) are often too inconvenient or intimidating for everyday use.

---

### 🎯 What BankBoosta Aims to Solve

BankBoosta addresses these modern financial challenges by offering an intuitive mobile budgeting app tailored for day-to-day usability. It's not just another finance app; it’s a tool that actively promotes healthy financial behavior by making budgeting approachable, visual, and goal-oriented.

---

### 🧠 Core Philosophies Behind BankBoosta

- **Simplicity**  
  BankBoosta is designed to be easy enough for anyone to use without training or financial knowledge. It features a clean interface, quick inputs, and immediate feedback on spending.

- **Transparency**  
  By categorizing every transaction and showing summaries, the app removes the guesswork — users see where every cent goes.

- **Empowerment through Awareness**  
  With visual tools like charts and summaries, users gain a clear understanding of their financial habits. Knowledge is power, and BankBoosta puts that power into their hands.

---

### 🧰 Key Capabilities

- ✅ **Track Daily Expenses Effortlessly**  
  Just a few taps allow users to log expenses on-the-go. Add notes, categorize transactions (e.g., Food, Transport, Entertainment), and even view location-based entries (if supported).

- 💰 **Set and Achieve Financial Goals**  
  Whether you're saving for a new phone, a trip, or just trying to build a buffer for emergencies — BankBoosta enables users to define savings goals, allocate funds, and track progress visually.

- 📊 **Real-time Insights and Visual Analytics**  
  The app generates clear charts showing spending over time, by category, or relative to income. This helps users spot overspending habits and make smarter decisions.

- 🔔 **Smart Alerts and Budget Reminders**  
  Friendly nudges remind users when they're nearing budget limits, or when it’s time to log recent expenses — keeping them engaged and proactive.

- 📅 **Monthly Budgets & Rollovers**  
  Users can set monthly limits for various categories and carry over unused budget portions into the next month, promoting continuity and smart planning.

---

### 👤 Who Is It For?

BankBoosta is designed with inclusivity in mind. Whether you're:

- A **student** budgeting for lunch, mobile data, and transport  
- A **young adult** managing rent, debt, and savings goals  
- A **family** looking to organize shared spending  
- Or even a **small business owner** monitoring basic business expenses  

…BankBoosta offers the flexibility and simplicity to fit into any lifestyle.

---

### 🧩 Why BankBoosta is Different

Unlike many apps that require bank integrations, complex setup, or paid features to access basic tools, **BankBoosta is self-contained, lightweight, and private**. It works offline and respects user data, making it especially appealing in regions with limited internet access or privacy concerns.

Moreover, it’s built by students, for students — meaning every feature is grounded in real, relatable use cases and daily budget challenges.

---

### 🌍 The Bigger Picture

BankBoosta also promotes financial literacy. In environments where personal finance isn’t taught, this app serves as a digital mentor — encouraging users to reflect, save, and plan.

Planned future enhancements may include:

- 📚 Educational tips and quizzes  
- 📉 Debt tracking  
- 📈 Investment simulations  
- 🤝 Community savings goals (e.g., saving together with friends or clubs)

---

### 💡 Final Thought

In short, **BankBoosta transforms budgeting from a boring chore into a motivating habit**. It’s not just an app — it’s a financial companion that helps users take control, stay organized, and ultimately build a better financial future, one transaction at a time.


---


## 🎨 UI/UX & Backend Design Considerations

> Great design isn’t just about how things look — it’s about how they work.

BankBoosta was designed with a dual emphasis on visual clarity and technical performance. Its design philosophy combines modern mobile UI standards with robust architectural practices to deliver an experience that is both user-friendly and future-ready.

---

### 🎨 **UI/UX Design Highlights**

User Interface (UI) and User Experience (UX) form the heart of BankBoosta’s usability strategy. The app’s front-end is guided by principles that ensure intuitive use, inclusivity, and visual consistency:

- **🎯 Material Design Principles**  
  The layout and component design follow Google’s Material Design guidelines, ensuring consistency across Android devices. Elements like floating action buttons, bottom navigation bars, and snackbars are used to create a seamless and familiar user experience.

- **🧠 Visual Feedback Elements**  
  The app uses iconography, progress indicators, dynamic cards, and friendly prompts to reduce cognitive load and increase engagement. For example, savings goals are represented with visually engaging progress bars, helping users intuitively understand their progress at a glance.

- **🌈 Readability & Aesthetic Balance**  
  The color palette balances vibrancy with usability — using contrast effectively to highlight important information (like overspending alerts or goal completion). Typography choices emphasize clarity and consistency, supporting both small and large screens.

- **♿ Accessibility-First Approach**  
  BankBoosta was developed with accessibility in mind, adhering to best practices such as proper contrast ratios, touch-friendly targets, and support for screen readers. This ensures that users with visual or motor impairments can comfortably navigate and utilize the app.

- **📱 Responsive Design**  
  The UI scales gracefully across a wide range of screen sizes and resolutions, ensuring functionality on both low-end devices and modern high-resolution smartphones.

---

### 🏗️ **Backend Architecture**

Behind the user-friendly frontend lies a solid, well-structured backend. BankBoosta's architecture was designed to prioritize performance, maintainability, and scalability — allowing the application to evolve over time as user needs grow.

- **☁️ Firebase Realtime Database**  
  A key component of the backend is the integration with **Firebase Realtime Database**, a cloud-hosted NoSQL database that allows for real-time data syncing across devices. This provides:
  - Instantaneous data updates across users and sessions
  - Lightweight network usage — ideal for mobile environments
  - Offline persistence for reliability in low-connectivity areas

- **🛠️ Kotlin + MVVM Architecture**  
  The app is developed using **Kotlin**, the modern and officially supported language for Android development. Kotlin's expressive syntax, null safety, and coroutine support make it ideal for building robust Android apps.

  The architectural pattern used is **MVVM (Model-View-ViewModel)**, which separates concerns cleanly:
  - **Model:** Manages app data and business logic
  - **View:** Displays the data and interacts with the user
  - **ViewModel:** Acts as a communication bridge, holding UI-related data that survives configuration changes (like screen rotations)

- **🧩 Modularity and Scalability**  
  Each feature of BankBoosta — such as expense tracking, goal setting, and analytics — is implemented in a modular fashion. This allows the codebase to remain organized and enables independent testing and extension.

- **🔒 Security & Data Handling**  
  Using Firebase Authentication and secure rules, user data is isolated and protected. Sensitive information is stored with access control policies, and network communications use HTTPS by default.

- **📦 Dependency Management**  
  Build and dependency configuration is handled via **Gradle (build.gradle.kts)**. External libraries (like Firebase, JitPack packages) are version-controlled and declared explicitly for maintainability.

- **🔍 Debugging & Performance Monitoring**  
  Firebase Crashlytics and Performance Monitoring are integrated for:
  - Real-time crash reports
  - Detailed traces of app performance
  - Detection of slow operations or long screen rendering times

---

### 🚀 Ready for the Future

With a clean architecture and user-first design, BankBoosta is structured to evolve. Planned improvements include integration of machine learning for spending predictions, user personalization, and AI-driven financial recommendations — all building on the strong design foundations described above.


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
---
# 📚 References

This section includes all the external resources, libraries, tools, and platforms used in the development of the **BankBoosta – prog3c-budgetapp** Android application, formatted in Harvard style.

1. **ST10257002, ST10293362, ST10326084, and ST10187287** (2025) *BankBoosta – prog3c-budgetapp*. GitHub. Available at: [https://github.com/ST10257002/prog3c-budgetapp](https://github.com/ST10257002/prog3c-budgetapp) (Accessed: 14 May 2025).

2. **JetBrains** (n.d.) *Kotlin programming language*. Available at: [https://kotlinlang.org/](https://kotlinlang.org/) (Accessed: 5 April 2025).  
   → Kotlin is the primary programming language used for building Android apps in this project.

3. **Android Developers** (n.d.) *Android Studio and SDK tools*. Google. Available at: [https://developer.android.com/studio](https://developer.android.com/studio) (Accessed: 13 May 2025).  
   → The app is built using Android Studio with Gradle and the Android SDK.

4. **Gradle Inc.** (n.d.) *Gradle Build Tool*. Available at: [https://gradle.org/](https://gradle.org/) (Accessed: 9 June 2025).  
   → Used for build automation and dependency management (`build.gradle.kts`, `settings.gradle.kts`).

5. **Google** (n.d.) *Firebase Crashlytics*. Firebase. Available at: [https://firebase.google.com/products/crashlytics](https://firebase.google.com/products/crashlytics) (Accessed: 1 June 2025).  
   → Integrated for real-time crash reporting and debugging (`com.google.firebase.crashlytics` plugin).

6. **Google** (n.d.) *Firebase Performance Monitoring*. Firebase. Available at: [https://firebase.google.com/products/performance](https://firebase.google.com/products/performance) (Accessed: 2 June 2025).  
   → Monitors app performance using the `com.google.firebase.firebase-perf` plugin.

7. **Google** (n.d.) *Firebase for Android*. Firebase. Available at: [https://firebase.google.com/docs/android/setup](https://firebase.google.com/docs/android/setup) (Accessed: 2 June 2025).  
   → Indicates overall Firebase integration via `google-services.json` and related plugins.

8. **GitHub** (n.d.) *JitPack Repository*. Available at: [https://jitpack.io/](https://jitpack.io/) (Accessed: 7 June 2025).  
   → Declared in `settings.gradle.kts` for third-party library integration.

9. **IntelliJ IDEA** (n.d.) *AndroidProjectSystem*. JetBrains. Available at: [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/) (Accessed: 6 June 2025).  
   → The project structure shows use of IntelliJ-based IDE features through `.idea` config files.

