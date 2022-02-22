package com.harjeet.missedcallduration;

/**
 * Created by HARJEET on 05-May-17.
 */

public class MissedData {

    private long id;
    private String number;
    private String start;
    private Double duration;

    public MissedData() {
    }

    public MissedData(long id, String number, String start, Double duration) {
        this.id = id;
        this.number = number;
        this.start = start;
        this.duration = duration;
    }

    public Double getDuration() {
        return duration;
    }

    public long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getStart() {
        return start;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setStart(String start) {
        this.start = start;
    }
}
