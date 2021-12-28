package client;

import java.util.Optional;

public class BotResponseFactory {
    public static Optional<BotResponse> createResponse(String username, String userCommand) {
        switch (userCommand) {
            case "дата":
            case "год":
            case "месяц":
            case "день":
            case "время":
                return Optional.of(new DateBotResponse(username, userCommand));
            default:
                return findOtherMatches(username, userCommand);
        }
    }

    /*
    ищет совпадения с различными шаблонами
    в настоящий момент реализован только оин шаблон
    создан для дальнейшего заполнения шаблонами других команд для бота
     */
    private static Optional<BotResponse> findOtherMatches(String username, String userCommand) {
        Optional<BotResponse> result;
        result = findCurrencyMatches(username, userCommand);
        return result;
    }

    /*
    ищет совпадения с шаблоном команды поиска курса валюты
    при совпадении возвращает новый объект с ответом
    при несовпадении возвращает пустой контейнер
     */
    private static Optional<BotResponse> findCurrencyMatches(String username, String userCommand) {
        if (isCurrencyMatches(userCommand)) {

            //извлекаем обозначение валюты из команды
            String currency = userCommand.substring(5);
            return Optional.of(new CurrencyBotResponse(username, currency));
        }

        return Optional.empty();
    }

    //проверяет на совпадение с шаблоном "курс " + "три латинские буквы независимо от регистра"
    private static boolean isCurrencyMatches(String userCommand) {
        String regEx = "^курс [A-z]{3}$";
        return userCommand.matches(regEx);
    }
}
