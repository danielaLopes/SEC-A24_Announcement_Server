package pt.ulisboa.tecnico.sec.server;

import java.io.Serializable;

public class Lock implements Serializable{
    protected Object _l;
    
    public Lock(){
        _l = new Object();
    }
}