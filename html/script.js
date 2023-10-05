document.addEventListener("DOMContentLoaded", function () {
    const list = document.getElementById("lines-list");
    const viewport = document.querySelector('.viewport');
    let listItemHeight;
    let visibleElements = 40; // Number of visible elements
    let scrollOffset = 0;
    let isScrollingToEnd = true;
    const scrollDuration = 8000; // 8 seconds
    const pauseDuration = 10000; // 60 seconds

    fetch("your-text-file.txt")
        .then(response => response.text())
        .then(text => {
            // Split the text into lines
            const lines = text.split("\n");

            // Create list items and add them to the ul
            lines.forEach(lineText => {
                const li = document.createElement("li");
                li.textContent = lineText;
                list.appendChild(li);

                // Check if the line needs scrolling logic
                if (li.scrollWidth > li.clientWidth) {
                    li.classList.add("scrollable");
                }
            });

            // Calculate the number of empty items to add to make the count multiple of visibleElements
            const remainder = lines.length % visibleElements;
            const emptyItemCount = remainder === 0 ? 0 : visibleElements - remainder;
            for (let i = 0; i < emptyItemCount; i++) {
                const emptyListItem = document.createElement('li');
                list.appendChild(emptyListItem);
            }

            // Trigger reflow to apply the animation
            list.style.display = "none";
            list.offsetHeight;
            list.style.display = "block";

            // Calculate the listItemHeight after the list is generated
            listItemHeight = list.firstElementChild.getBoundingClientRect().height;

            setInterval(scrollList, scrollDuration + pauseDuration);

        })
        .catch(error => {
            console.error("Error loading text file:", error);
        });



    function scrollList() {
        const scrollAmount = listItemHeight * visibleElements;
        const maxScroll = list.scrollHeight - viewport.clientHeight;
    
        if (isScrollingToEnd) {
            scrollOffset += scrollAmount;
    
            if (scrollOffset >= maxScroll) {
            list.style.transition = `transform ${scrollDuration / 1000}s linear`;
            list.style.transform = `translateY(-${maxScroll}px)`;
            setTimeout(() => {
                list.style.transition = 'none';
                isScrollingToEnd = false;
                scrollOffset = maxScroll;
                list.style.transform = `translateY(-${scrollOffset}px)`;
                setTimeout(() => {
                list.style.transition = `transform ${scrollDuration / 1000}s linear`;
                scrollOffset -= scrollAmount;
                if (scrollOffset < maxScroll) {
                    scrollOffset = maxScroll;
                }
                list.style.transform = `translateY(-${scrollOffset}px)`;
                }, 10);
            }, scrollDuration + pauseDuration);
            } else {
            list.style.transition = `transform ${scrollDuration / 1000}s linear`;
            list.style.transform = `translateY(-${scrollOffset}px)`;
            }
        } else {
            scrollOffset -= scrollAmount;
    
            if (scrollOffset <= 0) {
            list.style.transition = 'none';
            isScrollingToEnd = true;
            scrollOffset = 0;
            list.style.transform = `translateY(-${scrollOffset}px)`;
            setTimeout(() => {
                list.style.transition = `transform ${scrollDuration / 1000}s linear`;
                scrollOffset -= scrollAmount;
                if (scrollOffset < 0) {
                scrollOffset = 0;
                }
                list.style.transform = `translateY(-${scrollOffset}px)`;
            }, 10);
            } else {
            list.style.transition = `transform ${scrollDuration / 1000}s linear`;
            list.style.transform = `translateY(-${scrollOffset}px)`;
            }
        }
    }

});


