document.addEventListener('DOMContentLoaded', () => {
    const textInput = document.getElementById('question');
    if (!textInput) return;

    const charCount = document.getElementById('charCount');
    const maxLength = textInput.maxLength;

    function updateCounter() {
        const currentLength = textInput.value.length;
        charCount.textContent = `${currentLength}/${maxLength}`;

        if (maxLength - currentLength < 20) {
            charCount.classList.add('near-limit');
        } else {
            charCount.classList.remove('near-limit');
        }

        if (currentLength > 0) {
            charCount.classList.add('visible');
        } else {
            charCount.classList.remove('visible');
        }
    }

    textInput.addEventListener('input', updateCounter);
    updateCounter();
});