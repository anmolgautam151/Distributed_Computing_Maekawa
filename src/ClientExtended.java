import java.util.ArrayList;
import java.util.Random;
import java.io.*;
import java.util.*;
import java.net.*;

public class ClientExtended {

    public static ArrayList<String> filenames = new ArrayList<String>();
    public static HashMap<String, String> filetokens = new HashMap<String, String>();
    public static ArrayList<String> filepaths = new ArrayList<String>();
    public static HashMap<String, String> filecs = new HashMap<String, String>();
    public static String[] patharray = {"D1/", "D2/", "D3/"};

    

    String defaultpath =  "";

    public static int selfId;
    int timesClient = 0;
    public int acknow = 0;
    int return_key_flag = 0;
    int tempt = 0;
    int[] delay = {100, 150, 200, 250, 300};
    private final List<Integer> syncList = new ArrayList<>();
    int if_Failed=0;
    String[][] keys_have = {{"","",""},{"","",""},{"","",""}};
    int[] qourum= new int[3];

    public ClientExtended(int selfId) ////Constructor for ClientExtended
    {
        this.selfId = selfId;
        qourum[0] = selfId;
        qourum[1] = (selfId+1)%5;
        qourum[2] = (selfId+2)%5;
    }

    public static int findIndex(int arr[], int t) // Used to find index of a string in an array
    {
        int index = Arrays.binarySearch(arr, t);
        return (index < 0) ? -1 : index;
    }

    public void  clienthandler(Message msgrec)  ////Handles the incoming messages provided by listener
    {
        if(msgrec.type.equals("Reply")) ////Handles the allowed message from server to enter CS
        {
            int temp = acknow + 1;
            //System.out.println("Reply : " + temp);
            //acknow = acknow+1;
            timesClient = Math.max(timesClient,(msgrec.timestamp+1));
            //System.out.println("TimeStamp : "+ timesClient);
            String t = "!";
            tempt = 1;
            ///Checks if the recieved Message has the keys or Failed Message  and stores the decision

            if(msgrec.s1.equals("T"))
            {
                acknow = acknow+1;
                keys_have[0][msgrec.selfId] = "T";
            }
            else
            {
                if(msgrec.s1.equals("W"))
                {
                    
                }
                else if(msgrec.s1.equals("F"))
                {
                    System.out.println("Client Sent Failed");
                    if_Failed = 1;
                }
            }

            if(msgrec.s2.equals("T"))
            {
                acknow = acknow+1;
                keys_have[1][msgrec.selfId] = "T";
            }
            else
            {
                if(msgrec.s2.equals("W"))
                {
                    
                }
                else if(msgrec.s2.equals("F"))
                {
                    System.out.println("Client Sent Failed");
                    if_Failed = 1;
                }
            }

            if(msgrec.s3.equals("T"))
            {
                acknow = acknow+1;
                keys_have[2][msgrec.selfId] = "T";
            }
            else
            {
                if(msgrec.s3.equals("W"))
                {
                    
                }
                else if(msgrec.s3.equals("F"))
                {
                    System.out.println("Client Sent Failed");
                    if_Failed = 1;
                }
            }


            synchronized (syncList)
            {
                if(acknow == 9)
                {
                    syncList.add(1);
                    syncList.notifyAll(); ///Notifies when keys recieved
                }
            }
        }
        if(msgrec.type.equals("Queried")) ///Handles Queried message which also has the name of the available files
        {
            System.out.println("Filenames Recieved");
            filenames = msgrec.filenames;
            filepaths = msgrec.filepaths;
            timesClient = Math.max(timesClient,(msgrec.timestamp+1));
            //System.out.println("TimeStamp : "+ timesClient);
            for(int i=0; i<filenames.size();i++)
            {
                filetokens.put(filenames.get(i), "F");
                filecs.put(filenames.get(i), "F");
            }
            acknow = acknow+1;
            synchronized (syncList)
            {
                if(acknow == 1)
                {
                    syncList.add(1);
                    syncList.notifyAll();
                }
            }
        }
        if(msgrec.type.equals("Enquire")) ////Handles Enquire Message and Yields if recieved failed message from any process
        {
            timesClient = Math.max(timesClient,(msgrec.timestamp+1));
            System.out.println("Client Enquire");
            if(if_Failed == 1)
            {
                acknow = acknow-1;
                int indx = findIndex(qourum, msgrec.retKey);
                keys_have[indx][msgrec.selfId]= "";
                timesClient++;

                Message msg = new Message("Yield", msgrec.filename, selfId, timesClient, msgrec.retKey);
                Socket servertosend = Comm.socketlist.get(msgrec.selfId);
                SendMsg sendingobj = new SendMsg(servertosend);
                try
                {
                    sendingobj.send(msg);
                }
                catch(IOException e)
                {
                    System.out.println(e);
                }
            }
        }

    }
    public void returnkeys(String filename) //Returns keys to servers
    {
        for(int i=0; i<Comm.socketlist.size(); i++)
        {
            timesClient++;
            Message msg = new Message("Release", filename, selfId, timesClient);
            Socket servertosend = Comm.socketlist.get(i);
            SendMsg sendingobj = new SendMsg(servertosend);
            try
            {
                sendingobj.send(msg);
            }
            catch(IOException e)
            {
                System.out.println(e);
            }
        }

    }

