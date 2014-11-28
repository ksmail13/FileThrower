package dropbox.common;

/**
 * Created by micky on 2014. 11. 21..
 */
public class ByteConverter {

    public static int byteArrayToInt(byte... raw) {
        int res =0;
        for (int i = raw.length-1; i >=0 ; i--) {
            res |= (raw[i]&0xFF) << (8*(raw.length-1-i));
        }

        return res;
    }

    public static byte[] intToByteArray(int val) {
        return new byte[] {
                (byte)((val>>>24)&0xFF),
                (byte)((val>>>16)&0xFF),
                (byte)((val>>>8)&0xFF),
                (byte)((val)&0xFF)};
    }
}
