package si.uni_lj.fri.pbd2019.runsup.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "userProfiles")
public class UserProfile {

    @DatabaseField(id = true, useGetSet = true)
    private long id;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private int user;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private int weight;

    public UserProfile() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
