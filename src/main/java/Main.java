import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//import org.telegram.telegrambots.ApiContextInitializer;
import org.apache.log4j.BasicConfigurator;

public class Main {
    public static void main(String[] args) {
//        Analyze google=new Analyze();
        BasicConfigurator.configure();
//        Ap
        try {
            TelegramBotsApi botsApi= new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new ConfigBot());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
