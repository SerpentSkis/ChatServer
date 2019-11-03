import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

class Room {
    static final HashMap<String, Room> roomListMap = new HashMap<>();
    private final ArrayList<Socket> connectedUsers;
    private final ArrayList<Socket> userConnectionQueue = new ArrayList<>(1);
    private final ArrayList<String> messageListToBeSent;
    private final ArrayList<String> messageHistory;
    private Selector selector;

    Room(String name) throws IOException {
        roomListMap.put(name, this);
        this.connectedUsers = new ArrayList<>(16);
        this.messageListToBeSent = new ArrayList<>(8);
        this.messageHistory = new ArrayList<>(128);

        selector = Selector.open(); //basically a constructor call

        Thread roomThread = new Thread(() -> {
            try {
                serveRoom();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        roomThread.start();
    }

    private void serveRoom() throws IOException {
        // noinspection InfiniteLoopStatement
        while (true) {

            selector.select(); //blocks until any of the monitored channels are ready
            Set<SelectionKey> setOfReadyKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = setOfReadyKeys.iterator();
            String message;
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isReadable()) {
                    keyIterator.remove();

                    //use the actual channel now we know it does what we want
                    SocketChannel readableChannel = (SocketChannel) key.channel();

                    //we have to remove the channel from the selector before we can make it blocking
                    key.cancel();
                    readableChannel.configureBlocking(true);

                    message = Message.readMessageAsDataFrame(readableChannel.socket());
                    if (message.equals(Message.CLOSE)) {
                        readableChannel.close();
                        connectedUsers.remove(readableChannel.socket());

                    } else {
                        messageListToBeSent.add(message);
                        messageHistory.add(message);

                        readableChannel.configureBlocking(false);
                        selector.selectNow(); //necessary to make the selector happy
                        readableChannel.register(selector, SelectionKey.OP_READ); //have the selector notify me when this channel can read again.
                    }

                }
            }
            postMessages(messageListToBeSent, connectedUsers);
            messageListToBeSent.clear();
            addUsersToRoom();

        }

    }

    synchronized private void postMessages(ArrayList<String> messageList, ArrayList<Socket> userList) throws IOException {
        for (Socket user : userList) {

            SocketChannel socketChannel = user.getChannel();
            socketChannel.keyFor(selector).cancel();
            socketChannel.configureBlocking(true);

            for (String message : messageList) {
                Message.sendMessageAsDataFrame(message, user);
                System.out.println("Message sent out is: " + message);
                System.out.println("There are this many users connected: " + userList.size());
            }

            socketChannel.configureBlocking(false);
            selector.selectNow();
            socketChannel.register(selector, SelectionKey.OP_READ);

        }

    }

    synchronized private void addUsersToRoom() throws IOException {
        for (Socket socket : userConnectionQueue) {

            SocketChannel channel = socket.getChannel();
            channel.configureBlocking(false);
            selector.selectNow();
            channel.register(selector, SelectionKey.OP_READ);

            connectedUsers.add(socket);
            postMessages(messageHistory, userConnectionQueue);

        }
        userConnectionQueue.clear();


    }

    synchronized void joinRoom(SocketChannel socketChannel) {
        userConnectionQueue.add(socketChannel.socket());
        selector.wakeup();

    }


}
