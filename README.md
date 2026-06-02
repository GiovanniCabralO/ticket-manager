# 🎫 Ticket Manager CLI — Support Ticket Management System

🌎 Language:
🇺🇸 English | [🇧🇷 Português](README.pt-BR.md)

---

A command-line application built in Java for managing support tickets, developed to demonstrate practical knowledge of Object-Oriented Programming, JDBC, SQL, automated testing, and software design patterns.

The system allows users to create, track, update, and close support tickets while maintaining a complete audit trail of all changes through a persistent logging mechanism stored in SQLite.

---

## ✨ Features

* **Ticket creation** — create support tickets with title, description, and priority
* **Ticket listing** — display all tickets in a formatted table
* **Advanced filtering** — filter tickets by status and priority
* **Keyword search** — search titles and descriptions using SQL `LIKE`
* **Status updates** — business-rule validation (e.g., closed tickets cannot be reopened)
* **Ticket editing** — partial updates for title and description
* **Change history** — complete audit log for every modification
* **Ticket closing** — soft deletion through the `CLOSED` status
* **CSV export** — generate reports compatible with spreadsheet applications

---

## 🗄️ Concepts Demonstrated

This project was designed to showcase backend development and database best practices:

| Concept                  | Implementation                                        |
| ------------------------ | ----------------------------------------------------- |
| JDBC                     | Direct SQLite communication using `PreparedStatement` |
| Parameterized SQL        | Protection against SQL Injection                      |
| Full CRUD Operations     | Create, Read, Update, and Close workflows             |
| Repository Pattern       | Separation of data access and application logic       |
| Singleton Pattern        | Centralized database connection management            |
| Foreign Keys             | Relationship between tickets and audit logs           |
| Enums                    | Type-safe Status and Priority values                  |
| Business Rule Validation | Enforced at the repository layer                      |
| Unit Testing             | JUnit 5 test suite using an in-memory SQLite database |
| CSV Export               | Proper handling of quotes and special characters      |

---

## 🛠️ Technologies

* **Java 17**
* **SQLite**
* **JDBC**
* **Maven**
* **JUnit 5**

---

## 🚀 Running Locally

```bash
# 1. Clone the repository
git clone https://github.com/your-username/ticket-manager.git

# 2. Enter the project directory
cd ticket-manager

# 3. Build the project
mvn clean package

# 4. Run the application
java -jar target/ticket-manager-1.0.0-jar-with-dependencies.jar

# 5. Run the tests
mvn test
```

---

## 📁 Project Structure

```text
ticket-manager/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/java/com/ticketmanager/
    │   ├── Main.java
    │   ├── model/
    │   │   └── Ticket.java
    │   ├── database/
    │   │   └── DatabaseManager.java
    │   ├── repository/
    │   │   └── TicketRepository.java
    │   └── ui/
    │       └── Menu.java
    └── test/java/com/ticketmanager/
        └── TicketRepositoryTest.java
```

---

## 🗃️ Database Structure

The application uses two main tables:

### Tickets

Stores all support tickets.

* ID
* Title
* Description
* Status
* Priority
* Creation timestamp
* Last update timestamp

### Ticket Logs

Stores the audit trail of ticket changes.

* ID
* Related ticket
* Action performed
* Change details
* Log timestamp

This structure enables complete traceability throughout the lifecycle of each ticket.

---

## 🧪 Testing

The project includes approximately **20 unit tests** using JUnit 5 and an in-memory SQLite database, covering:

* Ticket creation
* Status updates
* Business rule validation
* Search and filtering
* Ticket editing
* Data export

---

## 📌 Author

**Giovanni Cabral** — Computer Engineering Student at Facens | Python & Automation Intern at Huawei Technologies

GitHub: https://github.com/GiovanniCabralO

LinkedIn: https://www.linkedin.com/in/giovannicabraldeoliveira
