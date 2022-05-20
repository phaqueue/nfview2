package edu.uci.ics.asterix.result;

public class Tuple {
    public Tuple(String x, String y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < this.x.length(); i++)
            hash = (hash * 97 + this.x.charAt(i) - 'A') % 19260817;
        for (int i = 0; i < this.y.length(); i++)
            hash = (hash * 97 + this.y.charAt(i) - 'A') % 19260817;
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (this.getClass() != other.getClass()) return false;
        Tuple that = (Tuple) other;
        return (this.x.equals(that.x)) && (this.y.equals(that.y));
    }

    public String getx() {
        return x;
    }

    public String gety() {
        return y;
    }

    private final String x;
    private final String y;
}
