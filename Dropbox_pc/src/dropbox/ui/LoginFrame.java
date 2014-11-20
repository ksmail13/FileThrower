package dropbox.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;

public class LoginFrame extends JFrame{
	private LoginPanel loginPanel;
	
	private JPanel mainPanel;
	private JTextField idTextField;
	private JPasswordField passwdTextField;
	
	public LoginFrame(){
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Dropbox Login");
		setSize(400, 300);
		
		mainPanel = new JPanel();
		loginPanel = new LoginPanel(this);
		add(loginPanel);
		
		pack();
		setResizable(false);
		setVisible(true);
	}
	
	
	
	class LoginPanel extends JPanel implements ActionListener{
		LoginFrame loginFrame;
		public LoginPanel(LoginFrame lg){
			loginFrame=lg;
			
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
			
			Checkbox saveAccountChkBox = new Checkbox("Save Account Info");
			
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
			if(e.getActionCommand().equals(" Log In ")){
				String inputID = idTextField.getText().trim();
				String inputPW = passwdTextField.getText().trim();
				
				System.out.println("ID : " + inputID + "/" + "PW : " + inputPW);
				
				
				System.out.println("log in ");
				String osName = System.getProperty("os.name");
				String userName = System.getProperty("user.name");
				System.out.println(osName + "/" + userName);
				
				File dir;
				if(osName.contains("Windows")){
					dir = new File("C:\\Dropbox Group");
					if(!dir.isDirectory()){
						System.out.println("no dir -> make dir");
						dir.mkdir();
					}
				}
				else if(osName.contains("Mac")){
					dir = new File("/users/"+userName+"/Dropbox Group");
					if(!dir.isDirectory()){
						System.out.println("no dir -> make dir");
						dir.mkdir();
					}
				}
				else{
					dir = new File("/users/"+userName+"/Dropbox Group");
					if(!dir.isDirectory()){
						System.out.println("no dir -> make dir");
						dir.mkdir();
					}
				}
				loginFrame.setVisible(false);
				new TrayDropbox(dir);
			}
			else if(e.getActionCommand().equals("Sign Up")){
				System.out.println("Sign Up");
				new SignUpFrame();
			}
		}
	}
}
