package dropbox.filemanage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import dropbox.common.MyFtpClient;

public class FileSynchronize {
	
	public FileSynchronize(WatchEvent event, String groupName, Path dir, String fileName) throws IOException{
		if(!fileName.equals(".DS_Store")){
			SynchronizeDirectory(event, groupName, dir, fileName);
		}
	}
	
	private void SynchronizeDirectory(WatchEvent event, String groupName, Path dir, String fileName) throws IOException{
		File uploadFile = new File(dir+"/"+fileName);
		long fileSize = uploadFile.length();
		
		JSONObject uploadJObj = new JSONObject();
		
		if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY){
			uploadJObj.put("EventType", "CREATE");
			
//			MyFtpClient myFtp = new MyFtpClient("192.168.0.4", 8081, "test", "test");
//			System.out.println(myFtp.login("test", "test"));
//			myFtp.send(dir.toString(), groupName, fileName);
			
		}
		else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE){
			uploadJObj.put("EventType", "DELETE");
		}
		
		uploadJObj.put("GroupID", groupName);
		
		JSONObject subFileJObj = new JSONObject();
		subFileJObj.put("FileName", fileName);
		subFileJObj.put("FileSize", fileSize);
		
		uploadJObj.put("File", subFileJObj);
		
		System.out.println("file info : " + uploadJObj);
	}
}
