# Personal Finance Tracker

A Java Swing-based desktop application for managing personal finances, tracking expenses, income, and bills.

## Features

- **User Authentication**
  - Secure login system
  - User-specific data management

- **Financial Management**
  - Track income and expenses
  - Manage recurring bills
  - Set and monitor monthly budgets
  - Real-time balance updates
  - Previous month's savings calculation

- **Bill Management**
  - Add and track bills
  - Mark bills as paid
  - Due date tracking
  - Bill payment history

- **Budget Features**
  - Set monthly budgets
  - Visual budget status indicators
  - Remaining budget calculations
  - Over-budget warnings

- **Data Visualization**
  - Clear overview of financial status
  - Real-time updates
  - Color-coded budget indicators

- **Data Export**
  - Export financial data to CSV
  - Detailed transaction history
  - Compatible with spreadsheet software

- **Additional Features**
  - Real-time date/time display
  - Data reset capability
  - Auto-refresh functionality

## Technical Details

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- SQLite JDBC Driver
- Swing GUI Framework

### Database Schema

The application uses SQLite with the following tables:

    - users: Stores user information and authentication data
    - transactions: Records income and expense transactions
    - bills: Manages bill information and payment status

## Usage

    1. Login/Registration
        - Start the application
        - Register a new account or login with existing credentials

    2. Managing Finances
        - Add income and expenses using respective panels
        - Set up monthly budget
        - Add and track bills

    3. Monitoring
        - View current balance
        - Track monthly expenses
        - Monitor budget status
        - Check previous month's savings

    4. Data Management
        - Export data to CSV
        - Reset data if needed
        - Regular auto-refresh of displayed information

## Acknowledgments


    - SQLite for database management
    - Java Swing for GUI components
    - Contributors and testers
