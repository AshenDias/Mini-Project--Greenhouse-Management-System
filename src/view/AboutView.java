package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AboutView extends JDialog {
    public AboutView(JFrame parent) {
        super(parent, "About", true);
        setSize(400, 500);
        setLocationRelativeTo(parent);

        // Create gradient background for main panel
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(220, 245, 220); // Light green
                Color color2 = new Color(180, 225, 180); // Medium green
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header with gradient background
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(46, 125, 50); // Dark green
                Color color2 = new Color(76, 175, 80); // Green
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(" Greenhouse Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Version label with gradient background
        JPanel versionPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(200, 230, 200); // Very light green
                Color color2 = new Color(220, 240, 220); // Lighter green
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        versionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(46, 125, 50)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        versionLabel.setForeground(new Color(46, 125, 50));
        versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        versionPanel.add(versionLabel, BorderLayout.CENTER);

        // Logo panel with gradient
        JPanel logoPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(220, 245, 220);
                Color color2 = new Color(200, 235, 200);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        logoPanel.setPreferredSize(new Dimension(100, 100));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel iconLabel = new JLabel("🏭", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        logoPanel.add(iconLabel, BorderLayout.CENTER);

        // Info area with gradient background
        JTextArea infoArea = new JTextArea() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(245, 255, 245); // Very light green
                Color color2 = new Color(235, 250, 235); // Light green
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        infoArea.setOpaque(false);
        infoArea.setText("""
            Greenhouse Management System
            Developed for AgroVision
            
            Features:
            • Plant Management
            • Disease Tracking
            • Inventory Control
            • Task Scheduling
            • Worker Management
            • Tool Monitoring
            • Harvest Records
            • Reporting & Analytics
            
            Developed using:
            • Java Swing
            • MySQL Database
            • MVC Architecture
            
            © 2025 AgroVision
            All rights reserved.
            """);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoArea.setForeground(new Color(46, 125, 50));
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(76, 175, 80)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Close button with gradient
        JButton closeButton = new JButton("Close") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient for normal state
                if (getModel().isPressed()) {
                    Color color1 = new Color(46, 125, 50); // Dark green when pressed
                    Color color2 = new Color(56, 142, 60);
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                    g2d.setPaint(gp);
                } else if (getModel().isRollover()) {
                    Color color1 = new Color(56, 142, 60); // Brighter green on hover
                    Color color2 = new Color(76, 175, 80);
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                    g2d.setPaint(gp);
                } else {
                    Color color1 = new Color(76, 175, 80); // Normal gradient
                    Color color2 = new Color(56, 142, 60);
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                    g2d.setPaint(gp);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };

        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        closeButton.addActionListener(e -> dispose());

        // Button panel with gradient
        JPanel buttonPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(240, 250, 240);
                Color color2 = new Color(230, 245, 230);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        buttonPanel.add(closeButton);

        // Layout
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(versionPanel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(logoPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
}