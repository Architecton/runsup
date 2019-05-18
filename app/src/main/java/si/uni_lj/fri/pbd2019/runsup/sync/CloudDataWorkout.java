package si.uni_lj.fri.pbd2019.runsup.sync;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import si.uni_lj.fri.pbd2019.runsup.model.GpsPoint;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;

public class CloudDataWorkout implements Serializable {
    private long _id;
    private List<CloudDataGpsPoint> gpsPoints;
    private String title;
    private String created;
    private int status;
    private double distance;
    private long duration;
    private double totalCalories;
    private double paceAvg;
    private int sportActivity;
    private String lastUpdate;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public List<CloudDataGpsPoint> getGpsPoints() {
        return gpsPoints;
    }

    public void setGpsPoints(List<CloudDataGpsPoint> gpsPoints) {
        this.gpsPoints = gpsPoints;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
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

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @SuppressLint("NewApi")
    public Workout toWorkout(User user) {
       Workout res = new Workout(this.title, this.sportActivity);
       res.setId(this._id);
       res.setUser(user);
       res.setDistance(this.distance);
       res.setDuration(this.duration);
       res.setStatus(this.status);
       res.setPaceAvg(this.paceAvg);
       res.setTotalCalories(this.totalCalories);

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
