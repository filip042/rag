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
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        const status = data.status;
        const checked = data.checked || 0;
        const total = data.total || 0;
        const checkedAll = data.checked_all === true;

        const spinner = document.getElementById("spinner");
        const answerText = document.getElementById("answer-text");
        const sourcesList = document.getElementById("sources-list");
        const sourcesDiv = document.getElementById("sources");

        if (status === "checking" && !checkedAll) {
            answerText.textContent = `Checking sources: ${checked}/${total}`;
            setTimeout(() => checkAnswer(taskId), refresh_rate);
            return;
        }
        if (status === "checking" && checkedAll) {
            answerText.textContent = "All sources checked, generating answer...";
            setTimeout(() => checkAnswer(taskId), refresh_rate);
            return;
        }
        if (status === "done") {
            spinner.style.display = "none";
            answerText.textContent = data.answer || "No answer found.";

            if (data.sources && data.sources.length > 0) {
                sourcesList.innerHTML = "";
                data.sources.forEach(src => {
                    const li = document.createElement("li");
                    li.textContent = src;
                    sourcesList.appendChild(li);
                });
                sourcesDiv.style.display = "block";
            }
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
