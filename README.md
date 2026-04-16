# 🌱 Greenhouse Management System

A comprehensive **Java Swing desktop application** for managing greenhouse operations with a modern, elegant UI and full database integration.

![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-UI-007396?style=for-the-badge)

---

## ✨ Features

### Core Modules
- **🌿 Plant Management** – Add, edit, delete, and track plants with water & nutrient requirements
- **🦠 Disease Tracking** – Monitor and manage plant diseases with severity levels
- **📦 Inventory Control** – Track tools, seeds, fertilizers, and supplies with low-stock alerts
- **📋 Task Management** – Schedule and monitor daily/weekly tasks with calendar view
- **👷 Worker Management** – Manage staff, roles, and attendance
- **🔧 Tool Monitoring** – Check-out/in tools and maintenance tracking
- **🌾 Harvest Records** – Record and analyze harvest yields with quality tracking
- **📊 Reports & Analytics** – Generate detailed reports (Plant, Financial, Task, Custom)

### UI/UX Highlights
- Beautiful **gradient-based modern UI** with consistent green theme
- Smooth animations and hover effects
- Responsive card layouts and custom styled components
- Professional dashboard with real-time statistics
- Search, filter, and sorting capabilities across all modules

---

## 🛠️ Technologies Used

- **Backend**: Java 21 (or higher)
- **UI Framework**: Java Swing + Custom Painting (Gradients, Rounded Corners)
- **Database**: MySQL
- **Architecture**: MVC (Model-View-Controller)
- **Build Tool**: Maven (recommended) / Plain Java Project

---

## 📂 Project Structure
Greenhouse-Management-System/
├── src/
│   ├── view/           
│   ├── controller/     
│   ├── model/          
│   └── util/           
├── database/
│   └── schema.sql     
├── resources/
│   └── images/        
├── README.md
└── pom.xml             


---

## 🚀 How to Run

### 1. Prerequisites
- Java JDK 17 or higher
- MySQL Server (8.0+ recommended)
- MySQL Workbench (optional)

### 2. Database Setup
1. Create a new database:
   ```sql
   CREATE DATABASE greenhouse_db;
   USE greenhouse_db;


   Import the schema (create all tables: Plant, Disease, Inventory, Task, Worker, Harvest, Tool, Attendance, etc.)

Note: You can find the full SQL schema in the database/ folder.

Update DatabaseConnection.java with your MySQL credentials

3. Running the Application

Open the project in IntelliJ IDEA (recommended) or any Java IDE
Run LoginView.java as the main class
Default demo login:
Username: demo
Password: demo

📸 Screenshots
<img width="990" height="695" alt="Screenshot 2026-04-16 124146" src="https://github.com/user-attachments/assets/b09362e9-f9d4-4037-8948-df626a116505" />
<img width="1919" height="1079" alt="Screenshot 2026-04-16 124312" src="https://github.com/user-attachments/assets/732e90d6-6e89-4bca-b193-9d2305300d67" />

👥 Contributors

Ashen – Lead Developer & Designer

⭐ Support
If you like this project, please give it a ⭐ on GitHub!
Feel free to open issues or submit pull requests for improvements.

📧 Contact
Ashen
Email: [ashendias067@gmail.com]
Location: Panadura, Sri Lanka
