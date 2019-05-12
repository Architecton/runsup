package si.uni_lj.fri.pbd2019.runsup.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "User")
public class User {

    @DatabaseField(generatedId = true, useGetSet = true)
    private long id;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private long accId;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private int authToken;


    public User() {
    }

    public User(String accId) {
        this.accId = accId.hashCode();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAccId() {
        return accId;
    }

    public void setAccId(long accId) {
        this.accId = accId;
    }

    public int getAuthToken() {
        return authToken;
    }

    public void setAuthToken(int authToken) {
        this.authToken = authToken;
    }
}

