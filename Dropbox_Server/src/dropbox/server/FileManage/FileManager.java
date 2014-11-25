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
        } else if("syncall".equals(subCategory)) {
            result = checkFile(sc, parsedObject);
        } else if("upcomplete".equals(subCategory)) {
            result = uploadComplete(sc, parsedObject);
        }
        else if("delete".equals(subCategory)) {
            result = deleteFile(sc, parsedObject);
        }

        result.put(Message.SUBCATEGORY_KEY, subCategory);
        return result;

    }

    private JSONObject deleteFile(SocketChannel sc, JSONObject parsedObject) {
        JSONObject res = new JSONObject();
        String filename = (String)parsedObject.get("filename");
        String groupid = (String)parsedObject.get("groupid");

        String deleteQuery = String.format("delete from infobase where name='%s' and infoid=(select fileid from fileinfo where groupid='%s')", filename, groupid);
        DatabaseConnector.getConnector().modify(deleteQuery);

        res.put("filename", filename);
        res.put("groupid", groupid);

        return res;
    }

    private JSONObject checkFile(SocketChannel sc, JSONObject parsedObject) {
        JSONObject res = new JSONObject();

        return res;
    }

    private JSONObject uploadComplete(SocketChannel sc, JSONObject parsedObject) {
        JSONObject res = new JSONObject();
        String groupId = (String) parsedObject.get("groupid");
        List<GroupMemberInfo> memberList = GroupManager.getManager().getGroupMemberList(groupId);

        String query = String.format("update fileinfo set uploadcomplete='true' where fileid in (select fileid from filefullinfo where groupid='%s' and name='%s')",groupId, parsedObject.get("filename"));
        DatabaseConnector.getConnector().modify(query);

        for (GroupMemberInfo info : memberList) {
            // 현재 연결되어 있는 다른 그룹원소켓
            AccountInfo accountInfo = info.getAccountInfo();
            SocketChannel tsc = AccountManager.getManager().getSession(accountInfo);
            if(tsc == null){
                Logger.errorLogging("Target "+accountInfo.getU_id()+" Socket is ", new NullPointerException());
                continue;
            }
            parsedObject.put(Message.SUBCATEGORY_KEY, "sync");
            parsedObject.put("groupname", info.getGroupInfo().getName());
            Message msg = new Message();
            msg.messageType = MessageType.File;
            msg.msg = parsedObject.toJSONString();

            Logger.debugLogging(String.format("sync file Message to %s:%s by %s\nfile Info %s", accountInfo.getId(), accountInfo.getU_id(), tsc, msg.msg));
            try {
                tsc.write(ByteBuffer.wrap(MessageWrapper.messageToByteArray(msg)));
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
            Long fileSize = (Long)parsedObject.get("filesize");

            DatabaseConnector dbConn = DatabaseConnector.getConnector();
            FileInfo newfile = new FileInfo(FileInfo.keyGenerate(), fileName, fileSize, groupId, false);
            dbConn.insert(newfile);

            res.put("result", true);
            res.put("fileid", newfile.getId());
            res.put("filename", newfile.getName());
            res.put("groupid", newfile.groupId);
        } catch (Exception e) {
            Logger.errorLogging(e);
            res.put("result", false);
        }

        return res;
    }

    private FileManager() {

    }

}
