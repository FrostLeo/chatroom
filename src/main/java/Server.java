package main.java;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static main.java.NotificationFactory.*;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println(serverPortNotification());
        int serverPort = ConsoleHelper.readNumServerPort();

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println(serverStartedNotification());

            //в цикле сервер принимает входящие подключения и создает для каждого из них
            //отдельный трэд-хэндлер
            while (true) {
                Socket inputSocket = serverSocket.accept();
                Handler currentHandler = new Handler(inputSocket);
                currentHandler.start();
            }
        } catch (Exception e) {
            System.out.println();
            e.printStackTrace();
        }
    }

    //отправляет сообщение всем участника чата
    private static void sendBroadcastMessage(Message message) {
        connectionMap.forEach((s, c) -> {
            try {
                c.send(message);
            } catch (IOException e) {
                System.out.println(sendMessagesFailureNotification());
            }
        });
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        //отправляет участнику чата сообщения об остальных присутствующих в чате участниках
        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String str : connectionMap.keySet()) {
                if (!userName.equals(str)) {
                    connection.send(new Message(MessageType.USER_ADDED, str));
                }
            }
        }

        /*
        основной метод чата - ожидает сообщения, слушая коннекты
            - если участник отправляет текстовое сообщение - рассылает его всем участникам чата;
            - если отправляется что-то кроме текстового сообщения - показывает сообщение об ошибке
         */
        private void serverMainLoop(Connection connection, String userName)
                throws IOException, ClassNotFoundException {

            while (true) {
                Message message = connection.receive();

                if (isTextType(message)) {
                    String sendOutToAll = getSendLine(userName, message);
                    sendBroadcastMessage(new Message(MessageType.TEXT, sendOutToAll));
                } else {
                    ConsoleHelper.writeMessage(messageTypeIncorrectNotification());
                }
            }
        }

        private boolean isTextType(Message message) {
            return message.getType() == MessageType.TEXT;
        }

        //формирует строку для отправки
        private String getSendLine(String userName, Message message) {
            return userName + ": " + message.getData();
        }

        //регистрация на сервере чата
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message userAnswer = requestUserName(connection, nameRequestNotification());
            String resultUserName = recursionUserNameRequest(connection, userAnswer);
            connectionMap.put(resultUserName, connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED, nameAcceptedNotification()));
            return resultUserName;
        }

        //метод запрашивает у пользователя имя для регистрации на сервере чата
        private Message requestUserName(Connection connection, String messageText) throws IOException {
            Message result;
            connection.send(new Message(MessageType.NAME_REQUEST, messageText));
            result = connection.receive();
            return result;
        }

        /*
        рекурсивный метод для регистрации на сервере чата
        если выбранное имя нелегально, то рекурсивно происходит повторный вызов с запросом нового имени
         */
        private String recursionUserNameRequest(Connection connection, Message userAnswer) throws IOException {
            String result = userAnswer.getData();
            if (isIllegalName(userAnswer, result)) {
                userAnswer = requestUserName(connection, nameIncorrectNotification());
                result = recursionUserNameRequest(connection, userAnswer);
            }
            return result;
        }

        /*
        проверка выбранного имени, возвращает true, еслм имя:
            - не соответствует требуемому формату;
            - пустое;
            - уже присутствует среди участников чата,
         */
        private boolean isIllegalName(Message answer, String result) {
            return answer.getType() != MessageType.USER_NAME
                    || answer.getData().isEmpty()
                    || connectionMap.containsKey(result);
        }

        @Override
        public void run() {
            String socketAddress = socket
                    .getRemoteSocketAddress()
                    .toString();
            ConsoleHelper.writeMessage(connectedNotification() + socketAddress);
            openConnection();
            ConsoleHelper.writeMessage(disconnectedNotification() + socketAddress);
        }

        private void openConnection() {
            String userName = null;
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);

                //рассылаем остальным участникам чата сообщение о новичке
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));

                //оповещаем нового участника о присутствующих в чате
                notifyUsers(connection, userName);

                //ожидаем сообщения в основном методе
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException ex) {
//                ConsoleHelper.writeMessage(ioErrorNotification() + socketAddress);
            } finally {
                if (kick(userName)) {
                    //рассылаем остальным сообщение об удалении участника
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
            }
        }
    }

    /*
    удаляет участника из мапы, если он был внесен
        (ситуация, когда имя пользователя есть, а пользователя в мапе нет - не может произойти,
        поскольку пользователя в мапу вносит метод serverHandshake(connection),
        который возвращает имя пользователя в переменную userName только при успешном завершении)
     */
    private static boolean kick(String userName) {
        if (userName != null) {
            connectionMap.remove(userName);
            return true;
        }
        return false;
    }
}
