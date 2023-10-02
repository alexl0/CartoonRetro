document.addEventListener("DOMContentLoaded", function () {
    const linesList = document.getElementById("lines-list");

    fetch("your-text-file.txt")
        .then(response => response.text())
        .then(text => {
            // Split the text into lines
            const lines = text.split("\n");

            // Create list items and add them to the ul
            lines.forEach(lineText => {
                const li = document.createElement("li");
                li.textContent = lineText;
                linesList.appendChild(li);

                // Check if the line needs scrolling logic
                if (li.scrollWidth > li.clientWidth) {
                    li.classList.add("scrollable");
                }
            });

            // Trigger reflow to apply the animation
            linesList.style.display = "none";
            linesList.offsetHeight;
            linesList.style.display = "block";
        })
        .catch(error => {
            console.error("Error loading text file:", error);
        });
});
