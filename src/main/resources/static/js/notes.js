// notes.js

// Helper function to handle fetch errors
function handleFetchError(response) {
    if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
    }
    return response;
}

function addNote() {
    const noteText = document.getElementById('noteText').value.trim();
    if (!noteText) {
        alert('Note cannot be empty');
        return;
    }

    fetch('/api/notes', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ text: noteText })
    })
    .then(handleFetchError)
    .then(() => {
        document.getElementById('noteText').value = ''; // Clear input field
        loadNotes();
    })
    .catch(error => console.error('Error adding note:', error));
}

function loadNotes() {
    fetch('/api/notes')
        .then(handleFetchError)
        .then(response => response.json())
        .then(renderNotes)
        .catch(error => console.error('Error loading notes:', error));
}

function renderNotes(notes) {
    const notesList = document.getElementById('notesList');
    notesList.innerHTML = ''; // Clear existing notes

    notes.forEach(note => {
        const noteElement = document.createElement('div');
        noteElement.className = 'note-list-item';
        noteElement.id = `note-${note.id}`;
        noteElement.innerHTML = `
            <span>${note.text}</span>
            <div class="note-actions">
                <button onclick="deleteNote(${note.id})">Delete</button>
                ${!note.done ? `<button onclick="markNoteDone(${note.id})">Mark as Done</button>` : '<span>Done</span>'}
            </div>
        `;
        notesList.appendChild(noteElement);
    });
}

function markNoteDone(id) {
    fetch(`/api/notes/${id}/done`, { method: 'PUT' })
        .then(handleFetchError)
        .then(() => {
            console.log(`Note ${id} marked as done`);
            loadNotes();
        })
        .catch(error => console.error('Error marking note as done:', error));
}

function deleteNote(id) {
    fetch(`/api/notes/${id}`, {
        method: 'DELETE',
    })
    .then(handleFetchError)
    .then(() => {
        console.log(`Note ${id} deleted successfully`);
        loadNotes();
    })
    .catch(error => console.error('Error deleting note:', error));
}

// Load notes when page loads
document.addEventListener('DOMContentLoaded', loadNotes);
