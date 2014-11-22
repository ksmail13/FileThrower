package dropbox.server.Base;

import org.mapdb.Atomic;

/**
 * Created by micky on 2014. 11. 21..
 *
 */
public abstract class InfoBase implements Queriable {
    protected String id;
    protected String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInsertQueryString(){
        return String.format("Insert into InfoBase(infoId, name) values (%s, %s);", id, name);
    }

    public abstract String keyGenerate();
}
