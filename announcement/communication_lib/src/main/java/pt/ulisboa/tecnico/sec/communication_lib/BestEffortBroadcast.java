package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class BestEffortBroadcast {
    private Communication _communication = new Communication();
    
    public void pp2pSend(ObjectOutputStream oos, VerifiableServerMessage vsm) throws IOException{
        _communication.sendMessage(vsm, oos);
    }
}