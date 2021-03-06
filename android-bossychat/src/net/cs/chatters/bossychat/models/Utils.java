package net.cs.chatters.bossychat.models;

import java.util.ArrayList;
import java.util.HashMap;


public class Utils {

    public final static String USERNAME = "pinpointchat.USERNAME";
    public final static String ACTION_NEW_MESSAGE = "pinpointchat.NEW_MESSAGE";
    public static final String ServerURL = "http://192.168.1.157/PDSD";
    public final static String SENDER_ID =  "378233293789";
    public final static String LOGGED_IN = "pinpointchat.LOGGED_IN";
    public final static String INTERLOCUTOR = "pinpointchat.INTERLOCUTOR";
    public final static String SharedPrefs = "pinpointchaty.SHARED_PREFS";
    public static final int OK = 200;
    public static final int numberOfDefinedColors = 5;
    public static final int userPositionUpdaterDELAY = 1;//1000 * 60 * 5;// 1000 * 60 * numarul de minute // VictorP
    public static final int Not_Acceptable = 406;
    public static final long refreshUsersList = 1000 * 20; // 1000 * 60 *  numarul de minute
    public static long timeOfLastReceivedMessage = 0;

    public static String username = "";
    public static String password = "";
    public static String USER_ID =  "";
    public static ArrayList messages = new ArrayList();
    public static double UserLat = 0;
    public static double UserLng = 0;

    //used to show a notifier on the user list that a message has been received
    public static HashMap<String, String> UnreadMessages;

    // String = User; Integer = message number sent to User in the current session
    public static HashMap<String, Integer> messageNoSentToUser;
    public static HashMap<String, Integer> messageNoReceivedFromUser;
    public static String regid="";
    public static  long appStartTime;

}
