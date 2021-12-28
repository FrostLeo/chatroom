package client;

import java.io.IOException;
import java.util.Optional;

import static util.PropertiesManager.botIntroNotification;

public class InfoBot extends Client {

    public static void main(String[] args) {
        new InfoBot().begin();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    //боту не нужно отправлять сообщения в консоль
    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "info_bot";
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void socketThreadMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage(botIntroNotification());
            super.socketThreadMainLoop();
        }

        /*
        процесс обработки сообщения:
        - разделяет входящую строку на имя пользователя и потенциальную команду
        (в аргументы переопределенного метода не может попасть входящая строка без символа ":",
        потому что метод processMessage внутреннего класса socketThread класса Client
        сортирует сообщения с MessageType == TEXT, которые собраны методом getSendLine класса Server);
        - запускает абстрактную фабрику, создающую контейнер с соответствующим BotResponse
        в случае обнаружения команды боту или пустой контейнер, в случае отсутствия какой-либо команды;
        - если возвращаемый контейнер содержит BotResponse - формирует ответ и отправляет его на сервер.
         */
        @Override
        protected void processIncomingMessage(String message) {
//            в общем случае для бота выводить входящее сообщение в консоль не требуется,
//            но при необходимости можно раскомментить:
//            ConsoleHelper.writeMessage(message);

            String[] splittedMessage = message.split(": ");
            String userName = splittedMessage[0];
            String userCommand = splittedMessage[1].trim();

            Optional<BotResponse> response = BotResponseFactory.createResponse(userName, userCommand);
            response.ifPresent(botResponse -> sendTextMessage(botResponse.getResponse()));
        }

        //для бота информация о добавлении пользователя в чат не требуется
        @Override
        protected void informAboutAddingNewUser(String userName) {
        }

        //для бота информация об удалении пользователя из чата не требуется
        @Override
        protected void informAboutDeletingUser(String userName) {
        }
    }
}
