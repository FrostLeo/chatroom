package main.java.client;

import main.java.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;

import static main.java.NotificationFactory.*;


public class Client {
    public static void main(String[] args) {
        new Client().begin();
    }

    protected Connection connection;
    private volatile boolean clientConnected = false;

    public void begin() {
        initialDaemonThread();
        synchronized (this) {
            try {
                this.wait();
            } catch (Exception e) {
                ConsoleHelper.writeMessage(clientErrorNotification());
                System.exit(1);
            }
            showConnectedNotification();
            clientMainLoop();
        }
    }

    //создает и запускает сокет тред в качестве демона
    private void initialDaemonThread() {
        Thread currentThread = getSocketThread();
        currentThread.setDaemon(true);
        currentThread.start();
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    //выводит пользователю сообщение об удачном подключении или об ошибке
    private void showConnectedNotification() {
        if (clientConnected) {
            ConsoleHelper.writeMessage(clientConnectedNotification());
        } else {
            ConsoleHelper.writeMessage(clientErrorNotification());
        }
    }

    /*
    основной метод клиента - если подключение существует, ожидает сообщение из консоли
        если сообщение "exit" - прерывает цикл
        если другое - отправляет сообщение на сервер
    */
    private void clientMainLoop() {
        while (clientConnected) {
            String receivedText = ConsoleHelper.readString();

            if (receivedText
                    .toLowerCase(Locale.ROOT)
                    .equals("exit"))
                break;

            if (shouldSendTextFromConsole()) {
                sendTextMessage(receivedText);
            }
        }
    }

    //метод создан для будущего переопределения в случае наследования клиента (планируется клиент с GUI)
    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    public void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage(sendMessagesFailureNotification());
            ConsoleHelper.writeMessage(disconnectedNotification());
            clientConnected = false;
        }
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage(serverIPNotification());
        return ConsoleHelper.readServerAddress();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage(serverPortNotification());
        return ConsoleHelper.readNumServerPort();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage(nameRequestNotification());
        return ConsoleHelper.readString();
    }

    //внутренний класс, инкапсюлирующий соединение с сервером и обрабатывающий сообщения от сервера
    public class SocketThread extends Thread {
        @Override
        public void run() {
            String address = getServerAddress();
            int port = getServerPort();

            try {
                connection = new Connection(new Socket(address, port));
                clientHandshake();
                socketThreadMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " " + userConnectedNotification());
        }

        protected void informAboutDeletingUser(String userName) {
            ConsoleHelper.writeMessage(userName + " " + userDisconnectedNotification());
        }

        //устанавливает новое значение clientConnected и дает команду основному трэду на продолжение
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            Message inputMessage;
            do {
                inputMessage = connection.receive();
                MessageType messageType = inputMessage.getType();
                nameValidate(messageType);
            } while (inputMessage.getType() == MessageType.NAME_REQUEST);
        }

        /*
        Валидация нкинейма:
        - сервер запросил никнейм - запросить никнейм у клиента, отправить на сервер сообщение с никнеймом;
        - сервер подтвердил никнейм - отправить разрешение на коннект и продолжение основного трэда
        */
        private void nameValidate(MessageType messageType) throws IOException {
            switch (messageType) {
                case NAME_REQUEST -> {
                    String name = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, name));
                }
                case NAME_ACCEPTED -> notifyConnectionStatusChanged(true);
                default -> throw new IOException(messageTypeIncorrectNotification());
            }
        }


        //основной метод сокетТрэда - постоянно ожидает сообщения и обрабатывает их, в зависимости от типа
        protected void socketThreadMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message inputMessage = connection.receive();
                MessageType messageType = inputMessage.getType();
                String messageData = inputMessage.getData();
                processMessage(messageType, messageData);
            }
        }

        private void processMessage(MessageType messageType, String messageData) throws IOException {
            switch (messageType) {
                case TEXT -> processIncomingMessage(messageData);
                case USER_ADDED -> informAboutAddingNewUser(messageData);
                case USER_REMOVED -> informAboutDeletingUser(messageData);
                default -> throw new IOException(messageTypeIncorrectNotification());
            }
        }
    }
}
