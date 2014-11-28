package dropbox.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import javax.swing.*;

import dropbox.filemanage.DirectoryWatch;

public class TrayDropbox {
	private static ManagerFrame managerFrame;
	public static DirectoryWatch directoryWatch;

	private static MenuItem openItem;
	private static MenuItem turnOnItem;
	private static MenuItem turnOffItem;
	private static MenuItem aboutItem;
	private static MenuItem exitItem;
	private static String id;
	
	public TrayDropbox(File dir, String id) {
		managerFrame = new ManagerFrame(id);

		this.id = id;
		createAndShowGUI();
		turnOnItem.setEnabled(false);
		directoryWatch = DirectoryWatch.getWather(dir);
		directoryWatch.StartMonitoring();
	}

	private static void createAndShowGUI() {
		// Check the SystemTray support
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		final PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(createImage("trayicon.png",
				"tray icon"));
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a popup menu components
		openItem = new MenuItem("Open");
		turnOnItem = new MenuItem("Turn On");
		turnOffItem = new MenuItem("Turn Off");
		aboutItem = new MenuItem("About");
		exitItem = new MenuItem("Exit");

		// Add components to popup menu
		popup.add(openItem);
		popup.add(turnOnItem);
		popup.add(turnOffItem);
		popup.addSeparator();
		popup.add(aboutItem);
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}

		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"This dialog box is run from System Tray");
			}
		});

		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// groupFrame.setVisible(true);
				managerFrame.show(true);
			}
		});

		turnOnItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Turn On");
				directoryWatch.ReStartMonitoring();
				turnOnItem.setEnabled(false);
				turnOffItem.setEnabled(true);
			}
		});

		turnOffItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Turn Off");
				directoryWatch.StopMonitoring();
				turnOnItem.setEnabled(true);
				turnOffItem.setEnabled(false);
			}
		});

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(
								null,
								"'bout Dropbox group Fuck ya'll\n\n\nKim Min Kyu\n\nKim Hee Joong\n\nBae Seung Oh");
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tray.remove(trayIcon);
				System.exit(0);
			}
		});
	}

	// Obtain the image URL
	protected static Image createImage(String path, String description) {
		URL imageURL = TrayDropbox.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}
}