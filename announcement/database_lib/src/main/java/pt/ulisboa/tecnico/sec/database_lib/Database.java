package pt.ulisboa.tecnico.sec.database_lib;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private Connection _con;

    public Database() {  
        try{  
            Class.forName("com.mysql.jdbc.Driver");  
            _con=DriverManager.getConnection("jdbc:mysql://localhost:3306/announcement","sec","1234");

            resetDatabase();
            createGeneralBoardTable();
        }
        catch(Exception e) {
            System.out.println(e);
        }  
    }

    public void createGeneralBoardTable() {
        try {
            String generalBoardTable = "CREATE TABLE IF NOT EXISTS GeneralBoard (Announcement VARCHAR(256) NOT NULL, Reference VARBINARY(256), AnnouncementID INT(8) NOT NULL, ClientUUID VARCHAR(255) NOT NULL, Seq INT AUTO_INCREMENT ,PRIMARY KEY(Seq)) CHARACTER SET utf8";
            PreparedStatement statement = _con.prepareStatement(generalBoardTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void createUserTable(String uuid) {
        try {
            String userTable = "CREATE TABLE IF NOT EXISTS " + uuid + " (Announcement VARCHAR(256) NOT NULL, Reference VARBINARY(256), AnnouncementID INT(8) NOT NULL, PRIMARY KEY(AnnouncementID)) CHARACTER SET utf8";
            PreparedStatement statement = _con.prepareStatement(userTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void dropGeneralBoardTable() {
        try {
            String generalBoardTable = "DROP TABLE GeneralBoard";
            PreparedStatement statement = _con.prepareStatement(generalBoardTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void dropUserTable(String uuid) {
        try {
            String userTable = "DROP TABLE " + uuid;
            PreparedStatement statement = _con.prepareStatement(userTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void resetDatabase() {
        try {
            String dropDatabase = "DROP DATABASE IF EXISTS announcement";
            String createDatabase = "CREATE DATABASE IF NOT EXISTS announcement";
            String useDatabase = "USE announcement";
            PreparedStatement statement = _con.prepareStatement(dropDatabase);
            statement.executeUpdate();
            statement = _con.prepareStatement(createDatabase);
            statement.executeUpdate();
            statement = _con.prepareStatement(useDatabase);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public int insertAnnouncementGB(String announcememnt, byte[] reference, int announcementID, String clientUUID) {
        try {
            String messageGB = "INSERT INTO GeneralBoard(Announcement, Reference, AnnouncementID, ClientUUID) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = _con.prepareStatement(messageGB);
            statement.setString(1, announcememnt);
            statement.setBytes(2, reference);
            statement.setInt(3, announcementID);
            statement.setString(4, clientUUID);

            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    public int insertAnnouncement(String announcememnt, byte[] reference, int announcementID, String clientTableName) {
        try {
            String messageGB = "INSERT INTO " + clientTableName + "(Announcement, Reference, AnnouncementID) VALUES (?, ?, ?)";
            PreparedStatement statement = _con.prepareStatement(messageGB);
            statement.setString(1, announcememnt);
            statement.setBytes(2, reference);
            statement.setInt(3, announcementID);

            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    public List<Announcement> getGBAnnouncements(int n) {
        List<Announcement> l = new ArrayList<Announcement>();
        try {
            String query = "SELECT * FROM GeneralBoard ORDER BY Seq DESC LIMIT " + Integer.toString(n);
            PreparedStatement preparedStatement = _con.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                ArrayList<Integer> references = (ArrayList<Integer>) ProtocolMessageConverter.byteArrayToObj(rs.getBytes(2));
                Announcement a = new Announcement(rs.getString(1), references, rs.getInt(3), rs.getString(4));
                l.add(0, a);
            }
        }
        catch (SQLException e) {
            System.out.println(e);
        }
        return l;
    } 

    public void closeConnection() {
        try {
            _con.close();
        }
        catch(Exception e) {
            System.out.println(e);
            System.out.println("Could not close connection.");
        }  
    }

}