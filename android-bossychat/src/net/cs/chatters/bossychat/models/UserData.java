package net.cs.chatters.bossychat.models;

import java.io.Serializable;

public class UserData implements Serializable{

	private static final long serialVersionUID = 1L;
	private String name;
    private String password;
    private int status;
    private String regid;

    public String getRegid(){
        return regid;
    }

    public void setRegid(String regid){
        this.regid = regid;
    }

    public void setStatus(int status){
        this.status=status;
    }

    public UserData(String username){
        name = username;
    }

    public UserData(){
        ;
    }

    public String getName() {
        return name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public void setPass(String pass) {
        this.password = pass;
    }
}
