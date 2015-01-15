package dropbox.filemanage;

import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WatchThread extends Thread {
	private String groupName;
	private volatile Set<String> noReactSet = new HashSet<String>();
	private Path dir;
	public synchronized void addIgnoreFile(String path, String name) {
		noReactSet.add(name);
	}
	
	public WatchThread(Path dir, String groupName) {
		this.dir = dir;
		this.groupName = groupName;
	}

	public void run() {
		testForDirectoryChange(dir, this.groupName);
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
					if(noReactSet.contains(event.context().toString())){ 
						noReactSet.remove(event.context().toString());
						continue;
					}
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
}