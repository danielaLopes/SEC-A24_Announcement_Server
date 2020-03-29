package pt.ulisboa.tecnico.sec.database_lib;

import java.util.List;
import java.util.ArrayList;

public class DBStructure {
    private List<GeneralBoardStructure> _generalBoard = new ArrayList<GeneralBoardStructure>();
    private List<UserBoardStructure> _userBoard = new ArrayList<UserBoardStructure>();
    private List<UserStructure> _users = new ArrayList<UserStructure>();

    public DBStructure(List<GeneralBoardStructure> generalBoard, List<UserBoardStructure> userBoard, List<UserStructure> users) {
        _generalBoard = generalBoard;
        _userBoard = userBoard;
        _users = users;
    }

    public List<GeneralBoardStructure> getGeneralBoard() { return _generalBoard; }

    public List<UserBoardStructure> getUserBoard() { return _userBoard; }

    public List<UserStructure> getUsers() { return _users; }
}