//started with code from https://developer.mozilla.org/en-US/docs/Learn/Server-side/Node_server_without_framework

const http = require('http');
const fs = require('fs');
const path = require('path');
const WebSocketServer = require('websocket').server;

const JOIN = "join";
const server = http.createServer(function (request, response) {
    console.log('request ', request.url);

    let filePath = 'resources' + request.url;
    if (filePath === 'resources/') {
        filePath = 'resources/index.html';
    }

    const extname = String(path.extname(filePath)).toLowerCase();
    const mimeTypes = {
        '.html': 'text/html',
        '.js': 'text/javascript',
        '.css': 'text/css',
        '.json': 'application/json',
        '.png': 'image/png',
        '.jpg': 'image/jpg',
        '.gif': 'image/gif',
        '.svg': 'image/svg+xml',
        '.wav': 'audio/wav',
        '.mp4': 'video/mp4',
        '.woff': 'application/font-woff',
        '.ttf': 'application/font-ttf',
        '.eot': 'application/vnd.ms-fontobject',
        '.otf': 'application/font-otf',
        '.wasm': 'application/wasm'
    };

    const contentType = mimeTypes[extname] || 'application/octet-stream';

    fs.readFile(filePath, function (error, content) {
        if (error) {
            if (error.code === 'ENOENT') {
                fs.readFile('resources/404.html', function (error, content) {
                    response.writeHead(404, {'Content-Type': 'text/html'});
                    response.end(content, 'utf-8');
                });
            } else {
                response.writeHead(500);
                response.end('Sorry, check with the site admin for error: ' + error.code + ' ..\n');
            }
        } else {
            response.writeHead(200, {'Content-Type': contentType});
            response.end(content, 'utf-8');
        }
    });

}).listen(8080, function () {
    console.log(' Server is listening on port 8080');
});


// started from https://www.npmjs.com/package/websocket
wsServer = new WebSocketServer({
    httpServer: server,
});


class Room {
    constructor(roomName) {
        this.roomName = roomName;
        this.userListConnections = new Map();
        this.messageList = [];
    }
}

let roomMap = new Map();
wsServer.on('request', function (request) {
    const connection = request.accept(null, request.origin);
    console.log((new Date()) + ' Connection accepted.');

    connection.on('message', function (message) {

        let messageSplit = message.utf8Data.split(" ");
        if (messageSplit[0] === JOIN) {
            messageSplit = messageSplit.utf8Data.split(" ");

            let roomName = messageSplit[1];
            let userName = messageSplit[3];

            let room;
            if (!roomMap.has(roomName)) {
                room = new Room(roomName);
                console.log("room " + messageSplit[1] + " created");
                roomMap.set(roomName, room);
            } else {
                room = roomMap.get(roomName);
            }
            console.log("user " + userName + " connected to room " + roomName);
            room.userListConnections.set(userName, connection);

            room.messageList.forEach(value => {
                connection.send(value);
            })

        } else {
            let userName = messageSplit[0];
            let userMessage = messageSplit[1];
            const USER = "{\"user\":\"";
            const MESSAGE = "\",\"message\":\"";
            const JSON_END = "\"}";
            const jsonMessage = USER + userName + MESSAGE + userMessage + JSON_END;

            roomMap.forEach((value) => {
                    if (value.userListConnections.has(userName)) {
                        value.userListConnections.forEach((value1) => {
                            value1.send(jsonMessage);
                        });
                        value.messageList.push(jsonMessage)
                    }
                }
            );

        }

    });

    connection.on('close', function (reasonCode, description) {
        console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected.');
    });

});

