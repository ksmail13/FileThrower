package dropbox.ui;

import dropbox.groupmanage.GroupManager;
import org.json.JSONException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateGroupFrame extends JFrame implements ActionListener {
	private JTextField groupNameField;
	private JTextArea commentArea;

	public CreateGroupFrame() {
		setSize(300, 170);

		JPanel mainPanel = new JPanel();
		Box mainBox = Box.createHorizontalBox();
		Box leftBox = Box.createVerticalBox();
		Box rightBox = Box.createVerticalBox();

		JLabel groupNameLabel = new JLabel("Group Name");
		groupNameField = new JTextField(15);

		JLabel commentLabel = new JLabel("Group Comment");
		commentArea = new JTextArea(4, 10);
		commentArea.setLineWrap(true);
		JScrollPane scrollPanel = new JScrollPane(commentArea);

		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		leftBox.add(groupNameLabel);
		leftBox.add(Box.createVerticalStrut(42));
		leftBox.add(commentLabel);

		Box idSubBox = Box.createHorizontalBox();
		idSubBox.add(groupNameField);

		rightBox.add(idSubBox);
		rightBox.add(scrollPanel);

		mainBox.add(leftBox);
		mainBox.add(rightBox);

		mainPanel.add(mainBox);
		mainPanel.add(sendButton);
		mainPanel.add(cancelButton);

		add(mainPanel);

		setResizable(false);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getActionCommand().equals("Send")) {
			System.out.println("send click");
			String inputGroupName = groupNameField.getText().trim();
			String inputComment = commentArea.getText().trim();

			try {
				new GroupManager(GroupManager.CREATE_GROUP, inputGroupName, inputComment);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("Cancel")) {
			System.out.println("cancel click");
			this.setVisible(false);
		}
	}
}