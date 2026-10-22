# Lost&Found

**Problem statement:**
Students lose items on campus and there is no centralized place to report or search for them, which means many items never return to their owners.

**Team:**
- Abel Cardoso (All roles: PO, Tech Lead, Developer, QA/DevOps)

**MVP Features:**
- [Sprint 1] User login and registration (Firebase Auth)
- [Sprint 1] Create a lost/found item ad
- [Sprint 1] See a list of all items
- [Sprint 1] View details and edit/delete own items
- [Later] Notify users when a similar item is found 

**Data Model (Firestore):**
- `users`: uid (string), email (string), displayName (string), createdAt (timestamp)
- `items`: id (string), ownerId (string), title (string), description (string), location (string), type (string), createdAt (timestamp)

**Architecture:**
```mermaid
flowchart LR
    A[Android app<br/>Kotlin + Compose] -->|Auth SDK| B[Firebase Authentication]
    A -->|Firestore SDK| C[(Cloud Firestore)]
    D[Cloud Functions<br/>Lab 3] --> C
