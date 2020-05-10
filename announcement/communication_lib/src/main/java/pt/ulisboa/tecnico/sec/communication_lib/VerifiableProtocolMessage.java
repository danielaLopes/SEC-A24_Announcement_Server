package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;

public class VerifiableProtocolMessage implements Serializable{
    private ProtocolMessage _pm;
    private byte[] _signedpm;

    public VerifiableProtocolMessage(ProtocolMessage pm, byte[] signedpm) {
        _pm = pm;
        _signedpm = signedpm;
    }

    public ProtocolMessage getProtocolMessage() { return _pm; }
    public byte[] getSignedProtocolMessage() { return _signedpm; }

    public void setProtocolMessage(ProtocolMessage pm) { _pm = pm; }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (!(o instanceof VerifiableProtocolMessage)) {
            return false;
        }

        VerifiableProtocolMessage vpm = (VerifiableProtocolMessage) o;

        if (this.getProtocolMessage().getCommand().equals("POST") ||
                this.getProtocolMessage().getCommand().equals("POSTGENERAL")) {
            return this.getProtocolMessage().getPostAnnouncement().equals(
                    vpm.getProtocolMessage().getPostAnnouncement());
        }
        else if (this.getProtocolMessage().getCommand().equals("READ")) {
            return this.getProtocolMessage().getReadNumberAnnouncements() ==
                    vpm.getProtocolMessage().getReadNumberAnnouncements() &&
                    this.getProtocolMessage().getToReadPublicKey().equals(
                            vpm.getProtocolMessage().getToReadPublicKey()
                    );
        }
        else if (this.getProtocolMessage().getCommand().equals("READGENERAL")) {
            return this.getProtocolMessage().getReadNumberAnnouncements() ==
                    vpm.getProtocolMessage().getReadNumberAnnouncements();
        }

        return false;
    }
}