package si.uni_lj.fri.pbd2019.runsup.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "UserProfile")
public class UserProfile {

    @DatabaseField(generatedId = true, useGetSet = true)
    private long id;
    @DatabaseField(foreign=true, foreignAutoRefresh=true)
    private User user;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
