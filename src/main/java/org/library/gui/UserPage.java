package org.library.gui;

import org.library.book.Book;
import org.library.database.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserPage extends JFrame {
    private JPanel panel;
    private JTable bookTable;
    private JButton borrowButton;
    private JButton returnButton;
    private JButton logoutButton;

    private List<Book> books;

    public UserPage() {
        setTitle("Library App - User Page");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);

        panel = new JPanel(new BorderLayout());

        // Load books from database
        loadBooks();

        // Create table to display books
        createTable();

        // Create borrow button
        borrowButton = new JButton("Borrow Book");
        borrowButton.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow != -1) {
                Book selectedBook = books.get(selectedRow);
                borrowBook(selectedBook);
            } else {
                JOptionPane.showMessageDialog(panel, "Select a book to borrow.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Create return button
        returnButton = new JButton("Return Book");
        returnButton.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow != -1) {
                Book selectedBook = books.get(selectedRow);
                returnBook(selectedBook);
            } else {
                JOptionPane.showMessageDialog(panel, "Select a book to return.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Create logout button
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            // Perform logout logic
            performLogout();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(borrowButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(logoutButton);

        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void loadBooks() {
        books = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM books")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                boolean available = rs.getBoolean("available");
                Book book = new Book(id, title, author, available);
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String[] columnNames = {"ID", "Title", "Author", "Available"};
        Object[][] data = new Object[books.size()][4];

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            data[i][0] = book.getId();
            data[i][1] = book.getTitle();
            data[i][2] = book.getAuthor();
            data[i][3] = book.isAvailable() ? "Yes" : "No";
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(model);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void borrowBook(Book book) {
        int userId = 1; // Replace with actual user ID (session user ID)
        try (Connection conn = DatabaseManager.getConnection()) {
            // Check if the book is available
            if (!book.isAvailable()) {
                JOptionPane.showMessageDialog(panel, "This book is already borrowed.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert into borrowed_books table
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO borrowed_books (user_id, book_id) VALUES (?, ?)")) {
                stmt.setInt(1, userId);
                stmt.setInt(2, book.getId());
                stmt.executeUpdate();
            }

            // Update book availability to false
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE books SET available = false WHERE id = ?")) {
                stmt.setInt(1, book.getId());
                stmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(panel, "Book borrowed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Refresh books list and table
            loadBooks();
            refreshTable();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void returnBook(Book book) {
        int userId = 1; // Replace with actual user ID (session user ID)
        try (Connection conn = DatabaseManager.getConnection()) {
            // Check if the book is borrowed by the current user
            boolean isBorrowed = false;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM borrowed_books WHERE user_id = ? AND book_id = ?")) {
                stmt.setInt(1, userId);
                stmt.setInt(2, book.getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    isBorrowed = true;
                }
            }

            if (!isBorrowed) {
                JOptionPane.showMessageDialog(panel, "You have not borrowed this book.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Delete from borrowed_books table
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM borrowed_books WHERE user_id = ? AND book_id = ?")) {
                stmt.setInt(1, userId);
                stmt.setInt(2, book.getId());
                stmt.executeUpdate();
            }

            // Update book availability to true
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE books SET available = true WHERE id = ?")) {
                stmt.setInt(1, book.getId());
                stmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(panel, "Book returned successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Refresh books list and table
            loadBooks();
            refreshTable();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void performLogout() {
        // Close the current UserPage frame
        dispose();

        // Show the login page
        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }

    private void refreshTable() {
        String[] columnNames = {"ID", "Title", "Author", "Available"};
        Object[][] data = new Object[books.size()][4];

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            data[i][0] = book.getId();
            data[i][1] = book.getTitle();
            data[i][2] = book.getAuthor();
            data[i][3] = book.isAvailable() ? "Yes" : "No";
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable.setModel(model);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UserPage().setVisible(true);
        });
    }
}
