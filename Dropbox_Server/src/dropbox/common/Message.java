package dropbox.common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.Serializable;

/**
 * Created by micky on 2014. 11. 21..
 */
public class Message implements Serializable{
    public static final String SUBCATEGORY_KEY ="SubCategory";

    public MessageType messageType;
    public String msg;

}
