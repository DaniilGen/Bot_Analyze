import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class ConfigBot extends TelegramLongPollingBot {
    public static String TOKEN = "";
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
        long chat_id = update.getMessage().getChatId();
        long telegram_id = update.getMessage().getFrom().getId();
        String first_name = update.getMessage().getFrom().getFirstName();
        String username = update.getMessage().getFrom().getUserName();
        SQL sqlRequests = new SQL();
        sqlRequests.checkUser(telegram_id, chat_id, "'" + first_name + "'", "'" + username + "'");
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            if (message_text.equals("/start")) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chat_id));
                message.setText("""
                        Привет, %s!  Меня зовут Аналайзер! Я бот, который поможет Вам просканировать фотографию и узнать, что же на ней??\s
                        А также я могу конвертировать текст с вашей фотографии!!!\s
                        Для начала работы введите /photo
                        Если хотите посмотреть историю Ваших сообщений напишите /history""".formatted(first_name));
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (message_text.equals("/photo")) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chat_id));
                message.setText("Пожалуйста, отправьте фотографию \uD83D\uDE04");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            } else if (message_text.equals("/history")) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chat_id));
                List<String> info;
                info = sqlRequests.dateHistory(telegram_id);
                message.setText("Пожалуйста, напишите номер даты Вашего сообщения (от 1 до 5) в формате 'Давай 2' \uD83D\uDE04 \n" + String.join(" ", info));
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }


            } else if (message_text.contains("Давай")) {
                String[] text = message_text.split(" ");
                int nomer = Integer.parseInt(text[1]) - 1;
                List<String> info;
                info = sqlRequests.History(telegram_id);
                if (nomer < info.size()) {
                    String[] data = info.get(nomer).split("@");
                    sqlRequests.readPicture(telegram_id, "'" + data[0] + "'");
                    SendPhoto message = new SendPhoto();
                    message.setChatId(String.valueOf(chat_id));
                    File photo1 = new File("C:\\Users\\Даниил\\IdeaProjects\\Bot_Analyze\\src\\main\\resources\\BLOB.jpg");
                    InputFile photo = new InputFile(photo1, "sfffssf");
                    message.setPhoto(photo);
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    SendMessage message1 = new SendMessage();
                    message1.setChatId(String.valueOf(chat_id));
                    message1.setText(data[1]);
                    try {
                        execute(message1);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(String.valueOf(chat_id));
                    message.setText("Извините, Я не понял \uD83E\uDD7A");
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chat_id));
                message.setText("Извините, Я не понял \uD83E\uDD7A");
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
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chat_id));
            message.setText("Пожалуйста, подождите ! \uD83E\uDD2A");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            long time = update.getMessage().getDate();
            ZoneId z = ZoneId.of("Europe/Moscow");
            Instant instant = Instant.ofEpochSecond(time);
            ZonedDateTime zonedDateTime = instant.atZone(z);
            String timeConvert = "'" + zonedDateTime.getDayOfMonth() + "-" + zonedDateTime.getMonth() + "-" + zonedDateTime.getYear() + "; " + zonedDateTime.getHour() + ":" + zonedDateTime.getMinute() + ":" + zonedDateTime.getSecond() + "'";

            List<PhotoSize> photos = update.getMessage().getPhoto();
            String f_id = Objects.requireNonNull(photos.stream().max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null)).getFileId();

            GetFile getFile = new GetFile();
            getFile.setFileId(f_id);
            String path = "C:\\Users\\Даниил\\IdeaProjects\\Bot_Analyze\\src\\main\\resources\\recent" + f_id + ".jpg";
            try {
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                downloadFile(file, new File(path));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }


            Analyze google = new Analyze();
            List<String> descr = new ArrayList<>();
            try {
                descr = google.search(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder ans = new StringBuilder();
            for (String s : descr) {
                ans.append(s).append("\n");
            }
            message = new SendMessage();
            message.setChatId(String.valueOf(chat_id));
            message.setText("❗️Я думаю что это \uD83E\uDD14 \n \n" + ans);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            String ansConvert = ans.toString().replaceAll("'", "`");
            sqlRequests.insertPicture(telegram_id, path, "'" + ansConvert + "'", timeConvert);
        }
    }
}
