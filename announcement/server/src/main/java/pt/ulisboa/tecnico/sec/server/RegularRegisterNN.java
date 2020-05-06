package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.Communication;
import pt.ulisboa.tecnico.sec.communication_lib.ServerMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RegularRegisterNN {

    private Server _server;
    private int _nServers;

    public RegularRegisterNN(Server server, int nServers) {
        _server = server;
        _nServers = nServers;
    }
    
}
