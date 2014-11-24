package dropbox.server.FileManage;

import dropbox.common.ByteConverter;
import dropbox.common.Message;
import dropbox.common.MessageType;
import dropbox.common.MessageWrapper;
import dropbox.server.Account.AccountInfo;
import dropbox.server.Account.AccountManager;
import dropbox.server.Base.ManagerBase;
import dropbox.server.Group.GroupManager;
import dropbox.server.Group.GroupMemberInfo;
import dropbox.server.Util.DatabaseConnector;
import dropbox.server.Util.Logger;
import org.json.simple.JSONObject;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Calendar;
import java.util.List;

/**
 * Created by micky on 2014. 11. 21..
 */
public class FileManager extends ManagerBase {
    public final static String DBNAME = "FileManager";

    private static FileManager manager = null;

    public static FileManager getManager() {
        if(manager == null) {
            manager = new FileManager();
        }
        return manager;
    }

    @Override
    public JSONObject messageHandling(SocketChannel sc, JSONObject parsedObject) {
        JSONObject result = null;
        String subCategory = (String) parsedObject.get(Message.SUBCATEGORY_KEY);
        if("upload".equals(subCategory)) {
            result = uploadFile(AccountManager.getManager().getLoginInfo(sc), parsedObject);
        } else if("sync".equals(subCategory)) {
            result = checkFile(AccountManager.getManager().getLoginInfo(sc), parsedObject);
        } else if("upcomplete".equals(subCategory)) {
            result = uploadComplete(AccountManager.getManager().getLoginInfo(sc), parsedObject);
        }
//        else if("addmember".equals(subCategory)) {
//        } else if("change".equals(subCategory)) {
//        } else if("exitgroup".equals(subCategory)) {
//        } else if("delete".equals(subCategory)) {
//        }

        result.put(Message.SUBCATEGORY_KEY, subCategory);
        return result;

    }

    private JSONObject checkFile(AccountInfo loginInfo, JSONObject parsedObject) {
        JSONObject res = new JSONObject();
        String loginId = loginInfo.getId();


        return res;
    }

    private JSONObject uploadComplete(AccountInfo loginInfo, JSONObject parsedObject) {
        JSONObject res = new JSONObject();
        String groupId = (String) parsedObject.get("groupid");
        List<GroupMemberInfo> memberList = GroupManager.getManager().getGroupMemberList(groupId);

        for (GroupMemberInfo info : memberList) {
            SocketChannel sc = AccountManager.getManager().getSession(info.getAccountInfo());

            DatabaseConnector.getConnector().modify("update fileinfo set uploadcomplete='true' where groupid='%s' and ");

            Message msg = new Message();
            msg.messageType = MessageType.File;
            msg.msg = parsedObject.toJSONString();
            try {
                sc.write(ByteBuffer.wrap(MessageWrapper.messageToByteArray(msg)));
            } catch (IOException e) {
                Logger.errorLogging(e);
            }
        }
        return res;
    }

    private JSONObject uploadFile(AccountInfo loginInfo, JSONObject parsedObject) {
        JSONObject res = new JSONObject();
        try {
            String fileName = (String) parsedObject.get("filename");
            String groupId = (String) parsedObject.get("groupid");
            Long fileSize = (Long) parsedObject.get("filesize");

            DatabaseConnector dbConn = DatabaseConnector.getConnector();
            dbConn.insert(new FileInfo(FileInfo.keyGenerate(), fileName, fileSize, Calendar.getInstance().getTime(), groupId, false));

            res.put("result", true);

        } catch (Exception e) {
            res.put("result", false);
        }

        return res;
    }

    private FileManager() {

    }

}
