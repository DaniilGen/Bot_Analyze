
import java.io.*;
import java.sql.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SQL {

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            String connectionUrl = "jdbc:sqlite:C:\\Users\\Даниил\\Documents\\Data_Bases\\Telegram_Bot.db";
            connection = DriverManager.getConnection(connectionUrl);
        } else {
            connection.close();
            String connectionUrl = "jdbc:sqlite:C:\\Users\\Даниил\\Documents\\Data_Bases\\Telegram_Bot.db";
            connection = DriverManager.getConnection(connectionUrl);
        }
        return connection;
    }


    public void checkUser(long telegram_id, long chat_id, String first_name, String username) {
        String SQL = "SELECT chat_id FROM Main_Info WHERE telegram_id=?";
        try {
            connection = getConnection();
            PreparedStatement request = connection.prepareStatement(SQL);
            request.setLong(1, telegram_id);
            ResultSet rs = request.executeQuery();
            if (!rs.next()) {
                SQL = "INSERT INTO Main_Info (telegram_id,chat_id,first_name,username) VALUES (%d,%d,%s,%s)".formatted(telegram_id, chat_id, first_name, username);
                request = connection.prepareStatement(SQL);
                request.executeUpdate();
            }
            rs.close();
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void readPicture(long telegram_id, String time) {
        String SQL = "SELECT photo FROM Users_Data WHERE telegram_id=? and time_request=%s".formatted(time);
        String filename = "C:\\Users\\Даниил\\IdeaProjects\\Bot_Analyze\\src\\main\\resources\\BLOB.jpg";
        try {
            connection = getConnection();
            PreparedStatement request = connection.prepareStatement(SQL);
            request.setLong(1, telegram_id);
            ResultSet rs = request.executeQuery();
            File file = new File(filename);
            FileOutputStream fos = new FileOutputStream(file);
            while (rs.next()) {
                InputStream input = rs.getBinaryStream("photo");
                byte[] buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    fos.write(buffer);
                }
            }
            fos.close();
            rs.close();
            connection.close();
        } catch (SQLException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertPicture(long telegram_id, String filename, String answer, String time) {
        String SQL = "INSERT INTO Users_Data (telegram_id,photo,bot_answer,time_request) " + "VALUES (%d,?,%s,%s)".formatted(telegram_id, answer, time);
        System.out.println(answer);
        System.out.println(time);
        try {
            connection = getConnection();
            File image = new File(filename);
            int num_rows = 0;
            FileInputStream fis = new FileInputStream(image);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                bos.write(buf, 0, readNum);
            }
            fis.close();
            PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setBytes(1, bos.toByteArray());
            num_rows = ps.executeUpdate();
            if (num_rows > 0) {
                System.out.println("Photo insert");
            }
            bos.close();
            ps.close();
            connection.close();
            System.out.println(connection);
        } catch (Exception e) {
            System.out.println(e);
        }


    }

    public List<String> dateHistory(long telegram_id) {
        String SQL = "SELECT time_request FROM Users_Data WHERE telegram_id=? ORDER BY time_request DESC LIMIT 5";
        List<String> info = new ArrayList<>();
        try {
            connection = getConnection();
            PreparedStatement request = connection.prepareStatement(SQL);
            request.setLong(1, telegram_id);
            ResultSet rs = request.executeQuery();
            int i = 0;
            while (rs.next()) {
                ++i;
                info.add(i + ") " + rs.getString("time_request") + "\n");
            }
            rs.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return info;
    }

    public List<String> History(long telegram_id) {
        String SQL = "SELECT * FROM Users_Data WHERE telegram_id=? ORDER BY time_request DESC LIMIT 5";
        List<String> info = new ArrayList<>();
        try {
            connection = getConnection();
            PreparedStatement request = connection.prepareStatement(SQL);
            request.setLong(1, telegram_id);
            ResultSet rs = request.executeQuery();
            while (rs.next()) {
                info.add(rs.getString("time_request") + "@" + rs.getString("bot_answer"));
            }
            rs.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return info;
    }

}
