package dropbox.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import dropbox.filemanage.DirectoryWatch;
import dropbox.groupmanage.GroupManager;
import dropbox.ui.LoginFrame;
import dropbox.ui.ManagerFrame;
import dropbox.ui.SignUpFrame;
import dropbox.ui.TrayDropbox;
import dropbox.ui.UiMain;

public class ReceiveWork {
	private volatile static ReceiveWork workInstance;
	public synchronized static ReceiveWork getInstance() {
		if(workInstance == null)
			workInstance = new ReceiveWork();
		return workInstance;
	}
	
	private ReceiveWork() { }
	
	private String rootDirStr;

	public synchronized void login(JSONObject jobj) throws JSONException {
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		String uid = (String) jobj.get("uid");
		boolean result = new Boolean(jobj.get("result").toString());

		if (result) {

			System.out.println("log in ");
			String osName = System.getProperty("os.name");
			String userName = System.getProperty("user.name");
			System.out.println(osName + "/" + userName);

			File dir;
			if (osName.contains("Windows")) {
				dir = new File("C:/Dropbox Group");
				rootDirStr = dir.toString();
				if (!dir.isDirectory()) {
					System.out.println("no dir -> make dir");
					dir.mkdir();
				}
			} else if (osName.contains("Mac")) {
				dir = new File("/users/" + userName + "/Dropbox Group");
				rootDirStr = dir.toString();
				if (!dir.isDirectory()) {
					System.out.println("no dir -> make dir");
					dir.mkdir();
				}
			} else {
				dir = new File("/users/" + userName + "/Dropbox Group");
				rootDirStr = dir.toString();
				if (!dir.isDirectory()) {
					System.out.println("no dir -> make dir");
					dir.mkdir();
				}
			}

			String configPath = LoginFrame.stConfigPath;
			System.out.println(configPath);

			File configFile = new File(configPath);

			boolean is_checked = LoginFrame.saveAccountChkBox.getState();
			String pre_id = LoginFrame.idTextField.getText().trim();
			String pre_pw = LoginFrame.passwdTextField.getText().trim();

			if (!is_checked) {
				pre_id = "";
				pre_pw = "";
			}

			try (BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(configFile))) {
				Properties prop = new Properties();
				prop.load(bis);

				prop.setProperty("auto_check", is_checked + "");
				prop.setProperty("id", pre_id);
				prop.setProperty("pw", pre_pw);

				FileOutputStream fos = new FileOutputStream(configFile);

				prop.store(fos, "");
				fos.flush();
				bis.close();
				fos.close();

			} catch (Exception e1) {
				e1.printStackTrace();
			}

			UiMain.loginFrame.setVisible(false);
			new TrayDropbox(dir, uid);
		} else {
			LoginFrame.idTextField.setText("");
			LoginFrame.passwdTextField.setText("");
			JOptionPane.showMessageDialog(null, "Re enter id and password");
		}
	}

	public void createGroup(JSONObject jobj) throws JSONException {
		System.out.println("createGroup");
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		String groupId = (String) jobj.get("groupid");
		String groupName = (String) jobj.get("groupname");
		boolean result = new Boolean(jobj.get("result").toString());

		if (result) {
			JOptionPane.showMessageDialog(null, "Success");
			ManagerFrame.createGroupFrame.setVisible(false);
			new GroupManager(GroupManager.SELECT_GROUP);
			DirectoryWatch.getWatcher().addWatchThread(groupName);
		} else {
			JOptionPane.showMessageDialog(null,
					"Re enter group name and comment");
		}
	}

	public void getGroupList(JSONObject jobj) throws JSONException {
		System.out.println("getGroupList");
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		String listdata = null;

		System.out.println("grouplist jobj : " + jobj);

		// listdata = jobj.getString("grouplist");
		// System.out.println(jobj.getString("grouplist"));
		JSONArray jarr = (JSONArray) jobj.get("grouplist");
		// System.out.println(jarr);

		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		// String listdata =
		// "[{\"Group_ID\":\"a\",\"Group_Master\":\"1\",\"Comment\":\"123\"},{\"Group_ID\":\"b\",\"Group_Master\":\"2\",\"Comment\":\"456\"},{\"Group_ID\":\"c\",\"Group_Master\":\"3\",\"Comment\":\"aadsfe\"}]";
		System.out.println(jarr.length());
		for (int i = 0; i < jarr.length(); i++) {
			JSONObject order2 = jarr.getJSONObject(i);
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("Group_ID", order2.getString("groupid"));
			item.put("Group_Name", order2.getString("groupname"));

			File dir = new File(rootDirStr + "/"
					+ order2.getString("groupname"));
			if (!dir.isDirectory()) {
				System.out.println("no dir, make dir : " + dir);
				dir.mkdir();
			}

			item.put("Group_Master", order2.getString("mastername"));
			// item.put("Group_Master", "masterid");
			item.put("Comment", order2.getString("comment"));

			list.add(item);
		}
		System.out.println("test : " + list);
		System.out.println("Group List : " + list);

		String[] tableHeads = { "Group Name", "Leader", "", "" };
		DefaultTableModel groupHeadModel = new DefaultTableModel(tableHeads, 0);

		for (int i = 0; i < list.size(); i++) {
			groupHeadModel.addRow(new Object[] { list.get(i).get("Group_Name"),
					list.get(i).get("Group_Master"),
					list.get(i).get("Group_ID"), list.get(i).get("Comment") });
		}

		ManagerFrame.groupTable.setModel(groupHeadModel);
		
		for (int i = 0; i < ManagerFrame.groupTable.getColumnCount(); i++) {
			TableColumn column = ManagerFrame.groupTable.getColumnModel()
					.getColumn(i);

			if (i == 0) {
				column.setPreferredWidth(200);
			} else if (i == 1) {
				column.setPreferredWidth(200);
			}else if (i == 2) {
				column.setMinWidth(0);
				column.setPreferredWidth(0);
			} else if (i == 3) {
				column.setMinWidth(0);
				column.setPreferredWidth(0);
			}
		}
		//TrayDropbox.directoryWatch.StopMonitoring();
		//TrayDropbox.directoryWatch = new DirectoryWatch(new File(rootDirStr));
		//TrayDropbox.directoryWatch.StartMonitoring();
		// return list;
	}

	public void getMemberList(JSONObject jobj) throws JSONException {
		System.out.println("getMemberlist");
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		String listdata = null;

		System.out.println("memberlist jobj : " + jobj);

		// listdata = jobj.getString("grouplist");
		// System.out.println(jobj.getString("grouplist"));
		JSONArray jarr = (JSONArray) jobj.get("grouplist");
		System.out.println(jarr);

		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		// String listdata =
		// "[{\"Group_ID\":\"a\",\"Group_Master\":\"1\",\"Comment\":\"123\"},{\"Group_ID\":\"b\",\"Group_Master\":\"2\",\"Comment\":\"456\"},{\"Group_ID\":\"c\",\"Group_Master\":\"3\",\"Comment\":\"aadsfe\"}]";
		try {

			for (int i = 0; i < jarr.length(); i++) {
				JSONObject order2 = jarr.getJSONObject(i);
				HashMap<String, String> item = new HashMap<String, String>();
				item.put("Member_ID", order2.getString("id"));

				list.add(item);
			}
		} catch (JSONException e) {
			;
		}
		System.out.println("Member List : " + list);

		String[] tableHeads = { "Member Id" };
		DefaultTableModel memberHeadModel = new DefaultTableModel(tableHeads, 0);

		for (int i = 0; i < list.size(); i++) {
			memberHeadModel
					.addRow(new Object[] { list.get(i).get("Member_ID") });
		}
		ManagerFrame.memberTable.setModel(memberHeadModel);
		// return list;
	}

	public void addMember(JSONObject jobj) throws JSONException {
		// TODO Auto-generated method stub
		System.out.println("add member");
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		String groupId = (String) jobj.get("groupid");
		String inviteid = (String) jobj.get("inviteid");
		boolean result = new Boolean(jobj.get("result").toString());

		System.out.println(jobj);

		if (result) {
			JOptionPane.showMessageDialog(null, "Success");
			new GroupManager(GroupManager.SELECT_GROUP);
		} else {
			JOptionPane.showMessageDialog(null, "No User");
		}
	}

	public void createAccount(JSONObject jobj) throws JSONException {
		// TODO Auto-generated method stub
		System.out.println("add member");
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		boolean result = new Boolean(jobj.get("result").toString());

		System.out.println(jobj);

		if (result) {
			JOptionPane.showMessageDialog(null, "Success");
			SignUpFrame.sf.setVisible(false);
		} else {
			JOptionPane.showMessageDialog(null, "No User");
		}
	}

	public void downloadFile(JSONObject jobj) throws JSONException {
		// TODO Auto-generated method stub
		System.out.println("download file");
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		String groupId = (String) jobj.get("groupid");
		String groupName = (String) jobj.get("groupname");
		String fileName = (String) jobj.get("filename");
		
		MyFtpClient myFtp = new MyFtpClient("10.0.26.191", 8081, "test", "test");
		System.out.println("file download success : " + myFtp.login("test", "test"));
		DirectoryWatch.getWatcher().getWatchThread(groupName).addIgnoreFile(groupName, fileName);
		myFtp.get(rootDirStr, groupName, groupId, fileName);
	}

	public void exitGroup(JSONObject jobj) throws JSONException {
		// TODO Auto-generated method stub
		System.out.println("exit group");
		
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		boolean result = new Boolean(jobj.get("result").toString());
		
		if (result) {
			JOptionPane.showMessageDialog(null, "Success");
			
			//File file = new File(rootDirStr+"/"+)
			
			try {
				new GroupManager(GroupManager.SELECT_GROUP);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "Fail");
		}
	}

	public void deleteGroup(JSONObject jobj) throws JSONException {
		// TODO Auto-generated method stub
		System.out.println("exit group");
		
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		boolean result = new Boolean(jobj.get("result").toString());
		
		if (result) {
			JOptionPane.showMessageDialog(null, "Success");
			
			//File file = new File(rootDirStr+"/"+)
			
			try {
				new GroupManager(GroupManager.SELECT_GROUP);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "Fail");
		}
	}
}
