package pt.ulisboa.tecnico.sec.database_lib;
import java.sql.*;

public class Database {
    private Connection _con;

    public Database() {  
        try{  
            Class.forName("com.mysql.jdbc.Driver");  
            _con=DriverManager.getConnection("jdbc:mysql://localhost:3306/announcement","sec","1234");  
        }
        catch(Exception e) {
            System.out.println(e);
        }  
    }

    public void createGeneralBoardTable() {
        try {
            String generalBoardTable = "CREATE TABLE IF NOT EXISTS GeneralBoard (Message VARCHAR(256) NOT NULL, Reference VARCHAR(256), Id INT(8) NOT NULL, PRIMARY KEY(Id)) CHARACTER SET utf8";
            PreparedStatement statement = _con.prepareStatement(generalBoardTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void createUserTable(String uuid) {
        try {
            String userTable = "CREATE TABLE IF NOT EXISTS " + uuid + " (Message VARCHAR(256) NOT NULL, Reference VARCHAR(256), Id INT(8) NOT NULL, PRIMARY KEY(Id)) CHARACTER SET utf8";
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
            PreparedStatement statement = _con.prepareStatement(dropDatabase);
            statement.executeUpdate();
            statement = _con.prepareStatement(createDatabase);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public int insertMessageGB(int publicKey, String message, String reference, int id) {
        try {
            String messageGB = "INSERT INTO GeneralBoard VALUES (?, ?, ?, ?)";
            PreparedStatement statement = _con.prepareStatement(messageGB);
            statement.setInt(1, publicKey);
            statement.setString(2, message);
            statement.setString(3, reference);
            statement.setInt(4, id);

            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
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