package net.cs.chatters.bossychat.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import net.cs.chatters.bossychat.activities.MainActivity;
// import net.cs.chatters.pinpointchat.database.MessagesDatabase;
import net.cs.chatters.bossychat.models.UserData;
import net.cs.chatters.bossychat.models.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;


public class Communicator {

    Context context;

    public Communicator(Context context){
        this.context=context;
    }

    public ArrayList<UserData> getUsers(String username){

        ArrayList<UserData> userData = new ArrayList<UserData>();
        try{
           userData =  (new UserFetcher().execute(username)).get();
        }

        catch (Exception e){
           Log.i("UserFetcher", e.getMessage());
        }

        return userData;
    }

    private void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException("Field " + name + " is null");
        }
    }


    public void setRegID(){

        checkNotNull(Utils.ServerURL, "GCMServerRegisterURL");
        checkNotNull(Utils.SENDER_ID, "SENDER_ID");

        GCMRegistrar.checkDevice(context);
        GCMRegistrar.checkManifest(context);


        Utils.regid = GCMRegistrar.getRegistrationId(context);


        if(Utils.regid.length()==0){
            GCMRegistrar.register(context, Utils.SENDER_ID);
        }
        Utils.regid = GCMRegistrar.getRegistrationId(context);
        Log.i("REGID",""+Utils.regid);
    }



    public int tryLogIn(String username, String password) {

        if (username.length() == 0)
            return MainActivity.EMPTY_FIELD;
        if (password.length() == 0)
            return MainActivity.EMPTY_FIELD;

        boolean result = false;

        try {
            setRegID();
            if(Utils.regid.equalsIgnoreCase("")){
                return MainActivity.INTERNAL_PROBLEM;
            }
            result = (new UserLogIn().execute(username, password, Utils.regid)).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (result) {
            return MainActivity.USER_AVAILABLE;
        }

        return MainActivity.USER_TAKEN;
    }


    public int tryRegister(String username, String password) {

        if (username.length() == 0)
            return MainActivity.EMPTY_FIELD;

        boolean result = false;

        try {
            setRegID();
            if(Utils.regid.equalsIgnoreCase("")){
                return MainActivity.INTERNAL_PROBLEM;
            }
            result = (new UserSubscriber().execute(username, password, Utils.regid)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (result) {
            return MainActivity.USER_AVAILABLE;
        }

        return MainActivity.USER_TAKEN;
    }

    public void sendMessageToServer(String sender_id, String receiver, String content) {

        if(!Utils.regid.equals(GCMRegistrar.getRegistrationId(context)))
            Log.i("Problem:", "different regids");

        new MessageSender().execute(Utils.USER_ID, receiver, content);
    }

}

class UserFetcher extends AsyncTask<String, Void, ArrayList<UserData>> {

    protected ArrayList<UserData> doInBackground(String... credentials){

        ArrayList<UserData> Users = new ArrayList<UserData>();
        String uri = Utils.ServerURL + "/get_users" + "?username=" + credentials[0];
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri);

        try {

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {

                String jsonString = EntityUtils.toString(entity);
                Log.e("Response UserFetcher:", jsonString);
                JSONObject responseData = new JSONObject(jsonString);
                JSONArray ResultsArray = responseData.getJSONArray("users");

                for (int i = 0; i < ResultsArray.length(); i++) {
                    try {
                        UserData tempUser = new UserData();
                        tempUser.setName(ResultsArray.getString(i));

                        Log.i("UserFetcher Username:",ResultsArray.getString(i));

                        Users.add(tempUser);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return Users;

            }

        } catch (UnsupportedEncodingException e) {

            Log.e("UnsupportedEncodingException", e.getMessage());
            e.printStackTrace();

        } catch (ClientProtocolException e) {

            Log.e("ClientProtocolException", e.getMessage());
            e.printStackTrace();

        } catch (IOException e) {

            Log.e("IOException", e.getMessage());
            e.printStackTrace();

        } catch (JSONException e) {

            Log.e("JSONException", e.getMessage());
            e.printStackTrace();

        }

        return null;

    }

}


class UserSubscriber extends AsyncTask<String, Void, Boolean> {

    protected Boolean doInBackground(String... credentials){


        final String user = credentials[0];
        final String password = credentials[1];
        final String regid = credentials[2];

        String status = "";
        String message = "";
        //format url
        String uri = Utils.ServerURL +"/register_user?username=";
        uri += user;
        uri += "&password="+password;
        uri += "&android_id="+regid;

        Log.i("ServerURL:",uri);

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri);

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            if (entity != null) {

                String jsonString = EntityUtils.toString(entity);
                JSONObject responseData = new JSONObject(jsonString);
                status = responseData.getString("status");
                Log.e("Response UserLogIn:", status);
                Utils.USER_ID = responseData.getString("uid");
            }
            // int exitCode = response.getStatusLine().getStatusCode();

            if (status.equals("ok")) {
                return true;
            } else if (status.equals("fail")) {
                return false;
            }


        } catch (ClientProtocolException e) {
            Log.i("Client:", e.getMessage());
        } catch (IOException e) {
            Log.i("I/O:", e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return false;

    }

}

class UserLogIn extends AsyncTask<String, Void, Boolean> {

    protected Boolean doInBackground(String... credentials) {


        final String user = credentials[0];
        final String password = credentials[1];
        final String regid = credentials[2];

        String status = "";
        String message = "";
        //format url
        String uri = Utils.ServerURL + "/login_user?username=";
        uri += user;
        uri += "&password=" + password;
        uri += "&android_id=" + regid;

        Log.i("ServerURL:", uri);


        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri);

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            if (entity != null) {

                String jsonString = EntityUtils.toString(entity);
                JSONObject responseData = new JSONObject(jsonString);
                status = responseData.getString("status");
                Utils.USER_ID = responseData.getString("uid");
                Log.e("Response UserLogIn:",jsonString);
            }
           // int exitCode = response.getStatusLine().getStatusCode();

            if (status.equals("ok")) {
                return true;
            } else if (status.equals("fail")) {
                return false;
            }


        } catch (ClientProtocolException e) {
            Log.i("Client:", e.getMessage());
        } catch (IOException e) {
            Log.i("I/O:", e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;

    }

}


class MessageSender extends AsyncTask<String, Void, Boolean> {

    protected Boolean doInBackground(String... data){

        final String sender = data[0];
        final String receiver = data[1];
        final String content = data[2];


        //building the request
        String uri = Utils.ServerURL +"/send_message?";
        uri += "sender_id="+sender;
        uri += "&receiver="+receiver;

        try {
            uri = uri + "&message=" + URLEncoder.encode(content, "UTF-8");
            Log.i("Encoding", uri);
        } catch (UnsupportedEncodingException e) {
            Log.i("Encoding error:", e.getMessage());
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri);

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);

            int exitCode = response.getStatusLine().getStatusCode();

            if (exitCode == Utils.OK) {
                return true;
            } else if (exitCode == Utils.Not_Acceptable) {
                return false;
            }


        } catch (ClientProtocolException e) {
            Log.e("Client:", e.getMessage());
        } catch (IOException e) {
            Log.e("I/O:", e.getMessage());
        }


        return false;

    }


}








