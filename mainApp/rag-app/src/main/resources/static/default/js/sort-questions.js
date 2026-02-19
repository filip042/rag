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