package client;

abstract public class BotResponse {
    protected String userName;
    protected String userCommand;

    public BotResponse(String username, String userCommand) {
        this.userName = username;
        this.userCommand = userCommand;
    }

    abstract public String getResponse();
}
