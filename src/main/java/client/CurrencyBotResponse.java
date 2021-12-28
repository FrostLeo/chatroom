package client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Locale;

import static util.PropertiesManager.*;

public class CurrencyBotResponse extends BotResponse {
    private String currency;

    public CurrencyBotResponse(String userName, String userCommand) {
        super(userName, userCommand);
        currency = userCommand.toUpperCase(Locale.ROOT);
    }

    // получает данные по валюте, собирает и возвращает строку для пользователя
    @Override
    public String getResponse() {
        String rate = getExchangeRate();
        return concatResponseLine(rate);
    }

    // получает JSON объект из строки и пытается парсить из него курс валюты
    // при неудаче возвращает null
    private String getExchangeRate() {
        try {
            String jsonData = getJsonData();
            JSONObject obj = new JSONObject(jsonData);
            JSONObject valute = obj.getJSONObject("Valute");
            JSONObject jsonCurrency = valute.getJSONObject(currency);
            return jsonCurrency.get("Value").toString();
        } catch (JSONException | IOException e) {
            return null;
        }
    }

    // получает данные по запросу, отправленному по созданному HTTP соединению и сохраняет их в строку
    private String getJsonData() throws IOException {
        String address = botCurrencyJsonHttpAddress();
        HttpURLConnection connection = openHttpConnection(address);
        setHttpRequestGetJson(connection);

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        }
    }

    // открывает HTTP соединение c переданным в строке URL
    private HttpURLConnection openHttpConnection(String address) throws IOException {
        URL url = new URL(address);
        return (HttpURLConnection) url.openConnection();
    }

    // устанавливает параметры GET запроса по созданному HTTP соединению
    private void setHttpRequestGetJson(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
    }

    // собирает строку для предоставления ответа пользователю в зависимости от того,
    // было получено значения курса или нет
    private String concatResponseLine(String rate) {
        if (rate == null) {
            return new StringBuilder(botInfoNotification())
                    .append(userName)
                    .append(": ")
                    .append(botJsonNotFoundNotification())
                    .append(currency)
                    .toString();
        }
        return new StringBuilder(botInfoNotification())
                .append(userName)
                .append(": 1")
                .append(currency)
                .append(" = ")
                .append(rate)
                .append("RUB")
                .toString();
    }
}
