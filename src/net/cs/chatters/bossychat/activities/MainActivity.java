package net.cs.chatters.bossychat.activities;

import net.cs.chatters.bossychat.R;
import net.cs.chatters.bossychat.models.Utils;
import net.cs.chatters.bossychat.net.Communicator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends Activity {

    String username = "";
    String password = "";

    public final static int USER_TAKEN = 0;
    public final static int USER_AVAILABLE = 1;
    public final static int EMPTY_FIELD = 2;
    public final static int INTERNAL_PROBLEM = 3;

    private Communicator communicator = new Communicator(this);

    private void startSession() {
        Utils.username = username;
        Utils.password = password;

        //starting UserListActivity
        Intent intent = new Intent(this, UsersListActivity.class);
        startActivity(intent);
        finish();

    }

    private void showAlertDialog(String message) {


        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(message);

        alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alertBuilder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alertDialog = alertBuilder.create();

        alertDialog.show();
    }

    private void trySignIn() {
        EditText user_field = (EditText) findViewById(R.id.user_field);
        String myUsername = user_field.getText().toString();
        EditText pass_field = (EditText) findViewById(R.id.password_field);
        String myPassword = pass_field.getText().toString();

        int loginResult = communicator.tryLogIn(myUsername, myPassword);

        if (loginResult == USER_TAKEN) {
            showAlertDialog("Incorrect username!");
        } else if (loginResult == EMPTY_FIELD) {
            showAlertDialog("Please insert an username");
        } else if (loginResult == INTERNAL_PROBLEM) {
            showAlertDialog("Sorry. The application had some communication errors.");
        } else {
            username = myUsername;
            password = myPassword;
            startSession();
        }
    }

    private void tryRegister() {
        EditText user_field = (EditText) findViewById(R.id.user_field);
        String desiredUsername = user_field.getText().toString();
        EditText pass_field = (EditText) findViewById(R.id.password_field);
        String desiredPassword = pass_field.getText().toString();

        int check_availability_result = communicator.tryRegister(desiredUsername, desiredPassword);

        if (check_availability_result == USER_TAKEN) {
            showAlertDialog("Username taken!");
        } else if (check_availability_result == EMPTY_FIELD) {
            showAlertDialog("Please insert an username");
        } else if (check_availability_result == INTERNAL_PROBLEM) {
            showAlertDialog("Sorry. The application had some communication errors.");
        } else {
            username = desiredUsername;
            password = desiredPassword;
            startSession();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        communicator = new Communicator(this);
        Button sign_in_button = (Button) findViewById(R.id.sing_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    trySignIn();
            }
        });

        Button register_button = (Button) findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryRegister();
            }
        });

    }


}
