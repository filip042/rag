const refresh_rate = 5000;
let intervalId = null;
let expectedFiles = -1; // -1 means "not waiting for a specific batch"

function fetchArticleCount() {
    fetch(articleCountEndpoint)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok ' + response.statusText);
            return response.json();
        })
        .then(data => {
            const totalFiles = data.totalFiles;
            const done = data.finishedFiles.length;
            const percent = totalFiles === 0 ? 100 : Math.round((100 * done) / totalFiles);

            document.getElementById('article-count').textContent = done + "/" + totalFiles + " articles indexed (" + percent + "%)";

            const indexingStatus = document.getElementById('indexingStatus');
            if (!indexingStatus || !indexingStatus.classList.contains('active')) return;

            if (expectedFiles !== -1 && totalFiles === expectedFiles) return;
            expectedFiles = -1;

            if (done === totalFiles && totalFiles > 0) {
                const statusText = indexingStatus.querySelector('strong');
                if (statusText) {
                    statusText.textContent = 'Indexing complete!';
                    statusText.style.color = '#4CAF50';
                }
                stopPolling();
            }
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            document.getElementById('article-count').textContent = 'Error loading count';
        });
}

function stopPolling() {
    if (intervalId) {
        clearInterval(intervalId);
        intervalId = null;
    }
}

function startPolling() {
    fetch(articleCountEndpoint)
        .then(r => r.json())
        .then(data => {
            expectedFiles = data.totalFiles;
        })
        .catch(() => {})
        .finally(() => {
            stopPolling();

            function pollUntilUpdated() {
                fetch(articleCountEndpoint)
                    .then(r => r.json())
                    .then(data => {
                        if (data.totalFiles !== expectedFiles) {
                            expectedFiles = -1;
                            fetchArticleCount();
                            intervalId = setInterval(fetchArticleCount, refresh_rate);
                        } else {
                            setTimeout(pollUntilUpdated, 300);
                        }
                    })
                    .catch(() => setTimeout(pollUntilUpdated, 300));
            }

            pollUntilUpdated();
        });
}

startPolling();