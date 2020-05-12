package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class ClientTest extends Client {

    public boolean postDelivered = false;
    public boolean postGeneralDelivered = false;
    public boolean readDelivered = false;
    public boolean readGeneralDelivered = false;

    public StatusCode postDeliveredSC;
    public StatusCode postGeneralDeliveredSC;
    public StatusCode readDeliveredSC;
    public StatusCode readGeneralDeliveredSC;

    public ClientTest(String pubKeyPath, String keyStorePath,
                  String keyStorePasswd, String entryPasswd, String alias, int nServers, int nFaults) {
        super(pubKeyPath, keyStorePath, keyStorePasswd, entryPasswd,
                    alias, nServers, nFaults);
    }

    // @Override
    // public void deliverPost(StatusCode sc) {
    //     //System.out.println("deliverPost");
    //     resetResponses();
    //     if (_clientUI != null)
    //         _clientUI.deliverPost(sc);
        
    // }

    @Override
    public void deliverPostGeneral(StatusCode sc) {
        System.out.println("deliverPostGeneral");
        postDelivered = true;
        postDeliveredSC = sc;  
    }

    // @Override
    // public void deliverRead(StatusCode sc, List<VerifiableAnnouncement> vas) {
    //     //System.out.println("dleiver reaea");
    //     resetResponses();
    //     List<Announcement> announcements = new ArrayList<Announcement>();
    //     for (VerifiableAnnouncement va : vas) {
    //         if(verifySignature(va, va.getAnnouncement().getClientPublicKey()))
    //             announcements.add(va.getAnnouncement());
    //     }
    //     if (_clientUI != null)
    //         _clientUI.deliverRead(sc, announcements);
    // }

    // @Override
    // public void deliverReadGeneral(StatusCode sc, List<VerifiableAnnouncement> vas) {
    //     //System.out.println("deliver read general");
    //     List<Announcement> announcements = new ArrayList<Announcement>();
    //     for (VerifiableAnnouncement va : vas) {
    //         if(verifySignature(va, va.getAnnouncement().getClientPublicKey()))
    //             announcements.add(va.getAnnouncement());
    //     }
    //     //System.out.println("status read general: " + sc);
    //     resetResponses();
    //     if (_clientUI != null) {
    //         _clientUI.deliverReadGeneral(sc, announcements);
    //     }
    // }


}