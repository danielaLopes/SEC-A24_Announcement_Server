package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.*;

import java.security.PublicKey;

/**
 * Does not perform broadcast with other servers, only answers to client and updates locally
 */
public class ByzantineServer extends Server {

    public ByzantineServer(boolean activateCC, int nServers, int nFaults, int port, char[] keyStorePasswd, char[] entryPasswd, String alias, String pubKeyPath,
                  String keyStorePath) {

        super(activateCC, nServers, nFaults, port, keyStorePasswd, entryPasswd, alias, pubKeyPath, keyStorePath);
    }

    @Override
    public VerifiableProtocolMessage registerUser(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        super.registerUser(vpm, cmh);
        return null;
    }

    @Override
    public void post(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        System.out.println("------------------BYZANTINE POST--------------------");
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String token = vpm.getProtocolMessage().getToken();
        User user = _users.get(clientPubKey);

        String newToken = user.getToken();
        RegisterMessage registerMessage = new RegisterMessage(vpm.getProtocolMessage().getAtomicRegisterMessages());
        RegisterMessage arm = _users.get(clientPubKey).getAtomicRegister1N().acknowledge(registerMessage);

        ProtocolMessage p = new ProtocolMessage("POST",  StatusCode.OK, getServerPubKey(), newToken, token);
        p.setAtomicRegisterMessages(arm.getBytes());
        cmh.sendMessage(createVerifiableMessage(p));
    }

    @Override
    public void postGeneral(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        System.out.println("---------------BYZANTINE POSTGENERAL-----------------");
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String token = vpm.getProtocolMessage().getToken();
        User user = _users.get(clientPubKey);
        String newToken = user.getToken();

        RegisterMessage arm = getRegularRegisterNN().acknowledge(vpm.getProtocolMessage());

        ProtocolMessage p = new ProtocolMessage("POSTGENERAL", StatusCode.OK, getServerPubKey(), newToken, token);
        p.setAtomicRegisterMessages(arm.getBytes());
        cmh.sendMessage(createVerifiableMessage(p));
    }

    @Override
    public void read(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        System.out.println("------------------BYZANTINE READ--------------------");
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String token = vpm.getProtocolMessage().getToken();
        User user = _users.get(clientPubKey);

        String newToken = user.getToken();
        int number = vpm.getProtocolMessage().getReadNumberAnnouncements();

        RegisterMessage registerMessage = new RegisterMessage(vpm.getProtocolMessage().getAtomicRegisterMessages());
        RegisterMessage arm = _users.get(clientPubKey).getAtomicRegister1N().value(registerMessage, number);

        ProtocolMessage p = new ProtocolMessage("READ", StatusCode.OK, getServerPubKey(), newToken, token);
        p.setAtomicRegisterMessages(arm.getBytes());
        VerifiableProtocolMessage response = createVerifiableMessage(p);

        cmh.sendMessage(response);

    }

    @Override
    public void readGeneral(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        System.out.println("---------------BYZANTINE READGENERAL-----------------");
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String token = vpm.getProtocolMessage().getToken();
        User user = _users.get(clientPubKey);

        String newToken = user.getToken();

        RegisterMessage arm = getRegularRegisterNN().value(vpm.getProtocolMessage());

        ProtocolMessage p = new ProtocolMessage("READGENERAL", StatusCode.OK, getServerPubKey(), newToken, token);
        p.setAtomicRegisterMessages(arm.getBytes());
        cmh.sendMessage(createVerifiableMessage(p));
    }
}
