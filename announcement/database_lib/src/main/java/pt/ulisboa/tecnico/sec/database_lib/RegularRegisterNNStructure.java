package pt.ulisboa.tecnico.sec.database_lib;

public class RegularRegisterNNStructure {
    private byte[] _generalBoard;

    public RegularRegisterNNStructure(byte[] generalBoard) {
        _generalBoard = generalBoard;
    }

    public byte[] getGeneralBoard() { return _generalBoard; }
    
}