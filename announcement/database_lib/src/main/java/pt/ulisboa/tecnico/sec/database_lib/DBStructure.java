package pt.ulisboa.tecnico.sec.database_lib;

import java.util.List;

public class DBStructure {
    private List<UserStructure> _users;
    private RegularRegisterNNStructure _regularRegisterNN;

    public DBStructure(List<UserStructure> users, RegularRegisterNNStructure regularRegisterNN) {
        _users = users;
        _regularRegisterNN = regularRegisterNN;
    }

    public List<UserStructure> getUsers() { return _users; }

    public RegularRegisterNNStructure getRegularRegisterNN() { return _regularRegisterNN; }
}