document.addEventListener("DOMContentLoaded", () => {

    async function submitForm(form) {
        const url = form.action;
        const method = form.method.toUpperCase();
        const formData = new FormData(form);

        try {
            const response = await fetch(url, {
                method: method,
                body: formData,
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                }
            });

            if (!response.ok) throw new Error(`Request failed: ${response.statusText}`);
            console.log(url);
            return await response.json();
        } catch (err) {
            console.error(err);
            alert("Something went wrong. Check console for details.");
        }
    }

    function applyVisibilityToTable(isPublic) {
        document.querySelectorAll("table tbody tr").forEach(row => {
            const isAdmin = row.querySelector("td:nth-child(2)").textContent === "Admin";
            row.style.display = isPublic && !isAdmin ? "none" : "";
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
