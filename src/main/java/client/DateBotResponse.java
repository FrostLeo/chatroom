package client;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static util.PropertiesManager.botInfoNotification;

public class DateBotResponse extends BotResponse {
    public DateBotResponse(String userName, String userCommand) {
        super(userName, userCommand);
    }

    /*
    в зависимости от обнаруженной команды выбирает требуемый формат
    и возвращает сформированную строку с ответом
    (других вариантов switch-case быть не может, потому что они отсечены при выполнении метода
    getResponse класса BotResponceFactory, так что default case избыточен)
     */
    @Override
    public String getResponse() {
        String dateFormat;
        switch (userCommand) {
            case "дата":
                dateFormat = "d.MM.YYYY";
                break;
            case "день":
                dateFormat = "d";
                break;
            case "месяц":
                dateFormat = "MMMM";
                break;
            case "год":
                dateFormat = "YYYY";
                break;
            case "время":
            default:
                dateFormat = "H:mm:ss";
                break;
        }
        String data = createResponseData(dateFormat);
        return concatResponseLine(data);
    }

    // создает соответствующую запрошенному формату строку с данными
    private String createResponseData(String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Calendar currentCalendar = new GregorianCalendar();
        simpleDateFormat.setTimeZone(currentCalendar.getTimeZone());
        return simpleDateFormat.format(currentCalendar.getTime());
    }

    // собирает строку для предоставления ответа пользователю
    private String concatResponseLine(String date) {
        return new StringBuilder(botInfoNotification())
                .append(userName)
                .append(": ")
                .append(date)
                .toString();
    }
}
