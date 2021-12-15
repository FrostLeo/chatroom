package main.java;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream (socket.getInputStream());
    }

    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
            out.flush();
        }
    }

    public Message receive() throws IOException {
        Message result = null;
        try {
            synchronized (in) {
                result = (Message) in.readObject();
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}