package dropbox.server.Base;

import org.mapdb.Atomic;

import java.io.Serializable;

/**
 * Created by micky on 2014. 11. 21..
 *
 */
public abstract class InfoBase implements Queriable, Serializable {
    protected final String id;
    protected final String name;

    public InfoBase(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public String getInsertQueryString(){
        return String.format("Insert into InfoBase(infoId, name) values ('%s', '%s');", id, name);
    }

    protected static String keyGenerate(String keyId){
        int num = (int)Math.round(Math.random()*100000000);
        return String.format("%s%09d", keyId, num);
    }

    @Override
    public boolean equals(Object obj) {
        InfoBase oth = (obj instanceof InfoBase)?(InfoBase)obj:null;
        return oth != null && this.id.equals(oth.id);
    }
}
