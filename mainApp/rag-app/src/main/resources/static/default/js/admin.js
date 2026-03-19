document.addEventListener("DOMContentLoaded", () => {

    async function submitForm(form) {
        const url = form.action;
        const method = form.method.toUpperCase();
        const formData = new FormData(form);

        const response = await fetch(url, {
            method: method,
            body: formData,
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        });

        if (!response.ok) {
            console.error(`Request failed: ${response.statusText}`);
            alert("Something went wrong. Check console for details.");
        }
        // console.log(url);
        return await response.json();
    }

    function applyVisibilityToTable(isPublic) {
        const select = document.getElementById("users");
        document.querySelectorAll("table tbody tr").forEach(row => {
            const isAdmin = row.querySelector("td:nth-child(2)").textContent === "Admin";
            const userId = row.querySelector("input[name='userId']")?.value;
            const username = row.querySelector("td:nth-child(1)").textContent;

            if (isPublic && !isAdmin) {
                row.style.display = "none";
                if (userId) {
                    const option = document.createElement("option");
                    option.value = userId;
                    option.textContent = username;
                    select.appendChild(option);
                }
            } else {
                row.style.display = "";
                if (userId) {
                    select.querySelector(`option[value="${userId}"]`)?.remove();
                }
            }
        });
    }

    const isPublicOnLoad = document.querySelector("input[name='isPublic']:checked").value === "true";
    applyVisibilityToTable(isPublicOnLoad);

    document.querySelector(".ajax-form.visibility").addEventListener("submit", async (e) => {
        e.preventDefault();
        const data = await submitForm(e.target);
        if (!data) {
            return;
        }
        document.getElementById("add-user-heading").textContent = data.isPublic ? "Add Admin to Project" : "Add User to Project";
        document.getElementById("current-users-heading").textContent = data.isPublic ? "Current Project Admins" : "Current Project Users";
        applyVisibilityToTable(data.isPublic);
    });

    document.querySelectorAll(".ajax-form.add-user").forEach(form => {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const data = await submitForm(form);
            if (!data) return;

            const select = document.getElementById("users");
            const optionToRemove = select.querySelector(`option[value="${data.user.id}"]`);
            if (optionToRemove) optionToRemove.remove();

            const tbody = document.querySelector("table tbody");
            const newRow = document.createElement("tr");
            newRow.innerHTML = `
                <td>${data.user.username}</td>
                <td>${data.user.admin ? 'Admin' : 'User'}</td>
                <td>
                    ${!data.user.admin ? `
                        <form class="ajax-form promote-user" action="/admin/promote" method="post" style="display:inline;">
                            <input type="hidden" name="userId" value="${data.user.id}">
                            <button type="submit">Promote</button>
                        </form>
                        <form class="ajax-form remove-user" action="/admin/remove" method="post" style="display:inline;">
                            <input type="hidden" name="userId" value="${data.user.id}">
                            <button type="submit" class="delete" onclick="return confirm('Remove this user from the project?');">Remove</button>
                        </form>
                    ` : ''}
                </td>
            `;
            const isPublic = document.querySelector("input[name='isPublic']:checked").value === "true";
            if (isPublic && !data.user.admin) {
                newRow.style.display = "none";
            }
            if (data.user.admin) {
                const existingRow = Array.from(tbody.querySelectorAll("tr")).find(row =>
                    row.querySelector("input[name='userId']")?.value === String(data.user.id)
                );
                if (existingRow) existingRow.remove();
            }
            tbody.appendChild(newRow);
        });
    });

    document.querySelector("table").addEventListener("submit", async (e) => {
        if (!e.target.classList.contains("promote-user")) return;
        e.preventDefault();
        const form = e.target;
        const data = await submitForm(form);
        if (!data) return;

        const row = form.closest("tr");
        row.querySelector("td:nth-child(2)").textContent = "Admin";
        form.remove();
        const removeForm = row.querySelector(".remove-user");
        if (removeForm) removeForm.remove();
    });

    document.querySelector("table").addEventListener("submit", async (e) => {
        if (!e.target.classList.contains("remove-user")) return;
        e.preventDefault();
        const form = e.target;
        const data = await submitForm(form);
        if (!data) return;

        const row = form.closest("tr");
        row.remove();

        const select = document.getElementById("users");
        const option = document.createElement("option");
        option.value = data.user.id;
        option.textContent = data.user.username;
        select.appendChild(option);
    });
});
