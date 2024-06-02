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

public class AdminPage extends JFrame {
    private JPanel panel;
    private JTable bookTable;
    private JButton addButton;
    private JButton deleteButton;
    private JButton updateButton;
    private JButton logoutButton;

    private List<Book> books;

    public AdminPage() {
        setTitle("Library App - Admin Page");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);

        panel = new JPanel(new BorderLayout());

        // Load books from database
        loadBooks();

        // Create table to display books
        createTable();

        // Create buttons
        addButton = new JButton("Add Book");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open dialog to add book
                showAddBookDialog();
            }
        });

        deleteButton = new JButton("Delete Book");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Delete selected book
                int selectedRow = bookTable.getSelectedRow();
                if (selectedRow != -1) {
                    int bookId = books.get(selectedRow).getId();
                    deleteBook(bookId);
                    loadBooks(); // Refresh books list from database
                    refreshTable(); // Refresh table with updated data
                } else {
                    JOptionPane.showMessageDialog(panel, "Select a book to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        updateButton = new JButton("Update Book");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open dialog to update book
                int selectedRow = bookTable.getSelectedRow();
                if (selectedRow != -1) {
                    Book selectedBook = books.get(selectedRow);
                    showUpdateBookDialog(selectedBook);
                } else {
                    JOptionPane.showMessageDialog(panel, "Select a book to update.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Create logout button
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Perform logout logic
                performLogout();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
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

        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        bookTable = new JTable(model);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        bookTable.setModel(model);
    }

    private void showAddBookDialog() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();

        JPanel dialogPanel = new JPanel(new GridLayout(2, 2));
        dialogPanel.add(new JLabel("Title:"));
        dialogPanel.add(titleField);
        dialogPanel.add(new JLabel("Author:"));
        dialogPanel.add(authorField);

        int result = JOptionPane.showConfirmDialog(panel, dialogPanel, "Add Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText();
            String author = authorField.getText();
            addBook(title, author);
            loadBooks(); // Refresh books list from database
            refreshTable(); // Refresh table with updated data
        }
    }

    private void addBook(String title, String author) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO books (title, author, available) VALUES (?, ?, true)")) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteBook(int bookId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM books WHERE id = ?")) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showUpdateBookDialog(Book book) {
        JTextField titleField = new JTextField(book.getTitle());
        JTextField authorField = new JTextField(book.getAuthor());

        JPanel dialogPanel = new JPanel(new GridLayout(2, 2));
        dialogPanel.add(new JLabel("Title:"));
        dialogPanel.add(titleField);
        dialogPanel.add(new JLabel("Author:"));
        dialogPanel.add(authorField);

        int result = JOptionPane.showConfirmDialog(panel, dialogPanel, "Update Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText();
            String author = authorField.getText();
            updateBook(book.getId(), title, author);
            loadBooks(); // Refresh books list from database
            refreshTable(); // Refresh table with updated data
        }
    }

    private void updateBook(int bookId, String title, String author) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE books SET title = ?, author = ? WHERE id = ?")) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void performLogout() {
        // Close the current AdminPage frame
        dispose();

        // Show the login page
        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdminPage().setVisible(true);
        });
    }
}
