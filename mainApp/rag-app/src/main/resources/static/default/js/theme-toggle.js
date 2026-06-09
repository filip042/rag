document.addEventListener('DOMContentLoaded', () => {
    const themeToggle = document.getElementById('theme-toggle');
    if (!themeToggle) return;

    const enableDarkMode = () => {
        document.documentElement.setAttribute('data-color-scheme', 'dark');
        localStorage.setItem('theme', 'dark');
        themeToggle.textContent = '☀️';
    };

    const disableDarkMode = () => {
        document.documentElement.removeAttribute('data-color-scheme');
        localStorage.setItem('theme', 'light');
        themeToggle.textContent = '🌙';
    };

    const savedTheme = localStorage.getItem('theme');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    if (savedTheme === 'dark' || (!savedTheme && prefersDark)) {
        enableDarkMode();
    } else {
        disableDarkMode();
    }

    themeToggle.addEventListener('click', () => {
        const isDarkMode =
            document.documentElement.getAttribute('data-color-scheme') === 'dark';

        isDarkMode ? disableDarkMode() : enableDarkMode();
    });
});
