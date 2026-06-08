# Backend API Specifications for Profile Settings Page

This document defines the new/additional backend REST API endpoints required to make the user profile page fully functional.

All endpoints require authorization using a JWT bearer token.

---

## 1. Global Headers & Configuration

Every request to these endpoints must include the following headers:

| Header Name | Value / Format | Description |
| :--- | :--- | :--- |
| `Authorization` | `Bearer <JWT_ACCESS_TOKEN>` | User authentication token |
| `Content-Type` | `application/json` | Required for all request payloads (`POST`, `PUT`, `PATCH`) |

---

## 2. User Preferences API

Manages user-specific preferences such as UI language and default payment modes.

### 2.1. Get User Preferences
Fetches the current user's profile and preferences.

* **URL**: `/api/user/preferences`
* **Method**: `GET`
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "language": "English",
  "defaultPaymentModeId": 1
}
```

### 2.2. Update User Preferences
Updates the current user's preferences (e.g., language selection or default payment mode).

* **URL**: `/api/user/preferences`
* **Method**: `PATCH`
* **Request Body**:
```json
{
  "language": "Hindi",
  "defaultPaymentModeId": 2
}
```
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "language": "Hindi",
  "defaultPaymentModeId": 2
}
```

---

## 3. Accounts API

Manages the list of linked bank accounts, credit cards, or cash accounts.

### 3.1. Get Linked Accounts
Retrieves the user's linked bank accounts and credit cards.

* **URL**: `/api/account`
* **Method**: `GET`
* **Response Status**: `200 OK`
* **Response Body**:
```json
[
  {
    "id": "1",
    "bankName": "HDFC Bank",
    "lastFour": "4321",
    "type": "Savings",
    "amount": 25000.50
  },
  {
    "id": "2",
    "bankName": "ICICI Credit Card",
    "lastFour": "9876",
    "type": "Credit",
    "amount": 100000.00
  },
  {
    "id": "3",
    "bankName": "Wallet Cash",
    "lastFour": "0000",
    "type": "Cash",
    "amount": 1500.00
  }
]
```

### 3.2. Link a New Account
Links a new bank account or credit card to the user's profile.

* **URL**: `/api/account`
* **Method**: `POST`
* **Request Body**:
```json
{
  "bankName": "SBI Bank",
  "lastFour": "5678",
  "type": "Savings",
  "amount": 15000.00
}
```
* **Response Status**: `201 Created`
* **Response Body**:
```json
{
  "id": "4",
  "bankName": "SBI Bank",
  "lastFour": "5678",
  "type": "Savings",
  "amount": 15000.00
}
```
