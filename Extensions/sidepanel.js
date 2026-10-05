document.addEventListener('DOMContentLoaded', () => {

    // Load saved research notes
    chrome.storage.local.get(['researchNotes'], function(result) {

        if (result.researchNotes) {
            document.getElementById('notes').value = result.researchNotes;
        }

    });

    // Summarize button
    document.getElementById('summarizebtn')
        .addEventListener('click', summarizeText);

    // Save notes button
    document.getElementById('saveNotesBtn')
        .addEventListener('click', saveNotes);

});


// Summarize selected text
async function summarizeText() {

    try {

        // Get active tab
        const [tab] = await chrome.tabs.query({
            active: true,
            currentWindow: true
        });

        // Get selected text from webpage
        const [{ result }] = await chrome.scripting.executeScript({
            target: {
                tabId: tab.id
            },
            function: () => window.getSelection().toString()
        });

        // Check if text is selected
        if (!result || !result.trim()) {

            showResult('Please select some text first');

            return;
        }


        // Send selected text to deployed Spring Boot backend
        const response = await fetch(
            'https://smart-research-assistant-8f37.onrender.com/api/research/process',
            {
                method: 'POST',

                headers: {
                    'Content-Type': 'application/json'
                },

                body: JSON.stringify({
                    content: result,
                    operation: 'summarize'
                })
            }
        );


        // Check API response
        if (!response.ok) {

            throw new Error(
                `API Error: ${response.status}`
            );

        }


        // Read response
        const text = await response.text();


        // Display result
        showResult(
            text.replace(/\n/g, '<br>')
        );


    } catch (error) {

        console.error('Error:', error);

        showResult(
            'Error: ' + error.message
        );

    }

}


// Save research notes
async function saveNotes() {

    const notes =
        document.getElementById('notes').value;


    chrome.storage.local.set(
        {
            researchNotes: notes
        },

        function() {

            alert('Notes saved successfully');

        }
    );

}


// Display result
function showResult(content) {

    document.getElementById('results').innerHTML =

        `<div class="result-item">

            <div class="result-content">
                ${content}
            </div>

        </div>`;

}