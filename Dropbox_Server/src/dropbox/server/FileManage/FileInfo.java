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
    protected final Date updateTime;
    protected final String groupId;

    public FileInfo(String id, String name, long fileSize, Date updateTime, String groupId) {
        super(id, name);
        this.fileSize = fileSize;
        this.updateTime = updateTime;
        this.groupId = groupId;
    }

    public static String keyGenerate() {
        int num = (int)Math.round(Math.random()*100000000);
        return String.format("F%9d",num);
    }

    @Override
    public String getInsertQueryString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        return super.getInsertQueryString() +
                String.format("Insert into FileInfo(infoId, fileSize, groupInfo, updateTime) values ('%s', '%s', '%s', '%s');",
                        id, fileSize, groupId, df.format(Calendar.getInstance().getTime()));
    }

    @Override
    public String getSelectQueryString() {
        return null;
    }
}
