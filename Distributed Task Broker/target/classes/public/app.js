// WebSocket connection to broker
let ws = null;
const BROKER_WS_URL = 'ws://localhost:8080/ws';

// Initialize WebSocket connection
function initWebSocket() {
    ws = new WebSocket(BROKER_WS_URL);

    ws.onopen = function (event) {
        console.log('Connected to broker');
        updateStatus('Connected to broker', 'success');
    };

    ws.onmessage = function (event) {
        console.log('Message from broker:', event.data);
        handleBrokerMessage(event.data);
    };

    ws.onerror = function (error) {
        console.error('WebSocket error:', error);
        updateStatus('Connection error', 'error');
    };

    ws.onclose = function (event) {
        console.log('Disconnected from broker');
        updateStatus('Disconnected from broker', 'warning');
        // Attempt to reconnect after 5 seconds
        setTimeout(initWebSocket, 5000);
    };
}

// Handle messages from broker
function handleBrokerMessage(data) {
    try {
        const message = JSON.parse(data);

        switch (message.type) {
            case 'TASK_ACCEPTED':
                updateStatus('Task accepted: ' + message.taskId, 'success');
                break;
            case 'TASK_COMPLETED':
                updateStatus('Task completed: ' + message.taskId, 'success');
                break;
            case 'STATUS_UPDATE':
                updateDashboard(message.data);
                break;
            default:
                console.log('Unknown message type:', message.type);
        }
    } catch (e) {
        console.error('Error parsing message:', e);
    }
}

// Update status message
function updateStatus(message, type) {
    const statusElement = document.getElementById('statusMessage');
    if (statusElement) {
        statusElement.textContent = message;
        statusElement.className = type || '';
    }
}

// Update dashboard statistics
function updateDashboard(data) {
    if (data.activeWorkers !== undefined) {
        document.getElementById('activeWorkers').textContent = data.activeWorkers;
    }
    if (data.pendingTasks !== undefined) {
        document.getElementById('pendingTasks').textContent = data.pendingTasks;
    }
    if (data.completedTasks !== undefined) {
        document.getElementById('completedTasks').textContent = data.completedTasks;
    }
}

// Submit task form handler
if (document.getElementById('taskForm')) {
    document.getElementById('taskForm').addEventListener('submit', function (e) {
        e.preventDefault();

        const taskName = document.getElementById('taskName').value;
        const taskData = document.getElementById('taskData').value;

        const task = {
            type: 'SUBMIT_TASK',
            name: taskName,
            data: taskData,
            timestamp: new Date().toISOString()
        };

        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify(task));
            updateStatus('Submitting task...', 'info');

            // Clear form
            document.getElementById('taskName').value = '';
            document.getElementById('taskData').value = '';
        } else {
            updateStatus('Not connected to broker', 'error');
        }
    });
}

// Initialize WebSocket when page loads
window.addEventListener('load', function () {
    initWebSocket();

    // If on dashboard, request status updates every 5 seconds
    if (document.getElementById('activeWorkers')) {
        setInterval(function () {
            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify({ type: 'REQUEST_STATUS' }));
            }
        }, 5000);
    }
});
