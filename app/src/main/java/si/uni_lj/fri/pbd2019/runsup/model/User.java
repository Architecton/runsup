package si.uni_lj.fri.pbd2019.runsup.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

@DatabaseTable
public class User implements Serializable {

    @DatabaseField(generatedId = true, useGetSet = true)
    private long id;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private long accId;
    @DatabaseField(useGetSet = true)
    private int authToken;
    @DatabaseField(dataType = DataType.SERIALIZABLE, useGetSet = true)
    private ArrayList<User> friends;
    @DatabaseField(dataType = DataType.SERIALIZABLE, useGetSet = true)
    private ArrayList<Message> messages;


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

    public ArrayList<User> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<User> friends) {
        this.friends = friends;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}

