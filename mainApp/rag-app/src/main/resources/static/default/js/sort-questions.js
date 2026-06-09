function sortQuestions(order) {
    const container = document.getElementById('questionList');
    const items = Array.from(container.getElementsByClassName('question-card'));

    items.sort((a, b) => {
        const idA = parseInt(a.getAttribute('data-id'));
        const idB = parseInt(b.getAttribute('data-id'));
        return order === 'asc' ? idA - idB : idB - idA;
    });

    items.forEach(item => container.appendChild(item));
}

function filterQuestions() {
    const query = document.getElementById('filterInput').value.toLowerCase();
    const cards = document.querySelectorAll('.question-card');

    cards.forEach(card => {
        const question = card.querySelector('.question-section p')?.textContent.toLowerCase() ?? '';
        const answer = card.querySelector('.answer-section p')?.textContent.toLowerCase() ?? '';
        const matches = question.includes(query) || answer.includes(query);
        card.style.display = matches ? '' : 'none';
    });
}