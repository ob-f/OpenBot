const WebSocket = require('ws');

const wss = new WebSocket.Server({ port: 8080 }, () => {
    console.log('Signaling server is now listening on port 8080');
});

// Each room = one browser + one robot, matched by Google email (roomId).
const rooms = new Map();

// Join only when message is just roomId (+ optional clientType), not a drive command.
const isJoinMessage = (msg) => {
    if (!msg.roomId || msg.roomId === 'request-roomId') {
        return false;
    }
    if (msg.driveCmd || msg.command || msg.status || msg.webrtc_event) {
        return false;
    }
    const keys = Object.keys(msg);
    return keys.every((k) => k === 'roomId' || k === 'clientType');
};

wss.on('connection', (ws) => {
    ws.clientType = null;
    ws.roomId = null;
    console.log(`Client connected (total: ${wss.clients.size})`);
    askIdOfClient(ws);

    ws.on('message', (data, isBinary) => {
        const message = isBinary ? data : data.toString();
        let msg;

        try {
            msg = JSON.parse(message);
        } catch (error) {
            console.log('Ignoring non-JSON message');
            return;
        }

        if (isJoinMessage(msg)) {
            const clientType = msg.clientType === 'robot' ? 'robot' : 'browser';
            joinRoom(msg.roomId, ws, clientType);
            return;
        }

        const roomId = msg.roomId || ws.roomId;
        if (!roomId) {
            console.log('Dropping message (client not in a room):', Object.keys(msg).join(','));
            return;
        }

        if (!ws.roomId) {
            joinRoom(roomId, ws, ws.clientType || 'browser');
        }

        relayToPeer(roomId, message, ws, msg);
    });

    ws.on('close', () => {
        console.log(`Client disconnected (${ws.clientType || 'unknown'} total: ${wss.clients.size})`);
        leaveRoom(ws);
    });
});

// Tell new clients to send their room id (Google email).
const askIdOfClient = (ws) => {
    ws.send(JSON.stringify({ roomId: 'request-roomId' }));
};

const joinRoom = (roomId, ws, clientType) => {
    if (!rooms.has(roomId)) {
        rooms.set(roomId, { browser: null, robot: null });
    }

    const room = rooms.get(roomId);
    const slot = clientType === 'robot' ? 'robot' : 'browser';
    const previous = room[slot];

    if (previous && previous !== ws && previous.readyState === WebSocket.OPEN) {
        console.log(`Closing previous ${slot} in room ${roomId}`);
        previous.close(4000, 'replaced by newer connection');
    }

    room[slot] = ws;
    ws.clientType = clientType;
    ws.roomId = roomId;

    const browserConnected = room.browser != null && room.browser.readyState === WebSocket.OPEN;
    const robotConnected = room.robot != null && room.robot.readyState === WebSocket.OPEN;
    console.log(
        `Room ${roomId}: ${clientType} joined (browser=${browserConnected}, robot=${robotConnected})`
    );
};

const leaveRoom = (ws) => {
    if (!ws.roomId) {
        return;
    }
    const room = rooms.get(ws.roomId);
    if (!room) {
        return;
    }
    if (ws.clientType === 'browser' && room.browser === ws) {
        room.browser = null;
    }
    if (ws.clientType === 'robot' && room.robot === ws) {
        room.robot = null;
    }
    ws.roomId = null;
};

const relayToPeer = (roomId, message, sender, msg) => {
    const room = rooms.get(roomId);
    if (!room) {
        console.log(`No room for ${roomId}`);
        return;
    }

    const peer = sender.clientType === 'browser' ? room.robot : room.browser;
    if (peer && peer.readyState === WebSocket.OPEN) {
        peer.send(message);
    } else {
        console.log(`No peer in room ${roomId} (sender=${sender.clientType})`);
    }
};
