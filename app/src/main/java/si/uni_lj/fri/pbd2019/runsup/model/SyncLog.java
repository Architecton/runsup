package si.uni_lj.fri.pbd2019.runsup.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "SyncLog")
public class SyncLog {

    @DatabaseField(generatedId = true, useGetSet = true)
    private long id;

    public SyncLog() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

