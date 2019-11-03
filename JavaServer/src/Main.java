import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Main {
    private static final String JOIN = "join";
    //TODO add server features

    public static void main(String[] args) throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));

        //noinspection InfiniteLoopStatement
        while (true) {

            SocketChannel socketChannel = serverSocketChannel.accept();

            Thread thread = new Thread(() -> {
                try {
                    HTTP http = new HTTP();

                    http.storeClientInput(socketChannel.socket());
                    http.processClientInput();
                    http.setUpFileInputStream();
                    http.httpResponse(socketChannel.socket());

                    if (http.getWsKey() != null) {
                        String[] message = Message.readMessageAsDataFrame(socketChannel.socket()).split(" ");
                        System.out.println("Message from client: " + Arrays.toString(message));
                        if (message[0].equals(JOIN) && message.length > 1) {
                            if (!Room.roomListMap.containsKey(message[1])) {
                                new Room(message[1]);
                            }
                            Room.roomListMap.get(message[1]).joinRoom(socketChannel);

                        }

                    }

                } catch (IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            });
            thread.start();

        }

    }

}
