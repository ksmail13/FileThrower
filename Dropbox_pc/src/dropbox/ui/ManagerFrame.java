package dropbox.ui;

import dropbox.config.ButtonColumn;
import dropbox.groupmanage.GroupManager;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;

public class ManagerFrame extends JFrame implements MouseListener,
		ActionListener {
	private JPopupMenu groupPopup;

	public static JTable groupTable;
	public static JTable memberTable;

	private JTextField newMemberField;

	private String selectedGroupName;
	private String selectedGroupLeader;
	private String selectedGroupId;
	private String id;
	
	public static CreateGroupFrame createGroupFrame;

	public ManagerFrame(String id) {
		this.id = id;
		setSize(800, 350);

		Box mainBox = Box.createHorizontalBox();

		Box leftBox = Box.createVerticalBox();
		JPanel subPane01 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel groupLabel = new JLabel("Group List");
		subPane01.add(groupLabel);

		String[] tableHeads = { "Group Name", "Leader" };
		DefaultTableModel groupHeadModel = new DefaultTableModel(tableHeads, 0);

		groupTable = new JTable(groupHeadModel);
		groupTable.getTableHeader().setReorderingAllowed(false);
		groupTable.getTableHeader().setResizingAllowed(false);
		JScrollPane groupTablePanel = new JScrollPane(groupTable);
		groupTable.getTableHeader().setPreferredSize(new Dimension(500, 20));

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

		JPanel subPane04 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton createGroupButton = new JButton("Create New Group");
		subPane04.add(createGroupButton);
		createGroupButton.addActionListener(this);

		leftBox.add(subPane01);
		leftBox.add(groupTablePanel);
		leftBox.add(subPane04);

		Box middleBox = Box.createVerticalBox();
		JPanel subPane02 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel memberLabel = new JLabel("Member List");
		subPane02.add(memberLabel);

		DefaultTableModel memberHeadModel = new DefaultTableModel(
				new Object[] { "Member ID" }, 0);
		memberTable = new JTable(memberHeadModel);
		memberTable.getTableHeader().setReorderingAllowed(false);
		memberTable.getTableHeader().setResizingAllowed(false);
		memberTable.getTableHeader().setPreferredSize(new Dimension(500, 20));

		JScrollPane memberTablePanel = new JScrollPane(memberTable);

		JPanel inviteSubBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		newMemberField = new JTextField(13);
		JButton inviteButton = new JButton("Invite");
		inviteButton.addActionListener(this);
		inviteSubBox.add(newMemberField);
		inviteSubBox.add(inviteButton);

		middleBox.add(subPane02);
		middleBox.add(memberTablePanel);
		middleBox.add(inviteSubBox);

		Box rightBox = Box.createVerticalBox();
		JPanel subPane03 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel historyLabel = new JLabel("History");
		subPane03.add(historyLabel);

		JPanel pane1 = new JPanel();
		pane1.add(new JLabel("do you wanna ?"));

		JPanel pane2 = new JPanel();
		pane1.add(new JLabel("File Transmit Group/File"));

		String[] items = { "aaa", "bbb", "ccc" };
		JPanel[] itmess = { pane1, pane2 };

		DefaultTableModel historyTableModel = new DefaultTableModel(
				new String[] { "Contents", "" }, 0);
		

		JTable historyTable = new JTable(historyTableModel);
		historyTable.getTableHeader().setReorderingAllowed(false);
		historyTable.getTableHeader().setResizingAllowed(false);
		historyTable.getTableHeader().setPreferredSize(new Dimension(500, 20));

		for (int i = 0; i < historyTable.getColumnCount(); i++) {
			TableColumn column = historyTable.getColumnModel().getColumn(i);
			if (i == 0) {
				column.setPreferredWidth(170);
			} else if (i == 1) {
				column.setPreferredWidth(60);
			}
		}

		Action delete = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTable table = (JTable) e.getSource();
				int modelRow = Integer.valueOf(e.getActionCommand());
				((DefaultTableModel) table.getModel()).removeRow(modelRow);
			}
		};

		ButtonColumn buttonColumn = new ButtonColumn(historyTable, delete, 1);
		buttonColumn.setMnemonic(KeyEvent.VK_D);

		JScrollPane historyListPanel = new JScrollPane(historyTable);

		rightBox.add(subPane03);
		rightBox.add(historyListPanel);

		mainBox.add(leftBox);
		mainBox.add(middleBox);
		mainBox.add(rightBox);
		add(mainBox);

		
		try {
			new GroupManager(GroupManager.SELECT_GROUP);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		pack();
		setSize(800, 400);
		setResizable(false);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getActionCommand().equals("Create New Group")) {
			createGroupFrame = new CreateGroupFrame();
		} else if (e.getActionCommand().equals("Invite")) {
			String newMemberID = newMemberField.getText().trim();
			if(newMemberID.equals("")){
				JOptionPane
				.showMessageDialog(null,
						"Please don't enter empty id.");
			}
			try {
				new GroupManager(GroupManager.INVITE, selectedGroupId, selectedGroupLeader, newMemberID);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("Exit Group")) {
			try {
				new GroupManager(GroupManager.EXIT_GROUP, selectedGroupId,
						selectedGroupLeader, "");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("Delete Group")) {
			if (!id.equals(selectedGroupLeader)) {
				JOptionPane.showMessageDialog(null,
						"Only leader can delete group.");
				return;
			}
			try {
				new GroupManager(GroupManager.DELETE_GROUP, selectedGroupId,
						selectedGroupLeader, "");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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

			selectedGroupName = ((String) table.getModel().getValueAt(row, 0)).trim();
			selectedGroupLeader = ((String) table.getModel().getValueAt(row, 1)).trim();
			selectedGroupId = ((String) table.getModel().getValueAt(row, 2)).trim();
			System.out.println("gname : "+selectedGroupName + " g_id : "+selectedGroupId);
			
			try {
				new GroupManager(GroupManager.SELECT_MEMBER, selectedGroupId);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
//			
//			String[] memberHeads = { "ID" };
//			DefaultTableModel groupHeadModel1 = new DefaultTableModel(
//					memberHeads, 0);
//			DefaultTableModel groupHeadModel2 = new DefaultTableModel(
//					memberHeads, 0);
//			DefaultTableModel groupHeadModel3 = new DefaultTableModel(
//					memberHeads, 0);
//			DefaultTableModel groupHeadModel4 = new DefaultTableModel(
//					memberHeads, 0);
//			groupHeadModel1.addRow(new Object[] { "Member1_1" });
//			groupHeadModel1.addRow(new Object[] { "Member1_2" });
//			groupHeadModel2.addRow(new Object[] { "Member2_1" });
//			groupHeadModel2.addRow(new Object[] { "Member2_2" });
//			groupHeadModel3.addRow(new Object[] { "Member3_1" });
//			groupHeadModel3.addRow(new Object[] { "Member3_2" });
//			groupHeadModel4.addRow(new Object[] { "Member4_1" });
//			groupHeadModel4.addRow(new Object[] { "Member4_2" });
//
//			if (row == 1) {
//				memberTable.setModel(groupHeadModel1);
//			}
//			if (row == 2) {
//				memberTable.setModel(groupHeadModel2);
//			}
//			if (row == 3) {
//				memberTable.setModel(groupHeadModel3);
//			}
//			if (row == 4) {
//				memberTable.setModel(groupHeadModel4);
//			}
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
