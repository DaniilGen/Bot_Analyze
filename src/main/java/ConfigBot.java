import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ConfigBot extends TelegramLongPollingBot {
    public static String TOKEN = "5128582945:AAGF2aAAJeKr6sysosq36K0KeB1Tb5aE9Fk";
    public static String NAME = "AnalyzePhotoBot";


    @Override
    public String getBotUsername() {
        return NAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            if (message_text.equals("/start")){
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chat_id));
                message.setText("""
                        Привет! Меня зовут Аналайзер! Я бот, который поможет Вам просканировать фотографию и узнать, что же на ней??\s
                        А также я могу конвертировать текст с вашей фотографии!!!\s
                        Для начала работы введи /photo""");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else
                if (message_text.equals("/photo")){
                    SendMessage message = new SendMessage();
                    message.setChatId(String.valueOf(chat_id));
                    message.setText("Пожалуйста, отправьте фотографию \uD83D\uDE04");
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            System.out.println();
            System.out.println(message_text);
            System.out.println();
        }
        if (update.hasMessage() && update.getMessage().hasPhoto()){
            List<PhotoSize> photos = update.getMessage().getPhoto();
            long chat_id = update.getMessage().getChatId();
            String f_id = Objects.requireNonNull(photos.stream().max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null)).getFileId();

            GetFile getFile = new GetFile();
            getFile.setFileId(f_id);
            String path= "C:\\Users\\Даниил\\IdeaProjects\\Bot_Analyze\\src\\main\\resources\\recent"+f_id+".jpg";
            try {
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                downloadFile(file, new File(path));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            Analyze google=new Analyze();
            List <String> descr =new ArrayList <> ();
            try {
                descr=google.search(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder ans= new StringBuilder();
            for(String s:descr){
                ans.append(s).append("\n");
            }
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chat_id));
            message.setText("❗️Я думаю что это \uD83E\uDD14 \n \n"+ans);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
