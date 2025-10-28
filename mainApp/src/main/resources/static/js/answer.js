const refresh_rate = 5000;

async function checkAnswer(taskId) {
    try {
        const response = await fetch(answerUrl, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                taskId: taskId,
            })
        });

        if (!response.ok) {
            throw {
                status: response.status,
                statusText: response.statusText,
                response: response
            };
        }

        const data = await response.json();

        if (!data.done) {
            setTimeout(() => checkAnswer(taskId), refresh_rate);
            return;
        }

        document.getElementById("spinner").style.display = "none";
        document.getElementById("answer-text").textContent = data.answer || "No answer found.";

        if (data.sources && data.sources.length > 0) {
            const ul = document.getElementById("sources-list");
            data.sources.forEach(src => {
                const li = document.createElement("li");
                li.textContent = src;
                ul.appendChild(li);
            });
            document.getElementById("sources").style.display = "block";
        }

    } catch (err) {
        console.error("Error fetching answer:", err);
        document.getElementById("answer-text").textContent = "Error fetching answer.";
        document.getElementById("spinner").style.display = "none";
    }
}

async function fetchAnswer() {
    try {
        const response = await fetch(askUrl, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                query: question,
                workSpace: workSpace
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        const taskId = data.taskId;
        if (!taskId) throw new Error("No taskId returned from backend");

        checkAnswer(taskId);

    } catch (err) {
        console.error("Error starting answer task:", err);
        document.getElementById("answer-text").textContent = "Error fetching answer.";
        document.getElementById("spinner").style.display = "none";
    }
}

fetchAnswer();
