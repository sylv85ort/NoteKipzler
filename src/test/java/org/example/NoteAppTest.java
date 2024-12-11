package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NoteAppTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    @BeforeEach
    public void setup() {
        // Setup mock data if necessary
    }

    @Test
    public void testAddNote() throws Exception {
        // Prepare test data
        NoteApp.NoteRequest noteRequest = new NoteApp.NoteRequest();
        noteRequest.setText("Test note");

        // Perform POST request to add a new note
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isOk()) // Expecting status 200
                .andExpect(jsonPath("$.text").value("Test note")) // Verifying the response
                .andExpect(jsonPath("$.done").value(false)); // Verifying default value of "done"
    }

    @Test
    public void testListNotes() throws Exception {
        // Test GET to list notes
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()) // Expecting the response to be an array
                .andExpect(jsonPath("$[0].text").exists()); // Expecting a note with text
    }

    @Test
    public void testMarkNoteDone() throws Exception {
        // Create a note using the constructor. Since the id is generated automatically, you don't need to set it.
        NoteApp.Note note = new NoteApp.Note("Test note");

        // Add the note to the notes list for the test
        NoteApp.getNotes().add(note);  // Use the getter if the list is not directly accessible

        // Perform PUT request to mark the note as done
        mockMvc.perform(put("/api/notes/{id}/done", note.getId())) // Use the generated id
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(true)); // Verify the note's 'done' status is updated
    }

    @Test
    public void testRemoveNote() throws Exception {
        // Perform DELETE request to remove a note
        mockMvc.perform(delete("/api/notes/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    public void testAddNote_EmptyText() throws Exception {
        // Arrange: Create a note request with empty text
        NoteApp.NoteRequest noteRequest = new NoteApp.NoteRequest();
        noteRequest.setText("");

        // Act & Assert: Perform a POST request and verify the response
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isBadRequest()) // Verify HTTP 400 Bad Request status
                .andExpect(jsonPath("$.error").exists()) // Check that the error field exists
                .andExpect(jsonPath("$.error").value("Note text cannot be empty")); // Verify error message
    }

}