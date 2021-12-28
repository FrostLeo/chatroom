package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static util.PropertiesManager.parsingFailureNotification;

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {
        System.out.println(message);
    }

    public static String readString() {
        String result = null;

        while (result == null) {
            try {
                result = reader.readLine();
            } catch (IOException e) {
                System.out.println(parsingFailureNotification());
            }
        }
        return result;
    }

    // рекурсивный метод для введения пользователем валидного адреса сервера
    // при неверных значениях адреса заставляет повторить ввод
    public static String readServerAddress() {
        String address = readString();
        if (isValidAddress(address)) {
            return address;
        }
        System.out.println(parsingFailureNotification());
        return readServerAddress();
    }

    /*
    делает проверку на валидность введенного адреса.
    возвращает false на null и пустое поле ввода
    возвращает true на localhost
    при помощи InetAddress парсит введенный URL или IP
    если при этом не бросает эксепшн - true
     */
    private static boolean isValidAddress(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        if (address.equals("localhost")) {
            return true;
        }

        try {
            InetAddress.getByName(address);
            return true;
        } catch (UnknownHostException e) {
            //особого поведения не требуется
        }
        return false;
    }

    // метод для введения валидного номера порта сервера,
    // при неверных значениях порта заставляет повторить ввод
    public static int readNumServerPort() {
        try {
            Integer port = Integer.parseInt(readString());
            if (isValidPort(port))
                return port;
        } catch (NumberFormatException e) {
            //особого поведения не требуется
        }
        System.out.println(parsingFailureNotification());
        return readNumServerPort();
    }

    // проверяет, чтобы номер порта укладывался в числовой диапазон возможных портов
    private static boolean isValidPort(Integer port) {
        return port != null
                && port <= 65535
                && port >= 1;
    }
}