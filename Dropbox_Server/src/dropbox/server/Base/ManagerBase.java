package dropbox.server.Base;

import dropbox.common.Message;
import dropbox.common.MessageType;
import dropbox.common.MessageWrapper;
import dropbox.server.Util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by micky on 2014. 11. 22..
 */
public abstract class ManagerBase {

    public static ManagerBase getInstance() {
        return null;
    }



    public void receiveMessage(SocketChannel sc, Message msg) throws IOException {
        JSONObject resultJson = null;
        Message message = null;
        try {
            JSONObject parsedObject = messageToJSONObject(msg);
            resultJson = messageHandling(sc, parsedObject);

        } catch (ClassCastException cce) {
            Logger.errorLogging(cce);
        } finally {

        }

        if(resultJson != null) {
            message = new Message();
            message.messageType = MessageType.Account;
            message.msg = resultJson.toJSONString();

            sc.write(ByteBuffer.wrap(MessageWrapper.messageToByteArray(message)));
        }
    }

    public abstract JSONObject messageHandling(SocketChannel sc, JSONObject parsedObject);

    public JSONObject messageToJSONObject(Message msg) {
        JSONObject obj = null;
        JSONParser parser = new JSONParser();
        try {
            obj = (JSONObject)parser.parse(msg.msg);
        } catch (ParseException e) {
            Logger.errorLogging(e);
        }

        return obj;
    }

    protected ManagerBase() { }
}
