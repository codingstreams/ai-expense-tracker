# Implemented Backend API Specifications for Profile Settings

This document defines the backend REST API endpoints implemented to make the user profile page fully functional.

All endpoints require authorization using a JWT bearer token.

---

## 1. Global Headers & Configuration

Every request to these endpoints must include the following headers:

| Header Name | Value / Format | Description |
| :--- | :--- | :--- |
| `Authorization` | `Bearer <JWT_ACCESS_TOKEN>` | User authentication token |
| `Content-Type` | `application/json` | Required for all request payloads (`PATCH`) |

---

## 2. User Configurations API

Manages user-specific preferences such as UI language and default payment modes.

### 2.1. Get User Configurations
Fetches the current user's profile configuration.

* **URL**: `/api/user/config`
* **Method**: `GET`
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "language": "English",
  "defaultPaymentModeId": 1
}
```

### 2.2. Update User Configurations
Updates the current user's configurations (e.g., language selection or default payment mode).

* **URL**: `/api/user/config`
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
Retrieves the user's linked bank accounts (Savings), cash/wallet accounts, and credit cards.

* **URL**: `/api/accounts`
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
    "bankName": "ICICI Bank Credit Card",
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
