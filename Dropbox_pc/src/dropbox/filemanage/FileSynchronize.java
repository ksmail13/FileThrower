package dropbox.filemanage;

import dropbox.common.Message;
import dropbox.common.MessageType;
import dropbox.common.MyFtpClient;
import dropbox.common.MySocket;
import dropbox.ui.ManagerFrame;
import org.json.JSONException;
import org.json.simple.JSONObject;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

public class FileSynchronize {
	
	public FileSynchronize(WatchEvent event, String groupName, Path dir, String fileName) throws IOException, JSONException{
		if(!fileName.equals(".DS_Store")){
			SynchronizeDirectory(event, groupName, dir, fileName);
		}
	}
	
	private void SynchronizeDirectory(WatchEvent event, String groupName, Path dir, String fileName) throws IOException, JSONException{
		File uploadFile = new File(dir+"/"+fileName);
		long fileSize = uploadFile.length();
		
		JSONObject uploadJObj = new JSONObject();
		
		DefaultTableModel tmpModel = (DefaultTableModel) ManagerFrame.groupTable.getModel();
    	int i;
    	for(i=0; i<tmpModel.getRowCount(); i++){
    		if(tmpModel.getValueAt(i, 0).equals(groupName)){
    			break;
    		}
    	}
    	String groupId = ((String) tmpModel.getValueAt(i, 2)).trim();
		
		if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY){
			uploadJObj.put(Message.SUBCATEGORY_KEY, "upload");
			
			MyFtpClient myFtp = new MyFtpClient("10.0.26.191", 8081, "test", "test");
			System.out.println("file upload success : " + myFtp.login("test", "test"));
			myFtp.send(dir.toString(), groupId, fileName);
			
			uploadJObj.put("filesize", fileSize);
			uploadJObj.put("filename", fileName);
			uploadJObj.put("groupid", groupId);
			System.out.println("file info : " + uploadJObj);
			
			Message msg = new Message();
			msg.messageType = MessageType.File;
			msg.msg = uploadJObj.toJSONString();
			
			try {
				MySocket ms = MySocket.getConnector();
				ms.send(msg);
			} catch (UnknownHostException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE){
			uploadJObj.put("EventType", "DELETE");
		}
		
		
		JSONObject subFileJObj = new JSONObject();
		subFileJObj.put("FileName", fileName);
		subFileJObj.put("FileSize", fileSize);
		
		uploadJObj.put("File", subFileJObj);
		
	}
}
