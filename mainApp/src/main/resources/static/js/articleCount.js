const refresh_rate = 5000;

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
            const percent = todo === 0 ? 100 : (100 * done) / todo;
            document.getElementById('article-count').textContent = done + "/" + todo + " articles indexed (" + percent + "%)";
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            document.getElementById('article-count').textContent = 'Error loading count';
        });
}
setInterval(fetchArticleCount, refresh_rate);
fetchArticleCount();