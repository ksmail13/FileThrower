package testpack;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
 
 
/**
 * @author Steven J.S Min
 *
 */
public class WatchingPaths {
 
       /**
        * @param args
        * @throws IOException
        */
       public static void main(String[] args) throws IOException {
 
              FileSystem fs = FileSystems.getDefault();
 
              Path watchPath = fs.getPath("/users/heejoongkim/monitor");
              WatchService watchService = fs.newWatchService();


              watchPath.register(watchService,
                           StandardWatchEventKinds.ENTRY_CREATE,
                           StandardWatchEventKinds.ENTRY_MODIFY,
                           StandardWatchEventKinds.ENTRY_DELETE);
 
              while (true) {
                     try {
                          
                           WatchKey changeKey = watchService.take();
                          
                           List<WatchEvent<?>> watchEvents = changeKey.pollEvents();
 
                           for (WatchEvent<?> watchEvent : watchEvents) {
                                  // Ours are all Path type events:
                                  WatchEvent<Path> pathEvent = (WatchEvent<Path>) watchEvent;
 
                                  Path path = pathEvent.context();
                                  WatchEvent.Kind<Path> eventKind = pathEvent.kind();
 
                                  System.out.println(eventKind + " root: /users/heejoongkim/monitor/"+path);
                            }
 
                           changeKey.reset(); // Important!
 
                     } catch (InterruptedException e) {
                           e.printStackTrace();
                     }
 
              }
       }
 
}