package util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class PropertiesManager {
    private static Properties prop = new Properties();

    static {
        try {
            prop.load(new FileReader("./src/main/resources/.properties"));
        } catch (IOException e) {
            System.out.println("Отсутствует файл /src/main/resources/.properties. Программа будет закрыта");
            System.exit(1);
        }
    }

    public static String parsingFailureNotification() {
        return prop.getProperty("parsingFailure");
    }

    public static String sendMessagesFailureNotification() {
        return prop.getProperty("sendMessagesFailure");
    }

    public static String messageTypeIncorrectNotification() {
        return prop.getProperty("messageTypeIncorrect");
    }

    public static String serverStartedNotification() {
        return prop.getProperty("serverStarted");
    }

    public static String serverPortNotification() {
        return prop.getProperty("serverPort");
    }

    public static String serverIPNotification() {
        return prop.getProperty("serverIP");
    }

    public static String nameRequestNotification() {
        return prop.getProperty("nameRequest");
    }

    public static String nameAcceptedNotification() {
        return prop.getProperty("nameAccepted");
    }

    public static String nameIncorrectNotification() {
        return prop.getProperty("nameIncorrect");
    }

    public static String connectedNotification() {
        return prop.getProperty("connected");
    }

    public static String disconnectedNotification() {
        return prop.getProperty("disconnected");
    }

    public static String userConnectedNotification() {
        return prop.getProperty("userConnected");
    }

    public static String userDisconnectedNotification() {
        return prop.getProperty("userDisconnected");
    }

    public static String clientConnectedNotification() {
        return prop.getProperty("clientConnected");
    }

    public static String clientErrorNotification() {
        return prop.getProperty("clientError");
    }

    public static String botIntroNotification() {
        return prop.getProperty("botIntro");
    }

    public static String botInfoNotification() {
        return prop.getProperty("botInfo");
    }

    public static String botCurrencyJsonHttpAddress() {
        return prop.getProperty("jsonCurrencyHttpAddress");
    }

    public static String botJsonNotFoundNotification() {
        return prop.getProperty("jsonCurrencyNotFound");
    }
}
