import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

class Message {

    static final String CLOSE = "Close";
    private static final String USER = "{\"user\":\"";
    private static final String MESSAGE = "\",\"message\":\"";
    private static final String JSON_END = "\"}";

    static String readMessageAsDataFrame(Socket socket) throws IOException {


        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

        byte byte1;
        try {
            byte1 = dataInputStream.readNBytes(1)[0];
        } catch (Exception e) {
            e.printStackTrace();
            return CLOSE;
        }

        assert ((byte1 & 0b11110000) == 0b10000000);
        byte messageType = (byte) (byte1 & 0b00001111);

        if (messageType == 0x8) {
            socket.close();
            return CLOSE;
        }

        byte byte2 = dataInputStream.readNBytes(1)[0];
        assert ((byte2 & 0b10000000) == 0b10000000);

        int messageLength;
        if ((byte2 & 0b01111111) == 126) {
            messageLength = dataInputStream.readShort();
        } else if ((byte2 & 0b01111111) == 127) {
            messageLength = (int) dataInputStream.readLong();
        } else {
            messageLength = (byte2 & 0b01111111);
        }

        byte[] maskingKey = dataInputStream.readNBytes(4);
        byte[] ENCODED = dataInputStream.readNBytes(messageLength);
        byte[] DECODED = new byte[messageLength];
        for (int i = 0; i < messageLength; i++) {
            DECODED[i] = (byte) (ENCODED[i] ^ maskingKey[i % 4]);
        }

        return new String(DECODED);


    }

    static void sendMessageAsDataFrame(String message, Socket socket) throws IOException {

        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        String[] splitMessage = message.split(" ", 2);

        if (splitMessage.length == 2) {


            message = USER + splitMessage[0] + MESSAGE + splitMessage[1] + JSON_END;

            dataOutputStream.writeByte((byte) 0b10000001);

            if (message.length() > 125 && message.length() < Short.MAX_VALUE) {
                dataOutputStream.writeByte(126);
                dataOutputStream.writeShort(message.length());

            } else if (message.length() > Short.MAX_VALUE) {
                dataOutputStream.writeByte(127);
                dataOutputStream.writeLong(message.length());

            } else {
                dataOutputStream.writeByte(message.getBytes().length);
            }

            dataOutputStream.write(message.getBytes());
            dataOutputStream.flush();
        }

    }

}
