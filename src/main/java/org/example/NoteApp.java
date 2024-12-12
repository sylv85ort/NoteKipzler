package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

@SpringBootApplication
@RequestMapping("/api/notes")
@RestController
@Controller
public class NoteApp {
    private static final Logger LOGGER = Logger.getLogger(NoteApp.class.getName());

    // Using CopyOnWriteArrayList for thread-safety during load testing
    protected static List<Note> notes = new CopyOnWriteArrayList<>();
    public static List<Note> getNotes() {
        return notes;
    }
    public static void main(String[] args) {
        SpringApplication.run(NoteApp.class, args);
    }


    // CORS Configuration
    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*");
        }
    }

    // REST Endpoints with logging
    @PostMapping
    public Note addNote(@RequestBody NoteRequest noteRequest) {
        LOGGER.info("Received note request: " + noteRequest.getText());

        if (noteRequest.getText() == null || noteRequest.getText().trim().isEmpty()) {
            LOGGER.warning("Attempted to add empty note");
            throw new IllegalArgumentException("Note text cannot be empty");
        }

        Note newNote = new Note(noteRequest.getText());
        notes.add(newNote);

        LOGGER.info("Note added with ID: " + newNote.getId());
        return newNote;
    }

    @GetMapping
    public List<Note> listNotes() {
        LOGGER.info("Listing notes. Total notes: " + notes.size());
        return new ArrayList<>(notes);
    }

    @PutMapping("/{id}/done")
    public Note markNoteDone(@PathVariable("id") Long id) {
        LOGGER.info("Attempting to mark note done. ID: " + id);
        for (Note note : notes) {
            if (note.getId().equals(id)) {
                note.setDone(true);
                LOGGER.info("Note marked as done: " + id);
                return note;
            }
        }
        LOGGER.warning("Note not found for marking done. ID: " + id);
        throw new RuntimeException("Note not found");
    }

    @DeleteMapping("/{id}")
    public void removeNote(@PathVariable("id") Long id) {
        LOGGER.info("Attempting to remove note with ID: " + id);

        Note noteToDelete = null;
        for (Note note : notes) {
            if (note.getId().equals(id)) {
                noteToDelete = note;
                break;
            }
        }

        if (noteToDelete != null) {
            notes.remove(noteToDelete);
            LOGGER.info("Note removed successfully. ID: " + id);
        } else {
            LOGGER.warning("Note not found for removal. ID: " + id);
        }
    }

    // Note DTO
    public static class Note {
        private static Long idCounter = 0L;

        private Long id;
        private String text;
        private boolean done;

        public Note(String text) {
            this.id = ++idCounter;
            this.text = text;
            this.done = false;
        }

        // Getters and setters
        public Long getId() { return id; }
        public String getText() { return text; }
        public boolean isDone() { return done; }
        public void setDone(boolean done) { this.done = done; }
    }

    // Request DTO
    public static class NoteRequest {
        private String text;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    @RestControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


}