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
            Class.forName("com.mysql.cj.jdbc.Driver");
            _con=DriverManager.getConnection("jdbc:mysql://localhost:3306/announcement?verifyServerCertificate=false&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true","sec","1234");

            resetDatabase();
            createGeneralBoardTable();
            createUsersTable();
            createOperationsTable();
        }
        catch(Exception e) {
            System.out.println(e);
        }  
    }

    public void createGeneralBoardTable() {
        try {
            String generalBoardTable = "CREATE TABLE IF NOT EXISTS GeneralBoard (Announcement VARCHAR(256) NOT NULL, Reference VARBINARY(256), AnnouncementID VARCHAR(255) NOT NULL, ClientUUID VARCHAR(255) NOT NULL, Seq INT AUTO_INCREMENT, PRIMARY KEY(Seq)) CHARACTER SET utf8";
            PreparedStatement statement = _con.prepareStatement(generalBoardTable);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void createUsersTable() {
        try {
            String usersTable = "CREATE TABLE IF NOT EXISTS Users (PublicKey VARBINARY(2000) NOT NULL, ClientUUID VARCHAR(256) NOT NULL, PRIMARY KEY(PublicKey, ClientUUID)) CHARACTER SET utf8";
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

    public void createOperationsTable() {
        try {
            String operationsTable = "CREATE TABLE IF NOT EXISTS Operations (ClientUUID VARCHAR(256) NOT NULL, OpUUID VARCHAR(255) NOT NULL, PRIMARY KEY(ClientUUID)) CHARACTER SET utf8";
            PreparedStatement statement = _con.prepareStatement(operationsTable);
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
            // String dropDatabase = "DROP DATABASE IF EXISTS announcement";
            String createDatabase = "CREATE DATABASE IF NOT EXISTS announcement";
            String useDatabase = "USE announcement";
            // PreparedStatement statement = _con.prepareStatement(dropDatabase);
            // statement.executeUpdate();
            PreparedStatement statement = _con.prepareStatement(createDatabase);
            statement.executeUpdate();
            statement = _con.prepareStatement(useDatabase);
            statement.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void resetDatabaseTest() {
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
        createOperationsTable();
    }

    public int insertAnnouncementGB(String announcememnt, byte[] reference, String announcementID, String clientUUID) {
        try {
            String messageGB = "INSERT INTO GeneralBoard(Announcement, Reference, AnnouncementID, ClientUUID) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = _con.prepareStatement(messageGB);
            statement.setString(1, announcememnt);
            statement.setBytes(2, reference);
            statement.setString(3, announcementID);
            statement.setString(4, clientUUID);
            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    public int insertAnnouncement(String announcememnt, byte[] reference, String announcementID, String clientTableName, byte[] accountability) {
        try {
            String messageGB = "INSERT INTO " + clientTableName + "(Announcement, Reference, AnnouncementID, Accountability) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = _con.prepareStatement(messageGB);
            statement.setString(1, announcememnt);
            statement.setBytes(2, reference);
            statement.setString(3, announcementID);
            statement.setBytes(4, accountability);
            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    public int insertUser(byte[] publicKey, String clientUUID) {
        try {
            String users = "INSERT INTO Users(PublicKey, ClientUUID) VALUES (?, ?)";
            PreparedStatement statement = _con.prepareStatement(users);
            statement.setBytes(1, publicKey);
            statement.setString(2, clientUUID);
            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    public int updateOperationUserRow(String clientUUID, String opUUID) {
        try {
            String users = "UPDATE Operations SET OpUUID = ? WHERE ClientUUID = ?";
            PreparedStatement statement = _con.prepareStatement(users);
            statement.setString(1, opUUID);
            statement.setString(2, clientUUID);
            statement.executeUpdate();  
            return 1;
        }
        catch(Exception e) {
            System.out.println(e);
            return 0;
        }
    }

    public int createOperationUserRow(String clientUUID, String opUUID) {
        try {
            String users = "INSERT INTO Operations(ClientUUID, OpUUID) VALUES (?, ?)";
            PreparedStatement statement = _con.prepareStatement(users);
            statement.setString(1, clientUUID);
            statement.setString(2, opUUID);
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

    public List<Announcement> getUserAnnouncements(int n, byte[] b) {
        List<Announcement> l = new ArrayList<Announcement>();
        try {
            String query = "SELECT ClientUUID FROM Users WHERE PublicKey=?";
            PreparedStatement preparedStatement = _con.prepareStatement(query);
            preparedStatement.setBytes(1, b);
            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()) {
                    String tableName = rs.getString(1);
                    System.out.println("TABLE NAME: " + tableName);
                if (n > 0)
                    query = "SELECT * FROM " + tableName + " ORDER BY Seq DESC LIMIT " + Integer.toString(n);
                else
                    query = "SELECT * FROM " + tableName + " ORDER BY Seq DESC";
                preparedStatement = _con.prepareStatement(query);
                rs = preparedStatement.executeQuery();
                while (rs.next()){
                    ArrayList<String> references = (ArrayList<String>) ProtocolMessageConverter.byteArrayToObj(rs.getBytes(2));
                    Announcement a = new Announcement(rs.getString(1), references, rs.getString(3), rs.getString(4));
                    l.add(0, a);
                }
            }
        }
        catch (SQLException e) {
            System.out.println(e);
        }
        return l;
    } 
    

    public List<Announcement> getGBAnnouncements(int n) {
        List<Announcement> l = new ArrayList<Announcement>();
        try {
            String query = null;
            if (n > 0)
                query = "SELECT * FROM GeneralBoard ORDER BY Seq DESC LIMIT " + Integer.toString(n);
            else
                query = "SELECT * FROM GeneralBoard ORDER BY Seq DESC";
            PreparedStatement preparedStatement = _con.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                ArrayList<String> references = (ArrayList<String>) ProtocolMessageConverter.byteArrayToObj(rs.getBytes(2));
                Announcement a = new Announcement(rs.getString(1), references, rs.getString(3), rs.getString(4));
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

    public DBStructure retrieveStructure() {
        List<GeneralBoardStructure> generalBoard = new ArrayList<GeneralBoardStructure>();
        List<UserBoardStructure> userBoard = new ArrayList<UserBoardStructure>();
        List<UserStructure> users = new ArrayList<UserStructure>();
        List<OperationsBoardStructure> operations = new ArrayList<OperationsBoardStructure>();

        try {
            String query = "SELECT * FROM GeneralBoard ORDER BY Seq ASC";
            PreparedStatement preparedStatement = _con.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                GeneralBoardStructure gbs = new GeneralBoardStructure(rs.getString(1), rs.getBytes(2), rs.getString(3), rs.getString(4));
                generalBoard.add(gbs);
            }

            query = "SELECT * FROM Operations";
            preparedStatement = _con.prepareStatement(query);
            rs = preparedStatement.executeQuery();
            while (rs.next()){
                OperationsBoardStructure obs = new OperationsBoardStructure(rs.getString(1), rs.getString(2));
                operations.add(obs);
            }

            query = "SELECT * FROM Users";
            preparedStatement = _con.prepareStatement(query);
            rs = preparedStatement.executeQuery();
            while (rs.next()){
                UserStructure us = new UserStructure(rs.getBytes(1), rs.getString(2));
                users.add(us);

                //For every user, a user table exists. Iterate over each user table
                String tableName = rs.getString(2);
                String userQuery = "SELECT * FROM " + tableName +" ORDER BY Seq ASC";
                PreparedStatement userPreparedStatement = _con.prepareStatement(userQuery);
                ResultSet userRS = userPreparedStatement.executeQuery();
                while (userRS.next()){
                    UserBoardStructure ubs = new UserBoardStructure(userRS.getString(1), userRS.getBytes(2), userRS.getString(3), tableName);
                    userBoard.add(ubs);
                }
            }
        }
        catch (SQLException e) {
            System.out.println(e);
        }

        return new DBStructure(generalBoard, userBoard, users, operations);
    }

}