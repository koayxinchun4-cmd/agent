// 简单的 Gemini API 调用封装
const API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
let apiKey = localStorage.getItem('gemini_api_key') || '';

document.getElementById('saveKey').addEventListener('click', () => {
    apiKey = document.getElementById('apiKey').value.trim();
    if (apiKey) {
        localStorage.setItem('gemini_api_key', apiKey);
        alert('API Key saved!');
    }
});

// 页面加载时填充已保存的 Key
document.getElementById('apiKey').value = apiKey;

document.getElementById('sendBtn').addEventListener('click', sendMessage);
document.getElementById('userInput').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendMessage();
});

async function sendMessage() {
    const input = document.getElementById('userInput');
    const message = input.value.trim();
    if (!message) return;
    if (!apiKey) {
        alert('Please enter your Gemini API key first.');
        return;
    }

    addMessage('user', message);
    input.value = '';

    try {
        const response = await fetch(`${API_BASE}?key=${apiKey}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                contents: [{ parts: [{ text: message }] }]
            })
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        const data = await response.json();
        const reply = data.candidates?.[0]?.content?.parts?.[0]?.text || 'No response';
        addMessage('assistant', reply);
    } catch (error) {
        addMessage('assistant', 'Error: ' + error.message);
    }
}

function addMessage(role, text) {
    const chatBox = document.getElementById('chatBox');
    const msgDiv = document.createElement('div');
    msgDiv.className = `message ${role}`;
    msgDiv.textContent = text;
    chatBox.appendChild(msgDiv);
    chatBox.scrollTop = chatBox.scrollHeight;
}
