package dropbox.server.FileManage;

import dropbox.server.Base.InfoBase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by micky on 2014. 11. 21..
 */
public class FileInfo extends InfoBase {
    protected final long fileSize;
    protected final String groupId;
    protected final boolean uploadComplete;

    public FileInfo(String id, String name, long fileSize, String groupId, boolean uploadComplete) {
        super(id, name);
        this.fileSize = fileSize;
        this.groupId = groupId;
        this.uploadComplete = uploadComplete;
    }

    public static String keyGenerate() {
        return InfoBase.keyGenerate("F");
    }

    @Override
    public String getInsertQueryString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        return super.getInsertQueryString() +
                String.format("Insert into fileinfo (fileid, filesize, groupid) values ('%s', '%s', '%s');",
                        id, fileSize, groupId, df.format(Calendar.getInstance().getTime()));
    }

    @Override
    public String getSelectQueryString() {
        return null;
    }
}
