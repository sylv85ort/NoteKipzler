package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SwingApp extends JFrame {
    private DefaultTableModel tableModel;
    private JTable notesTable;
    private JTextField noteInputField;

    public SwingApp() {
        // Set up the frame
        setTitle("Notes and Checklist Application");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        noteInputField = new JTextField();
        JButton addButton = new JButton("Add Note");
        JButton removeButton = new JButton("Remove Selected");

        inputPanel.add(new JLabel("Enter Note:"), BorderLayout.WEST);
        inputPanel.add(noteInputField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        // Create table model
        String[] columnNames = {"Done", "Note"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Only checkbox column is editable
            }
        };

        // Create table
        notesTable = new JTable(tableModel);
        notesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Adjust column widths
        TableColumnModel columnModel = notesTable.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(150);  // Make "Done" column very narrow
        columnModel.getColumn(0).setPreferredWidth(50);

        JScrollPane scrollPane = new JScrollPane(notesTable);

        // Add action listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNote();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedNote();
            }
        });

        // Add components to main panel
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Create bottom panel for remove button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(removeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add note with Enter key
        noteInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNote();
            }
        });

        // Add main panel to frame
        add(mainPanel);
    }

    private void addNote() {
        String noteText = noteInputField.getText().trim();
        if (!noteText.isEmpty()) {
            tableModel.addRow(new Object[]{false, noteText});
            noteInputField.setText("");
        }
    }

    private void removeSelectedNote() {
        int selectedRow = notesTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
        } else {
            // Optional: Remove completed notes if no row selected
            for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
                if ((Boolean) tableModel.getValueAt(i, 0)) {
                    tableModel.removeRow(i);
                }
            }
        }
    }

    public static void main(String[] args) {
        // Ensure GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SwingApp().setVisible(true);
            }
        });
    }
}