package edu.uci.ics.asterix.result;

import edu.uci.ics.asterix.result.metadata.Datatype;
import edu.uci.ics.asterix.result.metadata.Function;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

// This is a singleton... Or is it?
public class HashObject {
    private static HashMap<Tuple, Datatype> result = null;
    private static HashMap<Tuple, Boolean> found = null; // Why don't I use set instead lol
    private static HashMap<Tuple, String> settotype = null;
    private static HashMap<Tuple, Function> functionHash = null;
    private static HashMap<Tuple, Boolean> found2 = null; // This is for the Functions...
    private static Set<Tuple> printed = null;

    private HashObject() {
    }

    public static HashMap<Tuple, Boolean> getFound() {
        if (found == null) {
            found = new HashMap<>();
        }
        return found;
    }

    public static Boolean getFound(Tuple s) {
        if (found == null) {
            found = new HashMap<>();
        }
        if (!found.containsKey(s))
            return false;
        return found.get(s);
    }

    public static void setFound(Tuple s, Boolean bo) {
        if (found == null) {
            found = new HashMap<>();
        } else found.put(s, bo);
    }

    public static HashMap<Tuple, Datatype> getResult() {
        if (result == null)
            result = new HashMap<>();
        return result;
    }

    public static Datatype getResult(Tuple s) {
        if (result == null)
            result = new HashMap<>();
        return result.get(s);
    }

    public static void setResult(Tuple s, Datatype t) {
        if (result == null)
            result = new HashMap<>();
        result.put(s, t);
    }

    public static String getSettotype(Tuple s) {
        if (settotype == null)
            settotype = new HashMap<>();
        return settotype.get(s);
    }

    public static void setSettotype(Tuple t, String s) {
        if (settotype == null)
            settotype = new HashMap<>();
        settotype.put(t, s);
    }

    public static HashMap<Tuple, Function> getFunctionHash() {
        if (functionHash == null)
            functionHash = new HashMap<>();
        return functionHash;
    }

    public static Function getFunctionHash(Tuple s) {
        if (functionHash == null)
            functionHash = new HashMap<>();
        return functionHash.get(s);
    }

    public static void setFunctionHash(Tuple s, Function t) {
        if (functionHash == null)
            functionHash = new HashMap<>();
        functionHash.put(s, t);
    }

    public static HashMap<Tuple, Boolean> getFound2() {
        if (found2 == null) {
            found2 = new HashMap<>();
        }
        return found2;
    }

    public static Boolean getFound2(Tuple s) {
        if (found2 == null) {
            found2 = new HashMap<>();
        }
        if (!found2.containsKey(s))
            return false;
        return found2.get(s);
    }

    public static void setFound2(Tuple s, Boolean bo) {
        if (found2 == null) {
            found2 = new HashMap<>();
        } else found2.put(s, bo);
    }

    public static Boolean getPrinted(Tuple s) {
        if (printed == null) {
            printed = new HashSet<>();
        }
        return printed.contains(s);
    }

    public static void setPrinted(Tuple s) {
        if (printed == null) {
            printed = new HashSet<>();
        }
        printed.add(s);
    }
}
