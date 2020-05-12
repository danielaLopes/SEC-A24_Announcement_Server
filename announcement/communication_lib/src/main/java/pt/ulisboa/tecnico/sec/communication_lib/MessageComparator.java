package pt.ulisboa.tecnico.sec.communication_lib;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

public class MessageComparator {

    /**
     *
     * @param clientMessages
     * @param required is the minimum number of equivalent requests necessary for next phase
     * @return message with quorum
     */
    public static VerifiableProtocolMessage compareClientMessages(List<VerifiableProtocolMessage> clientMessages, int required) {

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
                int nOccur = occurrences.get(key);
                occurrences.put(key, ++nOccur);
            }
            else {
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

    public static VerifiableAnnouncement containsKey(Map<VerifiableAnnouncement, Integer> occurrences, VerifiableAnnouncement other) {
        for (Map.Entry<VerifiableAnnouncement, Integer> entry : occurrences.entrySet()) {
            if (entry.getKey().equals(other)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Map.Entry<StatusCode, List<VerifiableAnnouncement>> compareServerResponses(
                List<VerifiableProtocolMessage> responses, int required) {
        Map.Entry<StatusCode, List<VerifiableAnnouncement>> quorumSc = compareServerStatusCodes(responses, required);
        // returns null to tell there is still no quorum of messages with same status code
        if (quorumSc == null) return null;

        if (quorumSc.getKey().equals(StatusCode.OK)) {
            return new AbstractMap.SimpleEntry<>(StatusCode.OK, compareOkResponses(responses, required));
        }
        // quorum is a status code that is not OK
        // TODO: be careful, announcements here are going to be empty
        else {
            return quorumSc;
        }

    }

    // Gathers the list of announcements that have a quorum
    public static List<VerifiableAnnouncement> compareOkResponses(List<VerifiableProtocolMessage> responses, int required) {
        // maintains number of occurrences for each announcement
        Map<VerifiableAnnouncement, Integer> occurrences = new HashMap<>();
        for (VerifiableProtocolMessage response : responses) {
            RegisterMessage rm = new RegisterMessage(response.getProtocolMessage().getAtomicRegisterMessages());
            List<VerifiableAnnouncement> announcements = rm.getValues();
            for (VerifiableAnnouncement a : announcements) {
                VerifiableAnnouncement key = containsKey(occurrences, a);
                if (key != null) {
                    int nOccur = occurrences.get(key);
                    occurrences.put(key, ++nOccur);
                }
                else {
                    occurrences.put(a, 1);
                }
            }
        }

        List<VerifiableAnnouncement> quorumAnnouncements = new ArrayList<>();
        for (Map.Entry<VerifiableAnnouncement, Integer> entry : occurrences.entrySet()) {
            if (entry.getValue() > required) quorumAnnouncements.add(entry.getKey());
        }

        return quorumAnnouncements;
    }

    public static Map.Entry<StatusCode, List<VerifiableAnnouncement>> compareServerStatusCodes(
                List<VerifiableProtocolMessage> responses, int required) {
        // Counts number of responses by status code
        Map<StatusCode, List<VerifiableProtocolMessage>> responsesBySc = new HashMap<>();

        for (VerifiableProtocolMessage response : responses) {
            StatusCode sc = response.getProtocolMessage().getStatusCode();
            if (responsesBySc.containsKey(sc)) {
                responsesBySc.get(sc).add(response);
            }
            else {
                List<VerifiableProtocolMessage> lst = new ArrayList<>();
                lst.add(response);
                responsesBySc.put(response.getProtocolMessage().getStatusCode(), lst);
            }
        }

        // check if there is a status code with a quorum
        for (Map.Entry<StatusCode, List<VerifiableProtocolMessage>> entry : responsesBySc.entrySet()) {
            if (entry.getValue().size() > required) {
                return new AbstractMap.SimpleEntry<>(entry.getKey(), new ArrayList<>());
            }
        }
        return null;
    }
}
