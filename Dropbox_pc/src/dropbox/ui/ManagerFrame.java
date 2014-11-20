package dropbox.ui;

import java.awt.FlowLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.json.simple.JSONObject;

import dropbox.groupmanage.GroupManager;

public class ManagerFrame extends JFrame implements MouseListener, ActionListener {
	private JPopupMenu groupPopup;

	private JTable groupTable;
	private JTable memberTable;
	
	private JTextField newMemberField;

	private String selectedGroupName;
	private String selectedGroupLeader;
	private String id;

	public ManagerFrame(String id) {
		this.id = id;
		setSize(600, 350);

		Box mainBox = Box.createHorizontalBox();

		Box leftBox = Box.createVerticalBox();
		JLabel groupLabel = new JLabel("Group List");

		String[] tableHeads = { "Group Name", "Leader" };
		DefaultTableModel groupHeadModel = new DefaultTableModel(tableHeads, 0);
		groupHeadModel.addRow(new Object[] { "Group01", "Leader01" });
		groupHeadModel.addRow(new Object[] { "Group02", "Leader02" });
		groupHeadModel.addRow(new Object[] { "Group03", "Leader03" });
		groupHeadModel.addRow(new Object[] { "Group04", "Leader04" });

		groupTable = new JTable(groupHeadModel);
		groupTable.getTableHeader().setReorderingAllowed(false);
		groupTable.getTableHeader().setResizingAllowed(false);
		JScrollPane groupTablePanel = new JScrollPane(groupTable);

		groupPopup = new JPopupMenu();
		JMenuItem exitGroupItem = new JMenuItem("Exit Group");
		JMenuItem deleteGroupItem = new JMenuItem("Delete Group");
		JMenuItem groupInfoItem = new JMenuItem("Group Info");

		groupPopup.add(exitGroupItem);
		groupPopup.add(deleteGroupItem);
		groupPopup.add(groupInfoItem);

		exitGroupItem.addActionListener(this);
		deleteGroupItem.addActionListener(this);
		groupInfoItem.addActionListener(this);

		groupTable.addMouseListener(this);

		JButton createGroupButton = new JButton("Create New Group");
		createGroupButton.addActionListener(this);

		leftBox.add(groupLabel);
		leftBox.add(groupTablePanel);
		leftBox.add(createGroupButton);

		Box middleBox = Box.createVerticalBox();
		JLabel memberLabel = new JLabel("Member List");

		String[] memberHeads = { "ID" };
		DefaultTableModel memberHeadModel = new DefaultTableModel(memberHeads,
				0);
		memberTable = new JTable(memberHeadModel);
		memberTable.getTableHeader().setReorderingAllowed(false);
		memberTable.getTableHeader().setResizingAllowed(false);

		JScrollPane memberTablePanel = new JScrollPane(memberTable);

		Box inviteSubBox = Box.createHorizontalBox();
		newMemberField = new JTextField();
		JButton inviteButton = new JButton("Invite");
		inviteButton.addActionListener(this);
		inviteSubBox.add(newMemberField);
		inviteSubBox.add(inviteButton);

		middleBox.add(memberLabel);
		middleBox.add(memberTablePanel);
		middleBox.add(inviteSubBox);
		
		Box rightBox = Box.createVerticalBox();
		JLabel historyLabel = new JLabel("History");
		
		JPanel pane1 = new JPanel();
		pane1.add(new JLabel("do you wanna ?"));
		
		JPanel pane2 = new JPanel();
		pane1.add(new JLabel("File Transmit Group/File"));
		
		String[] items = {"aaa", "bbb", "ccc"}; 
		JPanel[] itmess = {pane1, pane2};
		JList historyList = new JList(itmess);
		JScrollPane historyListPanel = new JScrollPane(historyList);
		
		rightBox.add(historyLabel);
		rightBox.add(historyListPanel);
		

		mainBox.add(leftBox);
		mainBox.add(middleBox);
		mainBox.add(rightBox);
		add(mainBox);

		setResizable(false);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getActionCommand().equals("Create New Group")) {
			new CreateGroupFrame();
		} else if (e.getActionCommand().equals("Invite")) {
			JOptionPane.showMessageDialog(null, "is exited user?");
			String newMemberID = newMemberField.getText().trim();
			new GroupManager(GroupManager.INVITE, selectedGroupName, selectedGroupLeader, newMemberID);
		} else if (e.getActionCommand().equals("Exit Group")) {
			if(id.equals(selectedGroupLeader)){
				JOptionPane.showMessageDialog(null, "You are leader of the group.\n Leader can't exit group.");
				return;
			}
			new GroupManager(GroupManager.EXIT_GROUP, selectedGroupName, selectedGroupLeader, "");
		} else if (e.getActionCommand().equals("Delete Group")) {
			if(!id.equals(selectedGroupLeader)){
				JOptionPane.showMessageDialog(null, "Only leader can delete group.");
				return;
			}
			new GroupManager(GroupManager.DELETE_GROUP, selectedGroupName, selectedGroupLeader, "");
		} else if (e.getActionCommand().equals("Group Info")) {
			JOptionPane.showMessageDialog(null, "Group Info");
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		if (e.getButton() == 1) { // 클릭시
			JTable table = (JTable) e.getSource();
			int row = table.getSelectedRow(); // 선택되어진 row구하기
			if (row != -1) { // 셀이 선택되어진 상태인경우
				groupPopup.show(e.getComponent(), e.getX(), e.getY());
			}
			System.out.println(row);

			selectedGroupName = ((String) table.getModel().getValueAt(row, 0))
					.trim();
			selectedGroupLeader = ((String) table.getModel().getValueAt(row, 1))
					.trim();

			String[] memberHeads = { "ID" };
			DefaultTableModel groupHeadModel1 = new DefaultTableModel(
					memberHeads, 0);
			DefaultTableModel groupHeadModel2 = new DefaultTableModel(
					memberHeads, 0);
			DefaultTableModel groupHeadModel3 = new DefaultTableModel(
					memberHeads, 0);
			DefaultTableModel groupHeadModel4 = new DefaultTableModel(
					memberHeads, 0);
			groupHeadModel1.addRow(new Object[] { "Member1_1" });
			groupHeadModel1.addRow(new Object[] { "Member1_2" });
			groupHeadModel2.addRow(new Object[] { "Member2_1" });
			groupHeadModel2.addRow(new Object[] { "Member2_2" });
			groupHeadModel3.addRow(new Object[] { "Member3_1" });
			groupHeadModel3.addRow(new Object[] { "Member3_2" });
			groupHeadModel4.addRow(new Object[] { "Member4_1" });
			groupHeadModel4.addRow(new Object[] { "Member4_2" });

			if (row == 1) {
				memberTable.setModel(groupHeadModel1);
			}
			if (row == 2) {
				memberTable.setModel(groupHeadModel2);
			}
			if (row == 3) {
				memberTable.setModel(groupHeadModel3);
			}
			if (row == 4) {
				memberTable.setModel(groupHeadModel4);
			}
		}

		if (e.getButton() == 3) { // 우클릭시
			JTable table = (JTable) e.getSource();
			int row = table.getSelectedRow(); // 선택되어진 row구하기
			if (row != -1) { // 셀이 선택되어진 상태인경우
				groupPopup.show(e.getComponent(), e.getX(), e.getY());
			}
		}

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
