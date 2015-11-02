package fi.oulu.tol.vgs4msc;

/**
 * Created by dengcanrong on 15/10/12.
 */
public class User {
    private static String token;
    private static String name;
    private static String passwd;
    private static String sip_address;

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        User.name = name;
    }

    public static String getPasswd() {
        return passwd;
    }

    public static void setPasswd(String passwd) {
        User.passwd = passwd;
    }

    public static String getSip_address() {
        return sip_address;
    }

    public static void setSip_address(String sip_address) {
        User.sip_address = sip_address;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        User.token = token;
    }

}