    public int generateRand(int min, int max) ////Random Number Generator between a range
    {
        Random rand = new Random();
        return rand.nextInt(max - min) + min;
    }

    public void generateReq() throws IOException     ///Generates Request to server
    {
        int rand = 0;
        for(int i=0; i<5; i++)
        {
            try
            {
                Thread.sleep(((selfId+1)*delay[selfId])); ///Sleep before next request is generated
                //Thread.sleep(((selfId+1)*generateRand(1,300)));
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            if(i == 0)
            {
                System.out.println("Enquiry for file names from server");
                timesClient++;
                //System.out.println("TimeStamp : "+ timesClient);

                ///Sends Message for Enquiry
                Message msg = new Message("Enquiry", "None", selfId, timesClient);

                rand = generateRand(0,Comm.socketlist.size());
                Socket servertosend = Comm.socketlist.get(rand);
                //System.out.println("TimeStamp + TO : "+ timesClient + "  " +  rand);
                SendMsg sendingobj = new SendMsg(servertosend);

                try
                {
                    sendingobj.send(msg);
                }
                catch(IOException e)
                {
                    System.out.println(e);
                }

                ///waits till reply recieved from server
                synchronized (syncList) {
                    while(syncList.isEmpty()) {
                        try {
                            syncList.wait();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                syncList.remove(0);
                acknow = 0;
                System.out.println("File Names Recieved");
            }
            else
            {
                int randaction = generateRand(1,2);
                int randfile = generateRand(0, (filenames.size()));
                rand = generateRand(0,2);
                String tempfile = filenames.get(randfile);
                if(i%2 == 0) //////////READ
                {
                    System.out.println("Reading");
                    boolean flag = false;

                    timesClient++;

                    ////Request keys for Read
                    Message msg = new Message("Read", tempfile, selfId,timesClient);
                    //System.out.println("TimeStamp + TO : "+ timesClient + "  ALL" );
                    for(int j=0; j<Comm.socketlist.size(); j++)
                    {
                        Socket servertosend = Comm.socketlist.get(j);
                        SendMsg sendingobj = new SendMsg(servertosend);

                        try {
                            sendingobj.send(msg);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                    }
                    System.out.println(Comm.socketlist.size());

                    ////Waits till keys are recieved
                    synchronized (syncList) {
                        while(syncList.isEmpty()) {
                            try {
                                syncList.wait();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    syncList.remove(0);
                    if_Failed = 0;
                   
                    acknow = 0;
                    filetokens.put(tempfile, "T");

                    timesClient++;
                    tempt = 0;
                    //System.out.println("Reading now : " + tempfile);
                    //System.out.println("TimeStamp : "+ timesClient);
                    filecs.put(tempfile, "T");
                    read(randfile);
                    filecs.put(tempfile, "F");
                    System.out.println("Done Reading");
                    //System.out.println("Releasing Keys");
                    returnkeys(tempfile);

                }
                else ////////////////////WRITE
                {
                    System.out.println("Writing Now");

                    timesClient++;
                    //System.out.println("TimeStamp : "+ timesClient);
                    ///Requests for keys
                    Message msg = new Message("Write", tempfile, selfId, timesClient);
                    //System.out.println("TimeStamp + TO : "+ timesClient + "  ALL" );
                    for(int j=0; j<Comm.socketlist.size(); j++)
                    {
                        Socket servertosend = Comm.socketlist.get(j);
                        SendMsg sendingobj = new SendMsg(servertosend);

                        try {
                            sendingobj.send(msg);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                    }
                    //System.out.println(Comm.socketlist.size());

                    ////Waits till all keys are recieved
                    synchronized (syncList) {
                        while(syncList.isEmpty()) {
                            try {
                                syncList.wait();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    syncList.remove(0);
                    if_Failed = 0;
                    acknow = 0;
                    filetokens.put(tempfile, "T");

                    ///Critical Section for Write
                    timesClient++;
                    tempt = 0;
                    filecs.put(tempfile, "T");
                    write(randfile);
                    filecs.put(tempfile, "F");
                    System.out.println("Finished Write");
                    returnkeys(tempfile);
                }
            }
        }

    }

    public void write(int index) throws IOException  ////Writes in the files
    {
        for(int x=0; x<patharray.length; x++)
        {
            FileWriter ofile = new FileWriter(defaultpath+patharray[x]+filenames.get(index), true);
            String towrite = "< " + String.valueOf(selfId) +", "+ String.valueOf(timesClient) + "> \n";
            ofile.write(towrite);
            ofile.close();
        }

    }
    public void read(int index) throws IOException   ////Reads from a file
    {
        String lastLine = "";
        String line;
        BufferedReader input = new BufferedReader(new FileReader(defaultpath+patharray[index]+filenames.get(index)));

        while ((line = input.readLine()) != null) {
            lastLine = line;
        }
        System.out.println("Line" + lastLine);
        input.close();

    }
}