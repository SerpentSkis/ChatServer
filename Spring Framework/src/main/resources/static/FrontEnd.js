let webSocket = new WebSocket("ws://" + location.host + "/chat");
let userName;
let chatRoom;


function joinRoom() {
    userName = document.getElementById("userName").value;
    console.log("user name is: " + userName);
    chatRoom = document.getElementById("chatRoom").value;
    console.log("chat room is: " + chatRoom);

    let xhr = new XMLHttpRequest();
    xhr.open("GET", "chatRoom.html");
    xhr.addEventListener("load", function () {

        webSocket.send("join " + chatRoom + " user " + userName);

        webSocket.onmessage = function (e) {
            let parsedMessage = e.data.split(" ", 2);
            document.getElementById("chatBox").appendChild(document.createTextNode(parsedMessage[0] + ": " + parsedMessage[1]));
            document.getElementById("chatBox").appendChild(document.createElement("br"));

            console.log(parsedMessage);
        };

        document.body.innerHTML = this.responseText;

    });
    xhr.send();
}

function sendMessage() {
    webSocket.send(userName + " " + document.getElementById("message").value);
    webSocket.onmessage = function (e) {
        console.log("received message back from server");
        let parsedMessage = e.data.split(" ", 2);
        console.log(parsedMessage);
        document.getElementById("chatBox").appendChild(document.createTextNode(parsedMessage[0] + ": " + parsedMessage[1]));
        document.getElementById("chatBox").appendChild(document.createElement("br"));

    };
}
