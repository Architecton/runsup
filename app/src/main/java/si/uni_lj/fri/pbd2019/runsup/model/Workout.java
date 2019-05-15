package si.uni_lj.fri.pbd2019.runsup.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

@DatabaseTable
public class Workout implements Serializable {


    @DatabaseField(generatedId = true, useGetSet = true)
    private long id;
    @DatabaseField(foreign=true, foreignAutoRefresh=true)
    private User user;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private String title;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private Date created;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private int status;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private double distance;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private long duration;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private double totalCalories;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private double paceAvg;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private int sportActivity;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private Date lastUpdate;


    // initial status of workout
    public static final int statusUnknown = 0;
    // ended workout
    public static final int statusEnded = 1;
    // paused workout
    public static final int statusPaused = 2;
    // deleted workout
    public static final int statusDeleted= 3;
    // running workout
    public static final int statusRunning = 4;


    public Workout() {
    }

    public Workout(String title, int sportActivity) {
        this.title = title;
        this.sportActivity = sportActivity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
    }

    public double getPaceAvg() {
        return paceAvg;
    }

    public void setPaceAvg(double paceAvg) {
        this.paceAvg = paceAvg;
    }

    public int getSportActivity() {
        return sportActivity;
    }

    public void setSportActivity(int sportActivity) {
        this.sportActivity = sportActivity;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
