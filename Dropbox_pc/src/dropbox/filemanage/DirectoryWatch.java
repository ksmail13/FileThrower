package dropbox.filemanage;

import java.nio.file.*;
import java.util.List;
import java.io.*;

public class DirectoryWatch {
	private File rootDir;
	private File[] files;
	private WatchThread[] watchThread;
	private int folderCount;

	public DirectoryWatch(File dir) {
		rootDir = dir;
		files = dir.listFiles();

		int num = 0;
		for (File file : files) {
			if (file.listFiles() != null) {
				num++;
			}
		}
		folderCount = num;
		watchThread = new WatchThread[folderCount];
		
		num = 0;
		for (File file : files) {
			if (file.listFiles() != null) {
				watchThread[num] = new WatchThread(file.toPath(), file.getName());
				num++;
			}
		}
		System.out.println(folderCount);
	}

	public void StartMonitoring() {
		System.out.println(rootDir);
		for (int i = 0; i < folderCount; i++) {
			// It's a subdirectory
			System.out.println(i + " Monitoring Start : " + watchThread[i].dir);
			watchThread[i].start();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void ReStartMonitoring() {
		System.out.println(rootDir);
		for (int i = 0; i < folderCount; i++) {
			// It's a subdirectory
			System.out.println("Monitoring Start : " + watchThread[i].dir);
			watchThread[i].resume();
			
		}
	}

	@SuppressWarnings("deprecation")
	public void StopMonitoring() {
		System.out.println(folderCount);
		
		for (int i = 0; i < folderCount; i++) {
			System.out.println("Monitoring Stop : " + watchThread[i].dir);
			watchThread[i].suspend();
		}
	}

	public void testForDirectoryChange(Path myDir, String groupName) {
		while (true) {
			try {
				WatchService watcher = myDir.getFileSystem().newWatchService();
				myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE,
						StandardWatchEventKinds.ENTRY_MODIFY);

				WatchKey watckKey = watcher.take();

				List<WatchEvent<?>> events = watckKey.pollEvents();
				for (WatchEvent event : events) {
					if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						System.out.println("Group : " + groupName + " Created: " + myDir + "/" + event.context().toString());
						new FileSynchronize(event, groupName, myDir, event.context().toString().trim());
					}
					if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
						System.out.println("Group : " + groupName + " Delete: " + myDir + "/" + event.context().toString());
						new FileSynchronize(event, groupName, myDir, event.context().toString().trim());
					}
					if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
						System.out.println("Group : " + groupName + " Modify: " + myDir + "/" + event.context().toString());
						new FileSynchronize(event, groupName, myDir, event.context().toString().trim());
					}
				}

			} catch (Exception e) {
				System.out.println("Error: " + e.toString());
			}
		}
	}

	class WatchThread extends Thread {
		private Path dir;
		private String groupName;
		
		public WatchThread(Path dir, String groupName) {
			this.dir = dir;
			this.groupName = groupName;
		}

		public void run() {
			testForDirectoryChange(this.dir, this.groupName);
		}
	}
}