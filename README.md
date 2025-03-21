# Brokerage Stock Order Management System

A Spring Boot backend application for managing stock orders in a brokerage company. This system supports customer asset tracking, order creation/matching, and admin-level management. Includes JWT-based authentication and role-based access control.

---

## Features

### Authentication & Authorization
- JWT-based login (`/login`)
- Role-based access: `ADMIN` and `CUSTOMER`
- Admins can manage all users and match orders
- Customers can only access/manipulate their own data

### Order Management
- Create BUY/SELL orders
- Match orders (admin only)
- Cancel (delete) pending orders
- List orders with filters (date, status, side, username)

### Asset Management
- Track assets per customer including `TRY` (currency)
- Usable size vs. total size management
- Automatic asset updates on order operations
- Add assets to customers (admin only)

### Customer Management
- Create new customers (admin only)
- List all customers (admin only)
- Fetch own customer info 
- Fetch specific customer info (admin only)

---
## Project Structure

```bash
src/
├── main/
│   ├── java/com/yasin/brokerage/
│   │   ├── controller/         # REST API endpoints (CustomerController, OrderController, etc.)
│   │   ├── model/              # Entity classes (Customer, Order, Asset, enums)
│   │   ├── repository/         # Spring Data JPA repositories (CustomerRepository, OrderRepository, etc.)
│   │   ├── security/           # JWT-related configuration and filters
│   │   ├── service/            # Business logic and services
│   │   ├── ApplicationStartup.java             # Initial data setup (admin, assets, example customers etc.)
│   │   └── BrokerageCompanyApplication.java    # Main Spring Boot application class
│   └── resources/
│       ├── application.properties  # App configuration (H2 DB settings, etc.)
│       └── data.sql                # Optional initial data
└── test/*                          # Unit tests 

```

---

## How to Run

### Prerequisites
- Java 17+
- Maven 3.6+
- IDE (e.g., IntelliJ) or terminal

### Run with Maven

```bash
./mvnw spring-boot:run
```


### Run Tests
```bash
./mvnw test
```

## Technologies Used

| Technology        | Description                                    |
|-------------------|------------------------------------------------|
| Spring Boot       | Framework for building RESTful services        |
| Spring Security   | Provides authentication and authorization      |
| JWT (JSON Web Token) | Secure stateless authentication mechanism |
| Spring Data JPA   | ORM layer for database interaction             |
| H2 Database       | Lightweight in-memory database (for testing)   |
| Lombok            | Reduces boilerplate (getters/setters/etc.)     |
| JUnit & Mockito   | Unit testing and mocking                       |
| Maven             | Build tool and dependency management           |

---

## API Endpoints

### Authentication
- `POST /login` — Returns a JWT token after successful login

### Customers
- `POST /customers/create` — Create a new customer (Admin only)
- `GET /customers` — List all customers (Admin only) 
- `GET /customers?username=yasin` — View own customer info

### Assets
- `POST /assets/add` — Add or update an asset for a user (Admin only)
- `GET /assets` — View current user's assets

### Orders
- `POST /orders/create` — Create a new order
- `GET /orders` — List orders with optional filters (date, status, side)
- `DELETE /orders/delete` — Cancel a PENDING order
- `POST /orders/match` — Match a PENDING order (Admin only)


#### Order Filtering

The `/orders` endpoint supports advanced filtering using query parameters, allowing you to narrow down results based on user, date range, order status, and order side.

### Example Request

```http
GET /orders?username=admin&startDate=2024-01-01&endDate=2025-01-01&status=MATCHED&side=BUY
```

---

## Order Lifecycle

This section outlines how orders are handled throughout their lifecycle.

### 1. Create Order
- Endpoint: `POST /orders/create`
- Requires: `assetName`, `orderSide`, `size`, `price`
- Optional: `createDate` (only allows **future** dates)
- Validations:
    - For `SELL`: customer must have sufficient `usableSize` of the asset.
    - For `BUY`: customer must have enough usable `TRY` (i.e., `TRY.usableSize ≥ price * size`).
- Effects:
    - Asset balances are updated (only `usableSize`, not `size`).
    - Order is stored with `PENDING` status.

### 2. Cancel Order
- Endpoint: `DELETE /orders/delete?orderId=...`
- Conditions:
    - Only `PENDING` orders can be cancelled.
    - Only the owner or an `ADMIN` can cancel the order.
- Effects:
    - Asset `usableSize` is restored.
    - Order status is updated to `CANCELLED` (not deleted from DB).

### 3. Match Order (Admin Only)
- Endpoint: `POST /orders/match?orderId=...`
- Conditions:
    - Only `PENDING` orders can be matched.
    - Matching is only allowed if the `createDate` is **today or earlier**.
- Effects:
    - For `SELL`: asset `size` is reduced, `TRY` increased.
    - For `BUY`: asset `size` increased, `TRY.size` reduced.
    - Order status is updated to `MATCHED`.

---

## Sample Data Initialization

The application includes a startup class to initialize example data for testing:

`ApplicationStartup.java`

This class creates 3 users on application startup:

- `yasin` (CUSTOMER)
- `alice` (CUSTOMER)
- `admin` (ADMIN)

It also assigns initial assets for testing:
- `yasin`: 10,000 TRY and 20 NVDA shares
- `alice`: 5,000 TRY and 15 AAPL shares

This data allows you to:
- Immediately test order creation (BUY/SELL)
- Verify authorization flows for both regular users and admin
- See real results when calling `/orders`, `/assets`, etc.

---

## Postman Collection

The repository includes a ready-to-use Postman collection:  
**`Brokerage.postman_collection.json`**

It contains sample requests for:

- Creating customers and logging in
- Managing assets (add/view)
- Creating, listing, matching, and canceling orders
- Testing authorization with different user roles

### How to Use

1. Open **Postman**
2. Click **Import**
3. Choose `Brokerage.postman_collection.json` from the project root
4. Test endpoints with the provided example users

> Make sure the app is running at `http://localhost:8080`

---
