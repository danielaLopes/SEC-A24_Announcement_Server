package pt.ulisboa.tecnico.sec.database_lib;

import java.util.List;
import java.util.ArrayList;

public class DBStructure {
    private List<GeneralBoardStructure> _generalBoard = new ArrayList<GeneralBoardStructure>();
    private List<UserBoardStructure> _userBoard = new ArrayList<UserBoardStructure>();
    private List<UserStructure> _users = new ArrayList<UserStructure>();
    private List<OperationsBoardStructure> _operations = new ArrayList<OperationsBoardStructure>();

    public DBStructure(List<GeneralBoardStructure> generalBoard, List<UserBoardStructure> userBoard, List<UserStructure> users, List<OperationsBoardStructure> operations) {
        _generalBoard = generalBoard;
        _userBoard = userBoard;
        _users = users;
        _operations = operations;
    }

    public List<GeneralBoardStructure> getGeneralBoard() { return _generalBoard; }

    public List<UserBoardStructure> getUserBoard() { return _userBoard; }

    public List<UserStructure> getUsers() { return _users; }

    public List<OperationsBoardStructure> getOperations() { return _operations; }
}