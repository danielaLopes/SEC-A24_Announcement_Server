package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.AtomicRegisterMessages;
import pt.ulisboa.tecnico.sec.communication_lib.Communication;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.ServerMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

import java.security.PublicKey;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class RegularRegisterNN {

    private Server _server;
    private int _nServers;
    private List<RegisterValue> _registerValues;

    public RegularRegisterNN(Server server, int nServers) {
        _server = server;
        _nServers = nServers;
        _registerValues = new ArrayList<RegisterValue>();
    }

    public AtomicRegisterMessages acknowledge(ProtocolMessage pm) {
        //System.out.println("acknowledge");
        AtomicRegisterMessages arm = pm.getAtomicRegisterMessages();
        PublicKey clientPubKey = pm.getPublicKey();
        if (arm.getWts() > getLastUserTimeStamp(clientPubKey)) {
            RegisterValue rv = new RegisterValue(arm.getWts(), arm.getValues().get(0), clientPubKey);
            _registerValues.add(rv);
        }
        return new AtomicRegisterMessages(arm.getWts());
    }

    public AtomicRegisterMessages value(ProtocolMessage pm) {
        //System.out.println("value");
        AtomicRegisterMessages arm = pm.getAtomicRegisterMessages();
        int n = pm.getReadNumberAnnouncements();
        return new AtomicRegisterMessages(arm.getRid(), getLastTimeStamp(), getAnnouncements(n));
    }

    public int getLastTimeStamp() {
        if (_registerValues.size() > 0)
            return _registerValues.get(_registerValues.size() - 1).getTimeStamp();
        return 0;
    }

    public int getLastUserTimeStamp(PublicKey clientPublicKey) {
        int ts = -1;
        for (RegisterValue rv: _registerValues) {
            if(rv.getPublicKey().equals(clientPublicKey))
                ts = rv.getTimeStamp();
        }
        return ts;
    }

    public List<Announcement> getAnnouncements(int number) {
        int nAnnouncements = _registerValues.size();
        List<Announcement> values = new ArrayList<Announcement>();
        if ((0 < number) && (number <= nAnnouncements)) {
            List<RegisterValue> rv = new ArrayList<RegisterValue>(_registerValues.subList(nAnnouncements - number, nAnnouncements));
            for (RegisterValue r: rv)
                values.add(r.getValues());
        }
        else {
            for (RegisterValue r: _registerValues)
                values.add(r.getValues());
        }
        return values;
    }
    
}
