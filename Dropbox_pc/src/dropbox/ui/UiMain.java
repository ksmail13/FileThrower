package dropbox.ui;

import dropbox.common.MySocket;
import org.json.JSONException;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.UnknownHostException;


public class UiMain {
	public static MySocket ms;
	public static LoginFrame loginFrame;

	public static void main(String[] args) throws UnknownHostException, IOException, ParseException, JSONException {
		ms = MySocket.getConnector();
		loginFrame = new LoginFrame();
		
		ms.receive();
	}
}
