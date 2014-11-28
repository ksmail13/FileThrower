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
		ms = MySocket.getConnector();
		loginFrame = new LoginFrame();
		
		ms.receive();
	}
}
