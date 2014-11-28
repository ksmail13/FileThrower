package dropbox.filemanage;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.io.*;

public class DirectoryWatch {
	private static DirectoryWatch watcher;
	
	public static DirectoryWatch getWather(File dir) {
		if(watcher == null)
			watcher = new DirectoryWatch(dir);
		return watcher;
	}
	
	public static DirectoryWatch getWatcher() {
		if(watcher == null)
			throw new NullPointerException();
		return watcher;
	}
	
	private HashMap<String, WatchThread> watchManager;
	
	public WatchThread getWatchThread(String groupId) {
		return watchManager.get(groupId);
	}
	
	public void addWatchThread(String groupName) {
		WatchThread thread = new WatchThread(rootDir.toPath(), groupName);
		thread.setName(groupName);
		thread.start();
		watchManager.put(groupName,  thread);
	}
	
	private static File rootDir;
	private File[] files;
	private ArrayList<WatchThread> watchThreads;
	private int folderCount;
	
	private DirectoryWatch(File dir) {
		rootDir = dir;
		files = dir.listFiles();

		int num = 0;
		for (File file : files) {
			if (file.listFiles() != null) {
				num++;
			}
		}
		folderCount = num;
		//watchThread = new WatchThread[folderCount];
		watchThreads = new ArrayList<WatchThread>();
		watchManager = new HashMap<String, WatchThread>();
		num = 0;
		for (File file : files) {
			if (file.listFiles() != null) {
				WatchThread watchThread = new WatchThread(file.toPath(), file.getName());
				watchThread.setName(file.getName());
				watchManager.put(file.getName(), watchThread);
				watchThreads.add(watchThread);
				num++;
			}
		}
		System.out.println(folderCount);
	}

	public void StartMonitoring() {
		System.out.println(rootDir);
		for (int i = 0; i < folderCount; i++) {
			// It's a subdirectory
			System.out.println(i + " Monitoring Start : " + watchThreads.get(i).getName());
			watchThreads.get(i).start();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void ReStartMonitoring() {
		System.out.println(rootDir);
		for (int i = 0; i < folderCount; i++) {
			// It's a subdirectory
			System.out.println("Monitoring Start : " + watchThreads.get(i).getName());
			watchThreads.get(i).resume();
			
		}
	}

	@SuppressWarnings("deprecation")
	public void StopMonitoring() {
		System.out.println(folderCount);
		
		for (int i = 0; i < folderCount; i++) {
			System.out.println("Monitoring Stop : " + watchThreads.get(i).getName());
			watchThreads.get(i).suspend();
		}
	}
}
