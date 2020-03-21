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
            String generalBoardTable = "CREATE TABLE GeneralBoard (PublicKey INT(20) NOT NULL, Message VARCHAR(256) NOT NULL, Reference VARCHAR(256), Id INT(8), PRIMARY KEY(PublicKey)) CHARACTER SET utf8";
            PreparedStatement statement = _con.prepareStatement(generalBoardTable);
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