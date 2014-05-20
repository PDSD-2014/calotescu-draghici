package net.cs.chatters.bossychat.models;

public class Message implements Comparable{

    private String content;
    private String sender;
    private String receiver;
    private long date;          //messages are fetched by date
    private String hourMin;     //it is displayed by the username
    public int msgNo;

    public Message() {
    }

    public Message(String sender, String receiver, String content, int msgNo) {

        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.msgNo = msgNo;

        date = System.currentTimeMillis();
    }

    public void setHourMin(String hourMin){
        this.hourMin = hourMin;
    }

    public String getHourMin(){
        return this.hourMin;
    }

    public void setContent(String _content) {
        content = _content;
    }

    public void setSender(String _sender) {
        sender = _sender;
    }

    public void setReceiver(String _receiver) {
        receiver = _receiver;
    }

    public void setDate(long _date) {
        date = _date;
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public long getDate() {
        return date;
    }

    @Override
    public int compareTo(Object o) {

        return this.msgNo - ((Message)o).msgNo;
    }
}
