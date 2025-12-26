document.addEventListener("DOMContentLoaded", () => {
    const loadForm = document.getElementById("loadForm");

    loadForm.addEventListener("submit", function(event) {
        event.preventDefault();

        const directory = document.getElementById("directory").value;

        fetch(loadEndpoint, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ directory })
        })
            .then(response => {
                if (!response.ok) throw new Error("Upload failed");
                return response.text();
            })
            .then(() => {
                alert("Directory upload started.");
            })
            .catch(err => {
                console.error(err);
                alert("Error uploading directory.");
            });
    });
});

