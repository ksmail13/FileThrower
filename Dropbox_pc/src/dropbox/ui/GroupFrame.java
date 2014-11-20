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

public class GroupFrame extends JFrame implements MouseListener, ActionListener{
	private JPopupMenu groupPopup;
	public GroupFrame(){
		setSize(400, 350);
		
		Box mainBox = Box.createHorizontalBox();
		
		Box leftBox = Box.createVerticalBox();
		JLabel groupLabel = new JLabel("Group List");
		
		String[] tableHeads = {"Group Name", "Leader"};
		DefaultTableModel groupHeadModel = new DefaultTableModel(tableHeads, 1);
		
		JTable groupTable = new JTable(groupHeadModel);
		groupTable.getTableHeader().setReorderingAllowed(false);
		groupTable.getTableHeader().setResizingAllowed(false);
		
		groupPopup = new JPopupMenu();
		JMenuItem exitGroupItem = new JMenuItem("Exit Group");
	    JMenuItem deleteGroupItem = new JMenuItem("Delete Group");
	    JMenuItem groupInfoItem= new JMenuItem("Group Info");
		
	    groupPopup.add(exitGroupItem);
	    groupPopup.add(deleteGroupItem);
	    groupPopup.add(groupInfoItem);
	    
	    exitGroupItem.addActionListener(this);
	    deleteGroupItem.addActionListener(this);
	    groupInfoItem.addActionListener(this);
	    
	    groupTable.addMouseListener(this);
	    
		JScrollPane groupTablePanel = new JScrollPane(groupTable);
		
		
		JButton createGroupButton = new JButton("Create New Group");
		createGroupButton.addActionListener(this);
		
		leftBox.add(groupLabel);
		leftBox.add(groupTablePanel);
		leftBox.add(createGroupButton);
		
		Box rightBox = Box.createVerticalBox();
		JLabel memberLabel = new JLabel("Member List");
		
		String[] memberHeads = {"ID"};
		DefaultTableModel memberHeadModel = new DefaultTableModel(memberHeads, 1);
		JTable memberTable = new JTable(memberHeadModel);
		memberTable.getTableHeader().setReorderingAllowed(false);
		memberTable.getTableHeader().setResizingAllowed(false);
		
		JScrollPane memberTablePanel = new JScrollPane(memberTable);
		
		Box inviteSubBox = Box.createHorizontalBox();
		JTextField newMemberField = new JTextField();
		JButton inviteButton = new JButton("Invite");
		inviteButton.addActionListener(this);
		inviteSubBox.add(newMemberField);
		inviteSubBox.add(inviteButton);
		
		rightBox.add(memberLabel);
		rightBox.add(memberTablePanel);
		rightBox.add(inviteSubBox);
		
		mainBox.add(leftBox);
		mainBox.add(rightBox);
		add(mainBox);
		
		setResizable(false);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals("Create New Group")){
			JOptionPane.showMessageDialog(null,"Create New Group");
			File dir = new File("/users/heejoongkim/monitor/mkdirrr");
			if(!dir.isDirectory()){
				System.out.println("no direc");
				dir.mkdir();
			}
		}
		else if(e.getActionCommand().equals("Invite")){
			JOptionPane.showMessageDialog(null,"Invite");
			File dir = new File("/users/heejoongkim/monitor/asdf.pdf");
            if(dir.exists()) {
                boolean delFlag = dir.delete();
                    if(delFlag) {
                        System.out.println("WAS 저장파일 삭제 성공!");
                    } else {
                        System.out.println("WAS 저장파일 삭제 실패!");
                    }                                            
            } else {
                System.out.println("삭제할 파일이 존재하지 않습니다.");
            }
		}
		else if(e.getActionCommand().equals("Exit Group")){
			JOptionPane.showMessageDialog(null,"Exit group");
		}
		else if(e.getActionCommand().equals("Delete Group")){
			JOptionPane.showMessageDialog(null,"Delete Group");
		}
		else if(e.getActionCommand().equals("Group Info")){
			JOptionPane.showMessageDialog(null,"Group Info");
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		 if(e.getButton() == 3){ // 우클릭시
             JTable table = (JTable)e.getSource();
             int row = table.getSelectedRow(); // 선택되어진 row구하기
             if(row != -1 ){ // 셀이 선택되어진 상태인경우
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
