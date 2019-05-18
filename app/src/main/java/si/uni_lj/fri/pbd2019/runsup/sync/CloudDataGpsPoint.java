package si.uni_lj.fri.pbd2019.runsup.sync;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Locale;

import si.uni_lj.fri.pbd2019.runsup.model.GpsPoint;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;

public class CloudDataGpsPoint implements Serializable {
    private long _id;
    private long sessionNumber;
    private double latitude;
    private double longitude;
    private long duration;
    private float speed;
    private double pace;
    private double totalCalories;
    private String created;
    private String lastUpdate;
    private byte pauseFlag;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public long getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(long sessionNumber) {
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

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public byte getPauseFlag() {
        return pauseFlag;
    }

    public void setPauseFlag(byte pauseFlag) {
        this.pauseFlag = pauseFlag;
    }


    @SuppressLint("NewApi")
    public GpsPoint toGpsPoint(Workout workout) {
        GpsPoint res = new GpsPoint();
        res.setPauseFlag(this.pauseFlag);
        res.setDuration(this.duration);
        res.setId(this._id);
        res.setLatitude(this.latitude);
        res.setLongitude(this.longitude);
        res.setPace(this.pace);
        res.setTotalCalories(this.totalCalories);
        res.setSessionNumber(this.sessionNumber);
        res.setSpeed(this.speed);
        res.setWorkout(workout);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));
        try {
            res.setCreated(sdf.parse(this.created));
            res.setLastUpdate(sdf.parse(this.lastUpdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return res;
    }
}
