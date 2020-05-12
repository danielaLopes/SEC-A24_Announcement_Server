package pt.ulisboa.tecnico.sec.database_lib;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static Connection _con;
    private static String _databaseName;

    public Database(String db) {  
        try{  
            Class.forName("com.mysql.cj.jdbc.Driver");
            _con=DriverManager.getConnection("jdbc:mysql://localhost:3306/announcement?verifyServerCertificate=false&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true","sec","1234");
            _databaseName = db;
            resetDatabase();
            createRegularRegisterNNTable();
            createUsersTable();
        }
        catch(Exception e) {
            System.out.println(e);
        }  
    }

    public void createRegularRegisterNNTable() {
        try {
            String generalBoardTable = "CREATE TABLE IF NOT EXISTS RegularRegisterNN (ID MEDIUMINT NOT NULL AUTO_INCREMENT, Announcements VARBINARY(60000) NOT NULL, PRIMARY KEY(ID)) CHARACTER SET utf8";
            PreparedStatement statement = _con.prepareStatement(generalBoardTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void createUsersTable() {
        try {
            String usersTable = "CREATE TABLE IF NOT EXISTS Users (PublicKey VARBINARY(2000) NOT NULL," +
                                "ClientUUID VARCHAR(256) NOT NULL," +
                                "AtomicRegister1N VARBINARY(20000)," +
                                "ClientMessageHandler VARBINARY(20000)," +
                                "Token VARCHAR(256)," + 
                                "PRIMARY KEY(PublicKey, ClientUUID)) CHARACTER SET utf8";
            PreparedStatement statement = _con.prepareStatement(usersTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }


    public void createUserTable(String uuid) {
        try {
            String userTable = "CREATE TABLE IF NOT EXISTS " + uuid + " (Announcement VARCHAR(256) NOT NULL, Reference VARBINARY(256), AnnouncementID VARCHAR(255) NOT NULL, Accountability VARBINARY(2500) NOT NULL, Seq INT AUTO_INCREMENT, PRIMARY KEY(Seq)) CHARACTER SET utf8";
            PreparedStatement statement = _con.prepareStatement(userTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void dropDatabase() {
        try {
            String dropDatabase = "DROP DATABASE IF EXISTS " + _databaseName;
            PreparedStatement statement = _con.prepareStatement(dropDatabase);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void dropRegularRegisterNNable() {
        try {
            String generalBoardTable = "DROP TABLE RegularRegisterNN";
            PreparedStatement statement = _con.prepareStatement(generalBoardTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void dropUsersTable() {
        try {
            String usersTable = "DROP TABLE USERS";
            PreparedStatement statement = _con.prepareStatement(usersTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void resetDatabase() {
        try {
            String dropDatabase = "DROP DATABASE IF EXISTS " + _databaseName;
            String createDatabase = "CREATE DATABASE IF NOT EXISTS " + _databaseName;
            String useDatabase = "USE " + _databaseName;

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

    /*public void resetDatabaseTest() {
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
        createGeneralBoardTable();
        createUsersTable();
    }*/

    public int insertUser(byte[] publicKey, String clientUUID, byte[] atomicRegister1N, byte[] cmh) {
        try {
            String users = "INSERT INTO Users(PublicKey, ClientUUID, AtomicRegister1N, ClientMessageHandler) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = _con.prepareStatement(users);
            statement.setBytes(1, publicKey);
            statement.setString(2, clientUUID);
            statement.setBytes(3, atomicRegister1N);
            statement.setBytes(4, cmh);
            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    public int updateUserToken(String clientUUID, String token) {
        try {
            String users = "UPDATE Users SET Token = ? WHERE ClientUUID = ?";
            PreparedStatement statement = _con.prepareStatement(users);
            statement.setString(1, token);
            statement.setString(2, clientUUID);
            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    public int updateUserAtomicRegister1N(String clientUUID, byte[] atomicRegister1N) {
        try {
            String users = "UPDATE Users SET AtomicRegister1N = ? WHERE ClientUUID = ?";
            PreparedStatement statement = _con.prepareStatement(users);
            statement.setBytes(1, atomicRegister1N);
            statement.setString(2, clientUUID);
            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    public int updateRegularRegisterNNTable(byte[] announcements) {
        try {
            String table = "UPDATE RegularRegisterNN SET Announcements = ? WHERE ID = 1";
            PreparedStatement statement = _con.prepareStatement(table);
            statement.setBytes(1, announcements);
            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    public List<byte[]> getUsersPublicKeys() {
        List<byte[]> l = new ArrayList<byte[]>();
        try {
            String query = "SELECT PublicKey FROM Users";
            PreparedStatement preparedStatement = _con.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                l.add(rs.getBytes(1));
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

    public DBStructure retrieveStructure() {
        RegularRegisterNNStructure rrs = null;
        List<UserStructure> users = new ArrayList<UserStructure>();

        try {
            String query = "SELECT * FROM Users";
            PreparedStatement preparedStatement = _con.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                UserStructure us = new UserStructure(rs.getBytes(1), rs.getString(2), rs.getBytes(3), rs.getBytes(4), rs.getString(5));
                users.add(us);
            }

            query = "SELECT * FROM RegularRegisterNN";
            preparedStatement = _con.prepareStatement(query);
            rs = preparedStatement.executeQuery();
            while (rs.next()){
                rrs = new RegularRegisterNNStructure(rs.getBytes(1));
            }
        }
        catch (SQLException e) {
            System.out.println(e);
        }

        return new DBStructure(users, rrs);
    }

}