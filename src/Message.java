import java.util.ArrayList;

public class Message implements java.io.Serializable
{
    int selfId;
    String type;
    String filename;
    ArrayList<String> filepaths = new ArrayList<String>();
    ArrayList<String> filenames = new ArrayList<String>();
    String s1="", s2="", s3="";
    int timestamp;
    int retKey;

    public Message(int s)
    {
        this.selfId = s;
    }

    public Message(String type, String filename, int selfId, int timestamp)
    {
        this.type = type;
        this.filename = filename;
        this.selfId = selfId;
        this.timestamp = timestamp;
    }

    public Message(String type, ArrayList<String> filenames, ArrayList<String> filepaths, int selfId, int timestamp)
    {
        this.type = type;
        this.filepaths = filepaths;
        this.filenames = filenames;
        this.selfId = selfId;
        this.timestamp = timestamp;
    }

    public Message(String type, String filename, int selfId, int timestamp, String s1, String s2, String s3)
    {
        this.type = type;
        this.filename = filename;
        this.selfId = selfId;
        this.timestamp = timestamp;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;

    }
    public Message(String type, String filename, int selfId, int timestamp, int retKey) //yield
    {
        this.type = type;
        this.filename = filename;
        this.selfId = selfId;
        this.timestamp = timestamp;
        this.retKey = retKey;
    }
}
