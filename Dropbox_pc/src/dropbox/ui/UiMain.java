package dropbox.ui;

import java.io.IOException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.simple.parser.ParseException;

import dropbox.common.MyFtpClient;
import dropbox.common.MySocket;


public class UiMain {
	public static MySocket ms;
	public static LoginFrame loginFrame;

	public static void main(String[] args) throws UnknownHostException, IOException, ParseException, JSONException {
		// TODO Auto-generated method stub
		ms = MySocket.getConnector();
		loginFrame = new LoginFrame();
		
		//MyFtpClient myFtp = new MyFtpClient("10.0.25.186", 8081, "test", "test");
		//System.out.println(myFtp.login("test", "test"));
		//myFtp.send("/users/heejoongkim/monitor", "aaaddd", "asdf");
		
		ms.receive();
	}
}
