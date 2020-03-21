package pt.ulisboa.tecnico.sec.database_lib;
import java.sql.*;

public class Application {
    public static void main(String args[]) {
        Database db = new Database();

        db.createGeneralBoardTable();
        db.insertMessageGB(1,"ola", "ref", 3);
    }
}