package dropbox.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.*;

import org.json.simple.JSONObject;

import dropbox.common.Message;
import dropbox.common.MessageType;
import dropbox.common.MySocket;

public class SignUpFrame extends JFrame {
	public static SignUpFrame sf;
	private SignUpPanel signUpPanel;

	private JTextField idField;
	private JPasswordField passwdField;
	private JPasswordField rePasswdField;
	private JTextField emailField;

	public SignUpFrame() {
		sf = this;
		setSize(350, 190);
		setTitle("Dropbox Sign Up");
		signUpPanel = new SignUpPanel();
		add(signUpPanel);
		// pack();
		setResizable(false);
		setVisible(true);
	}

	class SignUpPanel extends JPanel implements ActionListener {
		public SignUpPanel() {
			Box mainBox = Box.createHorizontalBox();
			Box leftBox = Box.createVerticalBox();
			Box rightBox = Box.createVerticalBox();

			JLabel idLabel = new JLabel("ID");
			idField = new JTextField(15);
			JLabel passwdLabel = new JLabel("Password");
			passwdField = new JPasswordField(15);
			JLabel rePasswdLabel = new JLabel("Re-Password");
			rePasswdField = new JPasswordField(15);

			JLabel emailLabel = new JLabel("E-Mail");
			emailField = new JTextField(15);

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
			if (e.getActionCommand().equals("Send")) {
				System.out.println("send click");

				String inputID = idField.getText().trim();
				String inputPW = passwdField.getText().trim();
				String inputRePW = rePasswdField.getText().trim();
				String inputEmail = emailField.getText().trim();

				if (!inputPW.equals(inputRePW)) {
					JOptionPane
							.showMessageDialog(null,
									"Type the same password in both Password and Re-Password");
					return;
				}

				JSONObject signUpJObj = new JSONObject();

				signUpJObj.put("id", inputID);
				signUpJObj.put("password", inputPW);
				signUpJObj.put("email", inputEmail);
				signUpJObj.put(Message.SUBCATEGORY_KEY, "create");

				System.out.println(signUpJObj);
				
				Message msg = new Message();
				msg.messageType = MessageType.Account;
				msg.msg = signUpJObj.toJSONString();
				
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

			} else if (e.getActionCommand().equals("Cancel")) {
				System.out.println("cancel click");
				sf.setVisible(false);
			}
		}

	}
}