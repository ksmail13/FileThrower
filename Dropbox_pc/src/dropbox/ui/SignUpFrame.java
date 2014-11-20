package dropbox.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class SignUpFrame extends JFrame{
	private SignUpFrame sf;
	private SignUpPanel signUpPanel;
	
	public SignUpFrame(){
		sf=this;
		setSize(420, 190);
		setTitle("Dropbox Sign Up");
		signUpPanel = new SignUpPanel();
		add(signUpPanel);
		//pack();
		setResizable(false);
		setVisible(true);
	}

	class SignUpPanel extends JPanel implements ActionListener{
		public SignUpPanel() {
			Box mainBox = Box.createHorizontalBox();
			Box leftBox = Box.createVerticalBox();
			Box rightBox = Box.createVerticalBox();
			
			JLabel idLabel = new JLabel("ID");
			JTextField idField = new JTextField(15);
			JButton dupChkButton = new JButton("Check avail.");
			dupChkButton.addActionListener(this);
			JLabel passwdLabel = new JLabel("Password");
			JPasswordField passwdField = new JPasswordField(15);
			JLabel rePasswdLabel = new JLabel("Re-Password");
			JPasswordField rePasswdField = new JPasswordField(15);
			
			
			JLabel emailLabel = new JLabel("E-Mail");
			JTextField emailField = new JTextField(15);
			
			JButton sendButton = new JButton("Send");
			sendButton.addActionListener(this);
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);
			
			leftBox.add(idLabel);
			leftBox.add(Box.createVerticalStrut(12));
			leftBox.add(passwdLabel);
			leftBox.add(Box.createVerticalStrut(12));
			leftBox.add(rePasswdLabel);
			leftBox.add(Box.createVerticalStrut(12));
			leftBox.add(emailLabel);
			leftBox.add(Box.createVerticalStrut(12));

			Box idSubBox = Box.createHorizontalBox();
			idSubBox.add(idField);
			idSubBox.add(dupChkButton);
			
			rightBox.add(idSubBox);
			rightBox.add(passwdField);
			rightBox.add(rePasswdField);
			
			Box birthSubBox = Box.createHorizontalBox();
			rightBox.add(birthSubBox);
			
			rightBox.add(emailField);
			
			mainBox.add(leftBox);
			mainBox.add(rightBox);
			add(mainBox);
			add(sendButton);
			add(cancelButton);
			
			setVisible(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if(e.getActionCommand().equals("Send")){
				System.out.println("send click");
			}
			else if(e.getActionCommand().equals("Cancel")){
				System.out.println("cancel click");
				sf.setVisible(false);
			}
		}
		
	}
}