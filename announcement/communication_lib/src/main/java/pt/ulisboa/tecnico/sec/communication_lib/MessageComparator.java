package pt.ulisboa.tecnico.sec.communication_lib;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageComparator {

    /**
     *
     * @param clientMessages
     * @param required is the minimum number of equivalent requests necessary for next phase
     * @return message with quorum
     */
    public static VerifiableProtocolMessage compareClientMessages(List<VerifiableProtocolMessage> clientMessages, int required) {

        System.out.println("client messages to compare: " + clientMessages);
        List<VerifiableProtocolMessage> posts = clientMessages.stream()
                .filter(message -> message.getProtocolMessage().getCommand().equals("POST"))
                .collect(Collectors.toList());
        if (posts.size() > required) {
            return compareMessages(posts, required);
        }

        List<VerifiableProtocolMessage> reads = clientMessages.stream()
                .filter(message -> message.getProtocolMessage().getCommand().equals("READ"))
                .collect(Collectors.toList());
        if (reads.size() > required) {
            return compareMessages(reads, required);
        }

        List<VerifiableProtocolMessage> postGenerals = clientMessages.stream()
                .filter(message -> message.getProtocolMessage().getCommand().equals("POSTGENERAL"))
                .collect(Collectors.toList());
        if (postGenerals.size() > required) {
            return compareMessages(postGenerals, required);
        }

        List<VerifiableProtocolMessage> readGenerals = clientMessages.stream()
                .filter(message -> message.getProtocolMessage().getCommand().equals("READGENERAL"))
                .collect(Collectors.toList());
        if (readGenerals.size() > required) {
            return compareMessages(readGenerals, required);
        }
        return null; // indicates no quorum is achieved
    }


    public static VerifiableProtocolMessage compareMessages(List<VerifiableProtocolMessage> clientMessages, int required) {
        // we need to compare announcement
        Map<VerifiableProtocolMessage, Integer> occurrences = new HashMap<>();
        for (VerifiableProtocolMessage vpm : clientMessages) {
            VerifiableProtocolMessage key = containsKey(occurrences, vpm);
            if (key != null) {
                System.out.println("same occur");
                int nOccur = occurrences.get(key);
                occurrences.put(key, ++nOccur);
            }
            else {
                System.out.println("new occur");
                occurrences.put(vpm, 1);
            }
        }
        int highest = -1;
        VerifiableProtocolMessage vpm = null;
        for (Map.Entry<VerifiableProtocolMessage, Integer> entry : occurrences.entrySet()) {
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                vpm = entry.getKey();
            }
        }
        if (highest > required) {
            return vpm;
        }
        return null;
    }

    public static VerifiableProtocolMessage containsKey(Map<VerifiableProtocolMessage, Integer> occurrences, VerifiableProtocolMessage other) {
        for (Map.Entry<VerifiableProtocolMessage, Integer> entry : occurrences.entrySet()) {
            if (entry.getKey().equals(other)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
