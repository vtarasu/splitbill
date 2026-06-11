# SplitBill 

SplitBill is an expense sharing application that helps friends, roommates, travel groups, and teams track shared expenses and settle balances effortlessly.

Inspired by apps like Splitwise, SplitBill allows users to create groups, add expenses, track balances, and manage settlements while keeping the number of transactions to a minimum.

## Features

### Authentication
- User registration and login
- JWT-based authentication
- Secure password storage

### Groups
- Create expense groups
- Add and manage group members
- Track shared expenses within groups

### Expense Management
- Add expenses to groups and non-groups
- Split the expenses equally or unequally
- Track who paid and who owes
- View expense history

### Balance Tracking
- Real-time balance calculations
- Settlement tracking

### Subscription Plans
- Stripe-powered subscriptions
- Free tier usage limits
- Premium plan support

---

## Tech Stack

### Frontend
- React
- Vite
- Axios
- Tailwind CSS

### Backend
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate

### Database
- MySQL
- AWS RDS

### Payments
- Stripe

### Deployment
- Frontend: Netlify
- Backend: Render / AWS EC2
- Database: AWS RDS

---

## Architecture

```text
React (Netlify)
        |
        | HTTPS
        v
Spring Boot API
(Render / EC2)
        |
        v
MySQL (AWS RDS)
```

---

## Getting Started

### Prerequisites

- Java 21
- Gradle
- MySQL
- Node.js
- npm

---

## Backend Setup

Clone the repository:

```bash
git clone https://github.com/vtarasu/splitbill.git
cd splitbill
```

Create an environment configuration:

```properties
SPRING_DATASOURCE_URL=your-db-url
SPRING_DATASOURCE_USERNAME=your-db-username
SPRING_DATASOURCE_PASSWORD=your-db-password

JWT_SECRET=your-secret-key

STRIPE_SECRET_KEY=your-stripe-secret
STRIPE_WEBHOOK_SECRET=your-webhook-secret
```

Run:

```bash
gradle clean build
gradle spring-boot:run
```

Backend will start on:

```text
http://localhost:8080
```

---

## API Features

### Authentication

```http
POST /user/register
POST /user/login
```

### Groups

```http
POST /groups
GET /groups
GET /groups/{id}
```

### Expenses

```http
POST /expenses
GET /expenses/group/{groupId}
```

### Subscriptions

```http
POST /api/subscriptions/create-checkout-session
POST /api/subscriptions/webhook
```

---

## Future Enhancements

- Email invitations
- Recurring expenses
- Expense categories
- Settlement reminders
- Mobile application
- Receipt uploads
- Analytics dashboard

---

## Author

**Thirunavukkarasu V**
