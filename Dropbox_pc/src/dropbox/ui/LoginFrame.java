package dropbox.ui;

import java.awt.*;

import org.json.simple.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map.Entry;
import java.util.Properties;

import org.jdesktop.swingx.prompt.PromptSupport;

import dropbox.common.Message;
import dropbox.common.MessageType;
import dropbox.common.MessageWrapper;
import dropbox.common.MySocket;

import javax.swing.*;

public class LoginFrame extends JFrame {
	private LoginPanel loginPanel;

	private JPanel mainPanel;
	public static JTextField idTextField;
	public static JPasswordField passwdTextField;
	public static Checkbox saveAccountChkBox;
	public static String stConfigPath;
	
	public LoginFrame() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Dropbox Login");
		setSize(400, 300);

		mainPanel = new JPanel();
		loginPanel = new LoginPanel(this);
		add(loginPanel);

		String configPath = this.getClass()
				.getResource("../config/loginInfo.properties").getPath();
		System.out.println(configPath);
		stConfigPath = configPath;

		File configFile = new File(configPath);

		boolean is_checked = false;
		String pre_id = "";
		String pre_pw = "";

		try (BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(configFile))) {
			Properties prop = new Properties();
			prop.load(bis);

			is_checked = new Boolean(prop.getProperty("auto_check").trim());
			pre_id = prop.getProperty("id").trim();
			pre_pw = prop.getProperty("pw").trim();

			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		saveAccountChkBox.setState(is_checked);
		idTextField.setText(pre_id);
		passwdTextField.setText(pre_pw);
		

		pack();
		setResizable(false);
		setVisible(true);
	}

	class LoginPanel extends JPanel implements ActionListener {
		LoginFrame loginFrame;

		public LoginPanel(LoginFrame lg) {
			loginFrame = lg;

			Box mainBox = Box.createVerticalBox();

			idTextField = new JTextField(15);
			passwdTextField = new JPasswordField(15);
			PromptSupport.setPrompt("User ID", idTextField);
			PromptSupport.setPrompt("Password", passwdTextField);

			JButton loginButton = new JButton(" Log In ");
			loginButton.setSize(100, 100);
			loginButton.addActionListener(this);
			JButton signUpButton = new JButton("Sign Up");
			signUpButton.addActionListener(this);

			saveAccountChkBox = new Checkbox("Save Account Info");

			JPanel subPanel1 = new JPanel();
			JPanel subPanel2 = new JPanel();
			JPanel subPanel3 = new JPanel();

			subPanel1.add(idTextField);
			subPanel1.add(loginButton);
			subPanel2.add(passwdTextField);
			subPanel2.add(signUpButton);
			subPanel3.add(saveAccountChkBox);

			mainBox.add(subPanel1);
			mainBox.add(subPanel2);
			mainBox.add(subPanel3);

			add(mainBox);

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (e.getActionCommand().equals(" Log In ")) {
				String inputID = idTextField.getText().trim();
				String inputPW = passwdTextField.getText().trim();

				JSONObject loginJObj = new JSONObject();
				loginJObj.put("id", inputID);
				loginJObj.put("password", inputPW);
				loginJObj.put(Message.SUBCATEGORY_KEY, "login");
				System.out.println(loginJObj);
				
				Message msg = new Message();
				msg.messageType = MessageType.Account;
				msg.msg = loginJObj.toJSONString();
				
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
				

				//loginFrame.setVisible(false);
				
			} else if (e.getActionCommand().equals("Sign Up")) {
				System.out.println("Sign Up");
				new SignUpFrame();
			}
		}
	}
}
