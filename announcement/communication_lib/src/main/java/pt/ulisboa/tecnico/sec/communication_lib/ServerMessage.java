package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerMessage implements Serializable {
    private PublicKey _publicKey;
    //private VerifiableProtocolMessage _clientMessage;
    private PublicKey _clientPubKey;
    private String _command;
    private byte[] _readAnnouncements;
    private AtomicInteger _rid;
    private byte[] _regularValue;

    //public ServerMessage(PublicKey publicKey, VerifiableProtocolMessage vpm, String command, byte[] readAnnouncements) {
    public ServerMessage(PublicKey publicKey, PublicKey clientPubKey, String command, byte[] readAnnouncements) {
        _publicKey = publicKey;
        _command = command;
        _readAnnouncements = readAnnouncements;
        //_clientMessage = vpm;
        _clientPubKey = clientPubKey;
    }

    //public ServerMessage(PublicKey publicKey, VerifiableProtocolMessage vpm, String command, AtomicInteger rid) {
    public ServerMessage(PublicKey publicKey, PublicKey clientPubKey, String command, AtomicInteger rid) {
        _publicKey = publicKey;
        _command = command;
        _rid = rid;
        //_clientMessage = vpm;
        _clientPubKey = clientPubKey;
    }

    //public ServerMessage(PublicKey publicKey, VerifiableProtocolMessage vpm, String command, AtomicInteger rid, byte[] readAnnouncements) {
    public ServerMessage(PublicKey publicKey, PublicKey clientPubKey, String command, AtomicInteger rid, byte[] readAnnouncements) {
        _publicKey = publicKey;
        _command = command;
        _rid = rid;
        _readAnnouncements = readAnnouncements;
        //_clientMessage = vpm;
        _clientPubKey = clientPubKey;
    }

    public void setRegularValue(byte[] regularValue) { _regularValue = regularValue; }
    public byte[] getRegularValue() {return _regularValue;}

    public PublicKey getPublicKey() {return _publicKey; }
    //public VerifiableProtocolMessage getClientMessage() { return _clientMessage; }
    public PublicKey getClientPubKey() { return _clientPubKey; }
    public String getCommand() {return _command; }
    public AtomicInteger getRid() {return _rid; }
    public byte[] getReadAnnouncements() {return _readAnnouncements;}
}