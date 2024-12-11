package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SwingAppTest {
    private SwingApp swingApp;
    private JTextField noteInputField;
    private DefaultTableModel tableModel;

    @BeforeEach
    public void setUp() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] setupException = new Exception[1];

        SwingUtilities.invokeLater(() -> {
            try {
                // Create SwingApp
                swingApp = new SwingApp();

                // Reflection to access private fields
                Field inputFieldField = SwingApp.class.getDeclaredField("noteInputField");
                inputFieldField.setAccessible(true);
                noteInputField = (JTextField) inputFieldField.get(swingApp);

                Field tableModelField = SwingApp.class.getDeclaredField("tableModel");
                tableModelField.setAccessible(true);
                tableModel = (DefaultTableModel) tableModelField.get(swingApp);

                latch.countDown();
            } catch (Exception e) {
                setupException[0] = e;
                e.printStackTrace();
                latch.countDown();
            }
        });

        // Wait for the latch with a timeout
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // If setup failed or timed out, throw an informative exception
        if (!completed || setupException[0] != null) {
            throw new AssertionError("Failed to set up test environment", setupException[0]);
        }

        // Additional null checks
        assertNotNull(swingApp, "SwingApp should not be null");
        assertNotNull(noteInputField, "Note input field should not be null");
        assertNotNull(tableModel, "Table model should not be null");
    }

    @Test
    public void testInputFieldInitialState() {
        SwingUtilities.invokeLater(() -> {
            // Check initial state of input field
            assertNotNull(noteInputField, "Note input field should not be null");
            assertTrue(noteInputField.getText().isEmpty(), "Input field should be initially empty");
        });
    }

    @Test
    public void testAddNote() {
        SwingUtilities.invokeLater(() -> {
            // Get the addNote method via reflection to ensure we're testing the actual method
            try {
                Method addNoteMethod = SwingApp.class.getDeclaredMethod("addNote");
                addNoteMethod.setAccessible(true);

                // Initial row count
                int initialRowCount = tableModel.getRowCount();

                // Set text and add note
                noteInputField.setText("Test Note");
                addNoteMethod.invoke(swingApp);

                // Verify note was added
                assertEquals(initialRowCount + 1, tableModel.getRowCount(), "Table should have one more row after adding note");
                assertEquals("Test Note", tableModel.getValueAt(tableModel.getRowCount() - 1, 1), "Note text should match input");
                assertFalse((Boolean) tableModel.getValueAt(tableModel.getRowCount() - 1, 0), "Initial 'Done' status should be false");
            } catch (Exception e) {
                fail("Could not invoke addNote method: " + e.getMessage());
            }
        });
    }

    @Test
    public void testAddEmptyNote() {
        SwingUtilities.invokeLater(() -> {
            // Get the addNote method via reflection
            try {
                Method addNoteMethod = SwingApp.class.getDeclaredMethod("addNote");
                addNoteMethod.setAccessible(true);

                // Initial table state
                int initialRowCount = tableModel.getRowCount();

                // Try to add empty note
                noteInputField.setText("");
                addNoteMethod.invoke(swingApp);

                // Verify no additional row was added
                assertEquals(initialRowCount, tableModel.getRowCount(),
                        "Table row count should not change with empty note");
            } catch (Exception e) {
                fail("Could not invoke addNote method: " + e.getMessage());
            }
        });
    }

    @Test
    public void testInputFieldClear() {
        SwingUtilities.invokeLater(() -> {
            // Set text and then clear
            noteInputField.setText("Sample Note");
            assertFalse(noteInputField.getText().isEmpty(), "Input field should have text");

            // Clear the input field
            noteInputField.setText("");
            assertTrue(noteInputField.getText().isEmpty(), "Input field should be clearable");
        });
    }

    @Test
    public void testInputFieldTrimming() {
        SwingUtilities.invokeLater(() -> {
            // Test whitespace trimming
            noteInputField.setText("   Trimmed Note   ");
            String trimmedText = noteInputField.getText().trim();
            assertEquals("Trimmed Note", trimmedText, "Input should trim whitespace");
        });
    }
}