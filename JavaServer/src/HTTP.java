import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

class HTTP {

    private static final String MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final String SEC_WEB_SOCKET_KEY = "Sec-WebSocket-Key:";
    private static final String SWITCHING_PROTOCOLS = "HTTP/1.1 101 Switching Protocols\r\n";
    private static final String UPGRADE_WEBSOCKET = "Upgrade: websocket\r\n";
    private static final String CONNECTION_UPGRADE = "Connection: Upgrade\r\n";
    private static final String SEC_WEB_SOCKET_ACCEPT = "Sec-WebSocket-Accept: ";
    private static final String NEW_LINE_NEW_LINE = "\r\n\r\n";
    private static final String HTTP_1_1_200_OK = "HTTP/1.1 200 OK\r\n";
    private static final String CONTENT_LENGTH = "Content-Length: ";
    private static final String RESOURCES = "resources";
    private static final String NOT_FOUND = "404.html";
    private static final String INDEX_HTML = "index.html";
    private static final String SHA = "SHA-1";

    private final ArrayList<String[]> clientInput = new ArrayList<>(32);
    private String wsKey;
    private String wsAccept;
    private File file;
    private FileInputStream fileInputStream;

    void storeClientInput(Socket socket) throws IOException {
        String[] userInputSplit;
        Scanner scanner = new Scanner(socket.getInputStream());
        do {
            userInputSplit = scanner.nextLine().split(" ");
            clientInput.add(userInputSplit);

        } while (!userInputSplit[0].isBlank());

    }

    void processClientInput() throws NoSuchAlgorithmException {
        String requestedFileName = clientInput.get(0)[1];
        file = new File(RESOURCES + requestedFileName);

        if (requestedFileName.equals("/")) {
            file = new File(RESOURCES + "/" + INDEX_HTML);

        }
        if (wsKeyIndex() != -1) {
            wsKey = clientInput.get(wsKeyIndex())[1];

            wsAccept = Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance(SHA).digest(
                            (wsKey + MAGIC_STRING).getBytes()
                    )
            );
            System.out.println(wsAccept);

        }

    }

    private int wsKeyIndex() {
        for (int i = 0; i < clientInput.size(); i++) {
            if (clientInput.get(i)[0].equals(SEC_WEB_SOCKET_KEY)) {
                return i;
            }
        }
        return -1;
    }

    String getWsKey() {
        return wsKey;
    }

    void setUpFileInputStream() throws IOException {

        try {
            fileInputStream = new FileInputStream(file);

        } catch (FileNotFoundException e) {
            file = new File(RESOURCES + "/" + NOT_FOUND);
            fileInputStream = new FileInputStream(file);

        }

    }

    void httpResponse(Socket socket) throws IOException {

        if (wsKey != null) {
            switchProtocolToWebsocket(socket);

        } else {
            serveWebsite(socket);

        }

    }

    private void switchProtocolToWebsocket(Socket socket) throws IOException {
        socket.getOutputStream().write((SWITCHING_PROTOCOLS +
                UPGRADE_WEBSOCKET +
                CONNECTION_UPGRADE +
                SEC_WEB_SOCKET_ACCEPT + wsAccept + NEW_LINE_NEW_LINE).getBytes());
        socket.getOutputStream().flush();
    }

    private void serveWebsite(Socket socket) throws IOException {
        socket.getOutputStream().write((HTTP_1_1_200_OK +
                CONTENT_LENGTH + file.length() + NEW_LINE_NEW_LINE).getBytes());

        socket.getOutputStream().flush();
        fileInputStream.transferTo(socket.getOutputStream());
        socket.close();
    }


}
