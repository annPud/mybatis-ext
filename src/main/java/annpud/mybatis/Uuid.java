package annpud.mybatis;

import java.net.InetAddress;
import java.util.UUID;

/**
 * 生成uuid的类
 */
public class Uuid {

    private static final int IP;
    private static short counter = (short) 0;
    private static final int JVM = (int) (System.currentTimeMillis() >>> 8);

    static {
        int ipadd;
        try {
            ipadd = iptoInt(InetAddress.getLocalHost().getAddress());
        } catch (Exception e) {
            ipadd = 0;
        }
        IP = ipadd;
    }

    /**
     * 随机uuid
     *
     * @return 32位uuid
     */
    public static String random() {
        return randomSplit("");
    }

    /**
     * 顺序uuid
     *
     * @return 32位uuid
     */
    public static String order() {
        return orderSplit("");
    }

    /**
     * 随机uuid带分隔符
     *
     * @param character 分隔符
     * @return 36位带分隔符的uuid
     */
    public static String randomSplit(String character) {
        String value = UUID.randomUUID().toString();
        return value.substring(0, 8) + character +
            value.substring(9, 13) + character + value.substring(14, 18) +
            character + value.substring(19, 23) + character +
            value.substring(24);
    }

    /**
     * 顺序uuid带分隔符
     *
     * @param character 分隔符
     * @return 36位带分隔符的uuid
     */
    public static String orderSplit(String character) {
        return format(getIP()) + character + format(getJVM()) +
            character + format(getHiTime()) + character +
            format(getLoTime()) + character + format(getCount());
    }



    private static int iptoInt(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
        }
        return result;
    }

    static int getJVM() {
        return JVM;
    }

    static short getCount() {
        if (counter < 0) {
            counter = 0;
        }
        return counter++;
    }

    static int getIP() {
        return IP;
    }

    static short getHiTime() {
        return (short) (System.currentTimeMillis() >>> 32);
    }

    static int getLoTime() {
        return (int) System.currentTimeMillis();
    }

    static String format(int intval) {
        String formatted = Integer.toHexString(intval);
        StringBuilder buf = new StringBuilder("00000000");
        buf.replace(8 - formatted.length(), 8, formatted);
        return buf.toString();
    }

    static String format(short shortval) {
        String formatted = Integer.toHexString(shortval);
        StringBuilder buf = new StringBuilder("0000");
        buf.replace(4 - formatted.length(), 4, formatted);
        return buf.toString();
    }

}
