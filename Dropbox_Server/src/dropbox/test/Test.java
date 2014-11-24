package dropbox.test;

import dropbox.common.ByteConverter;
import dropbox.common.Message;
import dropbox.common.MessageType;
import dropbox.common.MessageWrapper;
import dropbox.server.Util.DatabaseConnector;
import dropbox.server.Util.Logger;
import org.json.simple.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import sun.rmi.runtime.Log;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Map;

import static java.lang.Thread.*;

/**
 * Created by micky on 2014. 11. 22..
 */
public class Test {
    public static void main(String[] args) {

//        DatabaseConnector connector = DatabaseConnector.getConnector();
//
//        try {
//            connector.select("select * from infobase as i left join accountinfo as a on i.infoid = a.accountid");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }



        byte[] len = ByteConverter.intToByteArray(242);
        System.out.println(ByteConverter.byteArrayToInt(len[3],len[2], len[1], len[0]));

        try {
            Socket socket = new Socket("localhost", 8080);
            JSONObject obj = new JSONObject();


            Message msg = new Message();

            msg.messageType = MessageType.Account;
            obj.put(Message.SUBCATEGORY_KEY, "login");
            obj.put("id", "aa");
            obj.put("password", "a");

            msg.msg = obj.toJSONString();

            byte[] byteMsg = MessageWrapper.messageToByteArray(msg);
            socket.getOutputStream().write(byteMsg);

            obj.clear();

            socket.getInputStream().read(byteMsg);
            //Message rmsg = MessageWrapper.byteArrayToMessage(byteMsg);

            //Logger.logging(rmsg.messageType+"");
            //Logger.logging(rmsg.msg);

            obj.put(Message.SUBCATEGORY_KEY, "grouplist");

            msg.messageType = MessageType.Group;
            msg.msg = obj.toJSONString();
            byteMsg = MessageWrapper.messageToByteArray(msg);
            socket.getOutputStream().write(byteMsg);

            sleep(2000L);
            socket.getInputStream().read(byteMsg);
            Message rmsg = MessageWrapper.byteArrayToMessage(byteMsg);

            Logger.logging(rmsg.messageType+"");
            Logger.logging(rmsg.msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * MapDB uses custom serialization which stores class metadata at single place.
     * Thanks to it is 10x more efficient than standard Java serialization.
     *
     * Using custom values in MapDB has three conditions:
     *
     *   1)  classes should be immutable. There is instance cache, background serialization etc
     *         Modifing your classes after they were inserted into MapDB may leed to unexpected things.
     *
     *   2) You should implement `Serializable` marker interface. MapDB tries to stay compatible
     *         with standard Java serialization.
     *
     *   3) Even your values should implement equalsTo method for CAS (compare-and-swap) operations.
     *

    public static class Person implements Serializable{
        final String name;
        final String city;

        public Person(String n, String c){
            super();
            this.name = n;
            this.city = c;
        }

        public String getName() {
            return name;
        }

        public String getCity() {
            return city;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Person person = (Person) o;

            if (city != null ? !city.equals(person.city) : person.city != null) return false;
            if (name != null ? !name.equals(person.name) : person.name != null) return false;

            return true;
        }

    }

    public static void main(String[] args) throws IOException {

        // Open db in temp directory
        File f = File.createTempFile("mapdb","temp");
        DB db = DBMaker.newMemoryDB().transactionDisable().closeOnJvmShutdown().make();;

        // Open or create table
        Map<String,Person> dbMap = db.getTreeMap("personAndCity");

        // Add data
        Person bilbo = new Person("Bilbo","The Shire");
        Person sauron = new Person("Sauron","Mordor");
        Person radagast = new Person("Radagast","Crazy Farm");

        dbMap.put("west",bilbo);
        dbMap.put("south",sauron);
        dbMap.put("mid",radagast);

        // Commit and close
        db.commit();
        db.close();


        //
        // Second option for using cystom values is to use your own serializer.
        // This usually leads to better performance as MapDB does not have to
        // analyze the class structure.
        //

        class CustomSerializer extends Serializer<Person> implements Serializable {

            @Override
            public void serialize(DataOutput out, Person value) throws IOException {
                out.writeUTF(value.getName());
                out.writeUTF(value.getCity());
            }

            @Override
            public Person deserialize(DataInput in, int available) throws IOException {
                return new Person(in.readUTF(), in.readUTF());
            }

            @Override
            public int fixedSize() {
                return -1;
            }

        }

        Serializer<Person> serializer = new CustomSerializer();

        DB db2 = DBMaker.newTempFileDB().make();

        Map<String,Person> map2 = db2.createHashMap("map").valueSerializer(serializer).make();

        map2.put("North", new Person("Yet another dwarf","Somewhere"));

        db2.commit();
        db2.close();


    }
        */
}
