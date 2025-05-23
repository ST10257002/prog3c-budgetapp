package vc.prog3c.poe.data

import vc.prog3c.poe.data.repository.*

object FirestoreService {
    val expenses = ExpenseRepository()
    val incomes = IncomeRepository()
    val categories = CategoryRepository()
    val users = UserRepository()
}

//Example on how we could use this
// FirestoreService.expenses.getAllExpenses { ... }



/*
Matt, need help creating the actual firebase firstore DB
https://youtu.be/QcsAb2RR52c

STEP 1
In the left menu, go to "Build" → Firestore Database.

Click "Create database".

Choose a starting security mode:

🔒 Production Mode: access only for authenticated users (recommended).

🔓 Test Mode: open access (temporary for quick testing).

Select a region (ideally close to your users or team).

Click "Enable".

STEP 2
users (collection)
 └── {userId} (document)
      ├── expenses (collection)
      ├── incomes (collection)
      ├── categories (collection)
      └── [user profile fields]

STEP 3
Go to Firestore → Rules and use something like:

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Allow users to read/write only their own data
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;

      match /{document=**} {
        allow read, write: if request.auth.uid == userId;
      }
    }
  }
}

THEN CHECK AUTHENTICATION
 */