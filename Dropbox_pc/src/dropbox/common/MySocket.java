package dropbox.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import dropbox.ui.UiMain;

public class MySocket {
	private static MySocket ms = null;

	public static MySocket getConnector() throws UnknownHostException,
			IOException {
		if (ms == null) {
			ms = new MySocket();
		}
		return ms;
	}

	private Socket s;

	private MySocket() throws UnknownHostException, IOException {
		s = new Socket("10.0.25.186", 8080);
		System.out.println("Socket Connected");
	}

	private void parse(ByteBuffer buffer) throws ParseException, JSONException {
		try {

			int offset = buffer.arrayOffset();
			byte[] buf = buffer.array();

			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(buf, 4, buf.length));
			Message msg = (Message) ois.readObject();

			System.out.println(msg.messageType + "");
			System.out.println(msg.msg);
			
			JSONObject jobj =  new JSONObject(msg.msg);
			String subCategory = (String)jobj.get(Message.SUBCATEGORY_KEY);
			
			//UiTest.loginFrame.setVisible(false);

			switch (msg.messageType) {
			// file request
			case File:
				break;
			// account request
			case Account:
				switch(subCategory){
				case "login":
					ReceiveWork.login(jobj);
				}
				// AccountManager.getManager().receiveMessage(sc, msg);
				break;
			// group request
			case Group:
				switch(subCategory){
				case "create":
					ReceiveWork.createGroup(jobj);
				case "grouplist":
					ReceiveWork.getGroupList(jobj);
				case "memberlist":
					ReceiveWork.getMemberList(jobj);
				}
				break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}

	public void send(Message msg) throws IOException {
		byte[] msgbuf = MessageWrapper.messageToByteArray(msg);

		s.getOutputStream().write(msgbuf);
		s.getOutputStream().flush();
	}

	public void receive() throws IOException, ParseException, JSONException {
		System.out.println("recv start");
		// 메모리를 직접할당해 가비지 컬렉션에 잡히지 않게 처리해
		// 나중에 full gc에 걸리는 시간을 줄인다.
		byte[] msgbuf = new byte[4096];
		while (true) {
			if (s.getInputStream().read(msgbuf, 0, 4096) != -1) {
				System.out.println("read buf : " + msgbuf);
				ByteBuffer buffer = ByteBuffer.wrap(msgbuf);
				parse(buffer);
			}
		}

	}

	private void disconnect(SocketChannel socketChannel) throws IOException {
		System.out.println("user " + socketChannel.getLocalAddress()
				+ " is disconnect");
		socketChannel.close();
	}
}
