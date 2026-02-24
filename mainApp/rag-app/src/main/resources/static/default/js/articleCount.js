const refresh_rate = 5000;
let intervalId = null;
let expectedTodo = -1; // -1 means "not waiting for a specific batch"

function fetchArticleCount() {
    fetch(articleCountEndpoint)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok ' + response.statusText);
            return response.json();
        })
        .then(data => {
            const todo = data.todo;
            const done = data.finishedFiles.length;
            const percent = todo === 0 ? 100 : Math.round((100 * done) / todo);

            document.getElementById('article-count').textContent = done + "/" + todo + " articles indexed (" + percent + "%)";

            const indexingStatus = document.getElementById('indexingStatus');
            if (!indexingStatus || !indexingStatus.classList.contains('active')) return;

            if (expectedTodo !== -1 && todo === expectedTodo) return;
            expectedTodo = -1;

            if (done === todo && todo > 0) {
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
            expectedTodo = data.todo;
        })
        .catch(() => {})
        .finally(() => {
            stopPolling();

            function pollUntilUpdated() {
                fetch(articleCountEndpoint)
                    .then(r => r.json())
                    .then(data => {
                        if (data.todo !== expectedTodo) {
                            expectedTodo = -1;
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