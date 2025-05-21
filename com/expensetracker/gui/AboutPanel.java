package com.expensetracker.gui;

import javax.swing.*;
import java.awt.*;

public class AboutPanel extends JPanel {

    public AboutPanel() {
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // Create main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        // Add description
        String description = """
                <html><head><meta charset="UTF-8">
                    <title>Personal Finance Tracker</title>
                    <style>
                        body {
                            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
                            max-width: 600px;
                            margin: 40px auto;
                            padding: 30px;
                            background-color: #f9f9f9;
                            color: #333;
                            border: 1px solid #ddd;
                            border-radius: 12px;
                            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
                        }
                
                        h1 {
                            text-align: center;
                            color: #2c3e50;
                            font-size: 16px;
                            margin-bottom: 20px;
                        }
                
                        p {
                            font-size: 10px;
                            line-height: 1.6;
                        }
                
                        ul {
                            margin-top: 10px;
                            padding-left: 20px;
                        }
                
                        li {
                            margin-bottom: 6px;
                            font-size: 10px;
                        }
                
                        .section-title {
                            font-weight: bold;
                            margin-top: 20px;
                            color: #34495e;
                        }
                
                        .credits {
                            margin-top: 30px;
                            font-size: 15px;
                        }
                
                        .credits ul {
                            padding-left: 20px;
                        }
                
                        .credits li {
                            list-style-type: square;
                        }
                
                        .footer {
                            margin-top: 20px;
                            font-style: italic;
                            color: #555;
                        }
                    </style>
                </head>
                <body>
                <h1>Personal Finance Tracker</h1>
                <p>
                    This Personal Finance Tracker is a Java Swing application developed as a second-semester student project.
                    <br>
                    It helps users manage their personal finances by tracking income, expenses, and bills.
                </p>
                
                <p class="section-title">Key Features:</p>
                <ul>
                    <li>Track income and expenses</li>
                    <li>Manage recurring bills</li>
                    <li>Set and monitor budgets</li>
                    <li>View financial summaries</li>
                    <li>Export data to CSV</li>
                </ul>
                
                <p class="section-title">Object-Oriented Programming Principles Demonstrated:</p>
                <ul>
                    <li>Encapsulation</li>
                    <li>Inheritance</li>
                    <li>Polymorphism</li>
                    <li>Abstraction</li>
                </ul>
                
                <div class="credits">
                    <p class="section-title">Developed by:</p>
                    <ul>
                        <li>Anirudh Jain</li>
                        <li>Ayush Pandey</li>
                        <li>Chitranshi Kumre</li>
                    </ul>
                
                    <p class="footer">
                        Made as: Sem-Long Project<br>
                        For: DSC6 paper OOPs<br>
                        Under the guidance of: Sir Anjani Verma
                    </p></div></body></html>""";

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(descriptionLabel);

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Add scroll pane to main panel
        add(scrollPane, BorderLayout.CENTER);
    }
}