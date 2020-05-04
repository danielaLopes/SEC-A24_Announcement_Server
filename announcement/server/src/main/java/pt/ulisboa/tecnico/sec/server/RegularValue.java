package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;
import java.util.List;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;

public class RegularValue {
        private int _ts;
        private PublicKey _wr;
        private List<Announcement> _val;
        
        public RegularValue(PublicKey wr, List<Announcement> val) {
            _ts = 0;
            _wr = wr;
            _val = val;
        }

        public int getTimestamp() { return _ts; }
        public PublicKey getWriterId() { return _wr; }
        public List<Announcement> getAnnouncements() { return _val; }
}