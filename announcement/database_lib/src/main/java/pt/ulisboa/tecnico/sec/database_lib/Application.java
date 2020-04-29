package pt.ulisboa.tecnico.sec.database_lib;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

import java.util.ArrayList;
import java.util.List;
public class Application {
    public static void main(String args[]) {
        Database db = new Database("announcement");

        //db.createGeneralBoardTable();
       /* List<Integer> ref = new ArrayList<Integer>();
        ref.add(1);
        ref.add(3);
        byte[] b = ProtocolMessageConverter.objToByteArray(ref);

        db.insertAnnouncementGB("a", b, 69, "funkyuuid");
        db.insertAnnouncementGB("b", b, 70, "funkyuuid");
        db.insertAnnouncementGB("c", b, 71, "funkyuuid");
        db.insertAnnouncementGB("d", b, 72, "funkyuuid");

        List<Announcement> a = db.getGBAnnouncements(3);
        System.out.println(a.get(0).getAnnouncement());*/

        

    }
}