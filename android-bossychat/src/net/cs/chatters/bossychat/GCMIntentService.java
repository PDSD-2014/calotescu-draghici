package net.cs.chatters.bossychat;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.cs.chatters.bossychat.activities.ChatActivity;
import net.cs.chatters.bossychat.database.MessagesDatabase;
import net.cs.chatters.bossychat.models.Message;
import net.cs.chatters.bossychat.models.Utils;
import net.cs.chatters.bossychat.net.Communicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;


/**
 * Created by kta
 */
public class GCMIntentService extends GCMBaseIntentService{

    private MessagesDatabase db;
    private int msgNo;

    public void setUnreadMessage(String id) {
        Utils.UnreadMessages.put(id, "1");

    }

    private void notifyUInewMessage(Context context){
        Intent intent = new Intent(Utils.ACTION_NEW_MESSAGE);
        context.sendBroadcast(intent);
        Log.i("GCM", "notifying new msg");
    }

    private void addMessagesToDB(String messages, Context context){

        ArrayList<Message> msgList = new ArrayList<Message>();
        Log.i("GCM received json", messages);

        //fetching data
        try {
            JSONObject msgsJSON = new JSONObject(messages);
            JSONArray ResultsArray = msgsJSON.getJSONArray("response");

            for(int i = 0; i<ResultsArray.length(); i++){

                int msgNo = ResultsArray.getJSONObject(i).getInt("msgNo");
                String content = ResultsArray.getJSONObject(i).getString("content");
                String sender = ResultsArray.getJSONObject(i).getString("sender");


                Message msg = new Message(sender,Utils.username,content, msgNo);
                msgList.add(msg);
                Log.i("GCM: added to list",msg.getContent());

                Log.i("Message number:", ""+msgNo+" content "+content + " sender " + sender);
            }
        } catch (JSONException e) {
            Log.i("GCM messages JSON exception", e.getMessage());
        }

        //adding messages to DB
        Collections.sort(msgList,new Comparator<Message>() {
            @Override
            public int compare(Message message, Message message2) {
                return message.compareTo(message2);
            }
        });

        Log.i("GCM", "I have" + msgList.size() + "messages");

        boolean notify = false;
        MessagesDatabase db = new MessagesDatabase(context);
        for (int i = 0; i < msgList.size(); i++){
            Message msg = msgList.get(i);
            Log.i("GCM prev received", ""+Utils.messageNoReceivedFromUser.get(msg.getSender()));
            Log.i("GCM received number", ""+msg.msgNo);


            if(Utils.messageNoReceivedFromUser.get(msg.getSender()) == null)
            	continue;

            //TODO: null pointer aici cand se scrie un msg
            if(msg.msgNo==Utils.messageNoReceivedFromUser.get(msg.getSender())+1){
                msg.setDate(System.currentTimeMillis());
                db.addMessage(msg);
                Utils.messageNoReceivedFromUser.put(msg.getSender(),msg.msgNo);
                notify = true;
                Log.i("GCM messages", ""+msg.msgNo);
                notify = true;
            }

        }

        db.close();

        if(notify){
            notifyUInewMessage(context);
        }

    }

    @Override
    protected void onMessage(Context context, Intent intent) {

        Utils.timeOfLastReceivedMessage = System.currentTimeMillis();

        Log.i("GCM", "receiving messages");
        String sender = intent.getStringExtra("sender");
        String message = intent.getStringExtra("message");

        if(sender.equals("ACK_REQUEST")){
            Log.i("GCM got message to update list status", sender);
            Communicator com = new Communicator(getApplicationContext());
            com.getUsers(Utils.username);
        }
        else{
            Log.i("GCM got message from", sender);
            Log.i("GCM the message ", message);
            setUnreadMessage(sender);
            db = new MessagesDatabase(getApplicationContext());
            db.addMessage(new Message(sender, Utils.username, message,msgNo));
            db.close();
            final String aux = sender;
            displayMessage(aux);
        }

        //make notification on ID
    }

    private void displayMessage(final String sender) {
        ChatActivity.getInstance().listview.post(new Runnable() {
            @Override
            public void run() {
                ChatActivity.getInstance().displayMessagesToScreen(sender);
            }
        });
    }

    @Override
    protected void onError(Context context, String s) {

        Log.i("GCM Intent error:", s);

    }

    @Override
    protected void onRegistered(Context context, String regid) {

        Log.i("GCM Intent registered:", regid);


    }

    @Override
    protected void onUnregistered(Context context, String regid) {

        Log.i("GCM Intent unregistered", regid);

    }
}
