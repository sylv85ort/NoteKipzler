function addNote() {
    const noteText = document.getElementById('noteText').value;
    if (!noteText.trim()) {
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
    .then(response => response.json())
    .then(note => {
        loadNotes();
        document.getElementById('noteText').value = '';
    })
    .catch(error => console.error('Error:', error));
}

function loadNotes() {
    fetch('/api/notes')
    .then(response => response.json())
    .then(notes => {
        const notesList = document.getElementById('notesList');
        notesList.innerHTML = ''; // Clear existing notes

        notes.forEach(note => {
            const noteElement = document.createElement('div');
            noteElement.className = 'note-list-item';
            noteElement.innerHTML = `
                <span>${note.text}</span>
                <div class="note-actions">
                    <button onclick="markNoteDone(${note.id})">Mark Done</button>
                    <button onclick="deleteNote(${note.id})">Delete</button>
                </div>
            `;
            notesList.appendChild(noteElement);
        });
    })
    .catch(error => console.error('Error:', error));
}

function markNoteDone(id) {
    fetch(`/api/notes/${id}/done`, { method: 'PUT'
    })
    .then(response => {
            if (response.ok) {
                console.log("Note marked as done");
                // Optionally remove the note from the UI
                fetchNotes();
            } else {
                console.error("Failed to mark note");
            }
        })
        .catch(error => {
            console.error("Error marking note as done:", error);
        });
}

function deleteNote(id) {
    fetch(`/api/notes/${id}`, {
        method: 'DELETE',
    })
    .then(response => {
        if (response.ok) {
            console.log("Note deleted successfully");
            // Optionally remove the note from the UI
            fetchNotes();
        } else {
            console.error("Failed to delete note");
        }
    })
    .catch(error => {
        console.error("Error deleting note:", error);
    });
}
function fetchNotes() {
    fetch('/api/notes')
        .then(response => response.json())
        .then(notes => {
            const notesList = document.getElementById('notesList');
            notesList.innerHTML = ''; // Clear current notes list
            notes.forEach(note => {
                const noteElement = document.createElement('div');
                noteElement.id = `note-${note.id}`;
                noteElement.innerHTML = `
                    <p>${note.text}</p>
                    <button onclick="deleteNote(${note.id})">Delete</button>
                    ${!note.done ? `<button onclick="markNoteDone(${note.id})">Mark as Done</button>` : '<span>Done</span>'}
                `;
                notesList.appendChild(noteElement);
            });
        })
        .catch(error => console.error('Error fetching notes:', error));
}



// Load notes when page loads
document.addEventListener('DOMContentLoaded', loadNotes);