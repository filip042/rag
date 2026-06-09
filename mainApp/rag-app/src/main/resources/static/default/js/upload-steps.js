// Enhanced UI logic for step management
const fileInput = document.getElementById('fileInput');
const directoryInput = document.getElementById('directoryInput');
const uploadButton = document.getElementById('uploadButton');
const step3 = document.getElementById('step3');
const selectedFilesDiv = document.getElementById('selected-files');
const indexingStatus = document.getElementById('indexingStatus');

function updateSelectedFiles() {
    const uploadType = document.querySelector('input[name="uploadType"]:checked').value;
    const input = uploadType === 'files' ? fileInput : directoryInput;
    const files = input.files;

    if (files.length > 0) {
        // Enable step 3
        step3.classList.remove('disabled');
        uploadButton.disabled = false;
        selectedFilesDiv.classList.remove('empty');

        // Show selected files
        let html = `<strong>${files.length} file(s) selected:</strong><ul class="file-list">`;
        const maxDisplay = 10;
        for (let i = 0; i < Math.min(files.length, maxDisplay); i++) {
            html += `<li>${files[i].webkitRelativePath || files[i].name}</li>`;
        }
        if (files.length > maxDisplay) {
            html += `<li><em>... and ${files.length - maxDisplay} more</em></li>`;
        }
        html += '</ul>';
        selectedFilesDiv.innerHTML = html;
    } else {
        // Disable step 3
        step3.classList.add('disabled');
        uploadButton.disabled = true;
        selectedFilesDiv.classList.add('empty');
        selectedFilesDiv.innerHTML = '';
    }
}

// Listen for file selection changes
fileInput.addEventListener('change', updateSelectedFiles);
directoryInput.addEventListener('change', updateSelectedFiles);

// Handle upload type switching
document.querySelectorAll('input[name="uploadType"]').forEach(radio => {
    radio.addEventListener('change', function() {
        if (this.value === 'files') {
            document.getElementById('filesInputContainer').classList.remove('hidden');
            document.getElementById('directoryInputContainer').classList.add('hidden');
            directoryInput.value = '';
        } else {
            document.getElementById('filesInputContainer').classList.add('hidden');
            document.getElementById('directoryInputContainer').classList.remove('hidden');
            fileInput.value = '';
        }
        updateSelectedFiles();
    });
});

// Show indexing status when form is submitted
document.getElementById('loadForm').addEventListener('submit', function() {
    console.log('submit fired');
    console.log('startPolling type:', typeof startPolling);
    const statusText = indexingStatus.querySelector('strong');
    if (statusText) {
        statusText.textContent = 'Indexing in progress...';
        statusText.style.color = '';
    }
    indexingStatus.classList.add('active');
    startPolling();
    console.log('startPolling called, intervalId:', intervalId);
});

// Optional: Disable file inputs during indexing (uncomment if desired)
/*
document.getElementById('loadForm').addEventListener('submit', function() {
    fileInput.disabled = true;
    directoryInput.disabled = true;
    document.querySelectorAll('input[name="uploadType"]').forEach(r => r.disabled = true);
});
*/