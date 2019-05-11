package si.uni_lj.fri.pbd2019.runsup.model;

import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;


@DatabaseTable(tableName = "gpsPoints")
public class GpsPoint {


    @DatabaseField(id = true, useGetSet = true)
    private long id;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private Workout workout;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private Long sessionNumber;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private double latitude;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private double longitude;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private long duration;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private float speed;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private double pace;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private double totalCalories;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private Date created;

    @DatabaseField(canBeNull = false, useGetSet = true)
    private Date lastUpdate;


    public GpsPoint() {
    }


    public GpsPoint(Workout workout, long sessionNumber, Location location, long duration, float speed, double pace, double totalCalories) {
        this.workout = workout;
        this.sessionNumber = sessionNumber;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.duration = duration;
        this.speed = speed;
        this.pace = pace;
        this.totalCalories = totalCalories;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Workout getWorkout() {
        return workout;
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;
    }

    public Long getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(Long sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public double getPace() {
        return pace;
    }

    public void setPace(double pace) {
        this.pace = pace;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
