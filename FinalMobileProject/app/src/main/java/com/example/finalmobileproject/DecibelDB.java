package com.example.finalmobileproject;

import java.sql.Timestamp;

public class DecibelDB {
    private long ID;
    private double currentDecibel;
    private double currentLAeq;
    private double currentLmax;
    private String loudlevel;

    public DecibelDB(long id, double currentDecibel, double currentLAeq, double currentLmax, String loudlevel) {
        this.ID = ID;
        this.currentDecibel = currentDecibel;
        this.currentLmax = currentLmax;
        this.currentLAeq = currentLAeq;
        this.loudlevel = loudlevel;
    }


//    public long getID() {
//        return ID;
//    }
//    public void setID(long ID) {
//        this.ID = ID;
//    }
//    private double getCurrentDecibel() {return currentDecibel;}
//    private void setCurrentDecibel(Double currentDecibel) {this.currentDecibel = currentDecibel;}
//    private double getMaxDecibel() {return maxDecibel;}
//    private void setMaxDecibel() {this.maxDecibel = maxDecibel;}
//    private Double getMinDecibel() {return minDecibel;}
//    private void setMinDecibel() {this.minDecibel = minDecibel;}
//    private String getLoudlevel() { return loudlevel;}
//    private void setLoudlevel() {this.loudlevel = loudlevel;}

    public long getID() {
        return ID;
    }

    public double getCurrentDecibel() {
        return currentDecibel;
    }

    public double getcurrentLAeq() {
        return currentLAeq;
    }

    public double getCurrentLmax() {
        return currentLmax;
    }

    public String getLoudLevel() {
        return loudlevel;
    }
}
