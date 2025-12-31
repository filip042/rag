document.addEventListener("DOMContentLoaded", () => {
    const loadForm = document.getElementById("loadForm");
    const fileInput = document.getElementById("fileInput");
    const directoryInput = document.getElementById("directoryInput");
    const selectedFilesDiv = document.getElementById("selected-files");
    const uploadTypeRadios = document.querySelectorAll('input[name="uploadType"]');
    const filesInputContainer = document.getElementById("filesInputContainer");
    const directoryInputContainer = document.getElementById("directoryInputContainer");

    uploadTypeRadios.forEach(radio => {
        radio.addEventListener("change", function() {
            if (this.value === "files") {
                filesInputContainer.classList.remove("hidden");
                directoryInputContainer.classList.add("hidden");
                directoryInput.value = '';
                selectedFilesDiv.textContent = '';
            } else {
                filesInputContainer.classList.add("hidden");
                directoryInputContainer.classList.remove("hidden");
                fileInput.value = '';
                selectedFilesDiv.textContent = '';
            }
        });
    });

    function updateSelectedFiles(files) {
        if (files.length > 0) {
            selectedFilesDiv.textContent = `${files.length} file(s) selected`;
        } else {
            selectedFilesDiv.textContent = '';
        }
    }

    fileInput.addEventListener("change", function() {
        updateSelectedFiles(this.files);
    });

    directoryInput.addEventListener("change", function() {
        updateSelectedFiles(this.files);
    });

    loadForm.addEventListener("submit", function(event) {
        event.preventDefault();

        const uploadType = document.querySelector('input[name="uploadType"]:checked').value;
        const files = uploadType === "files" ? fileInput.files : directoryInput.files;

        if (files.length === 0) {
            alert("Please select files or a directory to upload.");
            return;
        }

        const formData = new FormData();
        for (let i = 0; i < files.length; i++) {
            formData.append("files", files[i]);
        }

        fetch(loadEndpoint, {
            method: "POST",
            body: formData
        })
            .then(response => {
                if (!response.ok) throw new Error("Upload failed");
                return response.text();
            })
            .then(() => {
                alert("Upload started.");
                fileInput.value = '';
                directoryInput.value = '';
                selectedFilesDiv.textContent = '';
            })
            .catch(err => {
                console.error(err);
                alert("Error uploading files.");
            });
    });
});
