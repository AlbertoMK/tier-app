document.addEventListener('DOMContentLoaded', function() {
    fetch('menu.html')  // Ruta del archivo HTML que contiene el template
        .then(response => response.text())
        .then(html => {
            const templateContainer = document.createElement('div');
            templateContainer.innerHTML = html;

            const template = templateContainer.querySelector('#menuTemplate');
            const clone = document.importNode(template.content, true);

            document.getElementById('menu-container').appendChild(clone);
        })
        .catch(error => console.error('Error loading the template:', error));
});
