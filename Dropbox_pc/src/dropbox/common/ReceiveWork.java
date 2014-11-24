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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import dropbox.ui.LoginFrame;
import dropbox.ui.ManagerFrame;
import dropbox.ui.TrayDropbox;
import dropbox.ui.UiMain;

public class ReceiveWork {
	public static void login(JSONObject jobj) throws JSONException {
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
				if (!dir.isDirectory()) {
					System.out.println("no dir -> make dir");
					dir.mkdir();
				}
			} else if (osName.contains("Mac")) {
				dir = new File("/users/" + userName + "/Dropbox Group");
				if (!dir.isDirectory()) {
					System.out.println("no dir -> make dir");
					dir.mkdir();
				}
			} else {
				dir = new File("/users/" + userName + "/Dropbox Group");
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

	public static void createGroup(JSONObject jobj) throws JSONException {
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		String groupId = (String) jobj.get("groupid");
		String groupName = (String) jobj.get("groupname");
		boolean result = new Boolean(jobj.get("result").toString());

		if (result) {
			JOptionPane.showMessageDialog(null, "Success");
			ManagerFrame.createGroupFrame.setVisible(false);
		} else {
			JOptionPane.showMessageDialog(null,
					"Re enter group name and comment");
		}
	}

	public static void getGroupList(JSONObject jobj) throws JSONException {
		// TODO Auto-generated method stub
		String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
		String listdata = null;

		//System.out.println("jobj : "+jobj);
		
		//listdata = jobj.getString("grouplist");
		//System.out.println(jobj.getString("grouplist"));
		JSONArray jarr = (JSONArray)jobj.get("grouplist");
		//System.out.println(jarr);

		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		// String listdata =
		// "[{\"Group_ID\":\"a\",\"Group_Master\":\"1\",\"Comment\":\"123\"},{\"Group_ID\":\"b\",\"Group_Master\":\"2\",\"Comment\":\"456\"},{\"Group_ID\":\"c\",\"Group_Master\":\"3\",\"Comment\":\"aadsfe\"}]";
		try {

			for (int i = 0; i < jarr.length(); i++) {
				JSONObject order2 = jarr.getJSONObject(i);
				HashMap<String, String> item = new HashMap<String, String>();
				item.put("Group_ID", order2.getString("groupid"));
				item.put("Group_Name", order2.getString("groupname"));
				item.put("Group_Master", order2.getString("mastername"));
				item.put("Comment", order2.getString("comment"));

				list.add(item);
			}
		} catch (JSONException e) {
			;
		}
		System.out.println("List : " +list);
		
		String[] tableHeads = { "Group Name", "Leader", ""};
		DefaultTableModel groupHeadModel = new DefaultTableModel(tableHeads, 0);
		
		for(int i=0; i<list.size(); i++){
			groupHeadModel.addRow(new Object[]{list.get(i).get("Group_Name"), list.get(i).get("Group_Master"), list.get(i).get("Group_ID")});
		}
		ManagerFrame.groupTable.setModel(groupHeadModel);
		//return list;
	}

	public static void getMemberList(JSONObject jobj) throws JSONException {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
				String subCategory = (String) jobj.get(Message.SUBCATEGORY_KEY);
				String listdata = null;

				System.out.println("jobj : "+jobj);
				
				//listdata = jobj.getString("grouplist");
				//System.out.println(jobj.getString("grouplist"));
				JSONArray jarr = (JSONArray)jobj.get("grouplist");
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
				System.out.println("List : " +list);
				
				String[] tableHeads = { "Member Id"};
				DefaultTableModel memberHeadModel = new DefaultTableModel(tableHeads, 0);
				
				for(int i=0; i<list.size(); i++){
					memberHeadModel.addRow(new Object[]{list.get(i).get("Member_ID")});
				}
				ManagerFrame.memberTable.setModel(memberHeadModel);
				//return list;
	}
}
