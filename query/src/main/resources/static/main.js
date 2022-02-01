 function doSearch() {
    const searchInput = document.getElementById("searchInput");
    const searchResults = document.getElementById("searchResults");
    console.log(searchInput.value);

    fetch('/rest/search', {
        method: 'POST',
        body: searchInput.value
    }).then(async function(response) {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        }).then(function (docs) {
        console.log(docs);
        docs.documents.forEach(doc => {
            const div = renderResultCard(doc);
            searchResults.appendChild(div);
        });
    });
}

function renderResultCard(d) {
    var html = `
            <div class="d-flex justify-content-between align-items-center">
                <div class="d-flex flex-row align-items-center">
                    <img src="/images/${d.details.parentId}/${d.details.slideNo}.jpg">
                    <div class="d-flex flex-column"> <span><a href="file:///${d.details.parentFilename}">${d.details.parentFilename}</a></span>
                        <div class="d-flex flex-row align-items-center time-text"> 
                            <small>Marketing</small> <span class="dots"></span> 
                            <small>viewed Just now</small> <span class="dots"></span> 
                            <small>Edited 15 minutes ago</small> 
                        </div>
                    </div>
                </div>
                <span class="content-text-1">BA</span>
            </div>
        `;
    const div = document.createElement('div');
    div.classList.add("mt-3");
    div.innerHTML = html;
    return div;
}
