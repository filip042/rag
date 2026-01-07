const refresh_rate = 5000;
let intervalId = null;

function fetchArticleCount() {
    fetch(articleCountEndpoint)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            const todo = data.todo;
            const done = data.finishedFiles.length;
            const percent = todo === 0 ? 100 : Math.round((100 * done) / todo);

            document.getElementById('article-count').textContent = done + "/" + todo + " articles indexed (" + percent + "%)";

            const indexingStatus = document.getElementById('indexingStatus');
            if (indexingStatus && indexingStatus.classList.contains('active')) {
                if (done === todo && todo > 0) {
                    const statusText = indexingStatus.querySelector('strong');
                    if (statusText) {
                        statusText.textContent = 'Indexing complete!';
                        statusText.style.color = '#4CAF50';
                    }

                    if (intervalId) {
                        clearInterval(intervalId);
                        intervalId = null;
                    }
                }
            }
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            document.getElementById('article-count').textContent = 'Error loading count';
        });
}

intervalId = setInterval(fetchArticleCount, refresh_rate);
fetchArticleCount();