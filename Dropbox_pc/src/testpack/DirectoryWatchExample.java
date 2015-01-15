package testpack;

import java.nio.file.*;
import java.util.List;

class WatchThread extends Thread{
	private Path dir;
	public WatchThread(Path dir){
		this.dir = dir;
	}
	public void run(){
		(new DirectoryWatchExample()).testForDirectoryChange(this.dir);
	}
}

public class DirectoryWatchExample {
	public void testForDirectoryChange(Path myDir) {
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
						System.out.println("Created: "
								+ myDir+"/" + event.context().toString());
					}
					if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
						System.out.println("Delete: "
								+ myDir+"/" + event.context().toString());
					}
					if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
						System.out.println("Modify: "
								+ myDir+"/" + event.context().toString());
					}
				}

			} catch (Exception e) {
				System.out.println("Error: " + e.toString());
			}
		}
	}

//	public static void main(String[] args) {
//		Path myDir = Paths.get("/users/heejoongkim/monitor");
//		// define a folder root
//		System.out.println("Monitor Start");
//		File dir = new File("/users/heejoongkim/monitor");
//		DirectoryWatchExample watchExample01 = new DirectoryWatchExample();
//		//watchExample01.testForDirectoryChange(myDir);
//		File[] files = dir.listFiles();
//		int folderCount = files.length;
//		WatchThread[] watchThread = new WatchThread[folderCount];
//		
//		int num=0;
//		for (File file: files){
//		     if (file.listFiles() != null){
//		          //It's a subdirectory
//		    	 System.out.println(file);
//		    	 watchThread[num] = new WatchThread(file.toPath());
//		    	 watchThread[num].start();
//		    	 num++;
//		     }
//		}
//	}
}