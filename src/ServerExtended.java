import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class ServerExtended {

    Comm objcomm;
    int timestamp = 0;
    int reqsgenerated;
    public static String[] filenames = {"F1.txt", "F2.txt"};
    public static int[][] keylentto = {{9,9,9,9,9},{9,9,9,9,9}};
    public static String[][] filekeys = {
            {"T", "T", "T", "T", "T"}, {"T", "T", "T", "T", "T"}
    };
    public static int[][] timestamp_keylentto = {{0,0,0,0,0},{0,0,0,0,0}};
    public static int[] filelentto = {99,99,99,99};
    public int selfId;
    public static HashMap<String, ArrayList<Integer>> waiting_queueF = new HashMap<String, ArrayList<Integer>>();
    public static HashMap<Integer, ArrayList<Integer>> waiting_queueC = new HashMap<Integer, ArrayList<Integer>>();
    public static HashMap<Integer, Integer> pid_tmestamp = new HashMap<Integer, Integer>();
    String defaultpath = "";
    String[] machinedir = {"D1", "D2", "D3"};


    public ServerExtended(Comm comobj, int selfId) // Constructor for ServerExtended
    {
        this.objcomm = comobj;
        this.timestamp = 0;
        this.reqsgenerated = 0;
        this.selfId = selfId;
    }

    public static int findIndex(String arr[], String t) // Used to find index of a string in an array
    {
        int index = Arrays.binarySearch(arr, t);
        return (index < 0) ? -1 : index;
    }

    public void serverreciever(Message msgrec) //Recieves message from listener and handles it
    {
        reptoReq(msgrec);
    }

    public void reptoReq(Message msgrec) //Handles Enquiry and Read/Write requests
    {
        if(msgrec.type.equals("Enquiry")) ///Handles enquiry request from clients
        {
            System.out.println("Query By Client");
            timestamp = Math.max(timestamp,(msgrec.timestamp+1));
            //System.out.println("Timestamp : " + timestamp);
            String[] pathnames;
            ArrayList<String> retfilepaths = new ArrayList<String>();
            ArrayList<String> retfilenames = new ArrayList<String>();
          
            String temppath = defaultpath + machinedir[selfId];
            File f = new File(temppath);
            pathnames = f.list();

            ///makes a list of files that are available
            for (String pathname : pathnames)
            {
                retfilepaths.add("./D1" + pathname);
                retfilenames.add(pathname);
                //System.out.println("FILE : " + pathname);
            }

            ///Sends the list of files  back to the client
            timestamp++;
            //System.out.println("Timestamp : " + timestamp);
            Message msg = new Message("Queried", retfilenames, retfilepaths, selfId, timestamp);
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

        if(msgrec.type.equals("Release")) ////////handles the release Message from Client
        {
            System.out.println("RELEASE"");
            timestamp = Math.max(timestamp,(msgrec.timestamp+1));

            int index = findIndex(filenames, msgrec.filename);
            filekeys[index][msgrec.selfId%5]= "T";
            filekeys[index][(msgrec.selfId+1)%5]= "T";
            filekeys[index][(msgrec.selfId+2)%5]= "T";

            keylentto[index][msgrec.selfId%5] = 9;
            keylentto[index][(msgrec.selfId+1)%5] = 9;
            keylentto[index][(msgrec.selfId+2)%5] = 9;

            int index_to_remove=0;

            ///Checks if there is any other processes request for the same file

            if(!waiting_queueF.isEmpty() && waiting_queueF.containsKey(msgrec.filename))
            {
                ArrayList<Integer> temp = waiting_queueF.get(msgrec.filename);
                if(temp.size() > 0)
                {
                    int pid_to_send = 0;
                    int min_time = 1000;
                    for (int i = 0; i < temp.size(); i++)
                    {
                        if (pid_tmestamp.get(temp.get(i)) < min_time) {
                            min_time = pid_tmestamp.get(temp.get(i));
                            pid_to_send = temp.get(i);
                            index_to_remove = i;
                        } else if (pid_tmestamp.get(temp.get(i)) == min_time) {
                            if (temp.get(i) < pid_to_send) {
                                min_time = pid_tmestamp.get(temp.get(i));
                                pid_to_send = temp.get(i);
                                index_to_remove = i;
                            }
                        }
                    }

                    temp.remove(index_to_remove);
                    waiting_queueF.put(msgrec.filename, temp);
                    pid_tmestamp.remove(pid_to_send);


                    String keys[] = {"Null", "Null", "Null"};
                    ArrayList<Integer> temp2 = waiting_queueC.get(pid_to_send);
                    //System.out.println("Temp2 Size : "+ temp2.size());


                    for (int i=0; i<temp2.size(); i++)
                    {
                        //keys[pid_to_send-temp2.get(i)] = "T";
                        //System.out.println("Temp2 : "+temp2.get(i));
                        if(temp2.get(i) == pid_to_send)
                        {
                            keys[0] = "T";
                        }
                        if(temp2.get(i) == ((pid_to_send+1)%5))
                        {
                            keys[1] = "T";
                        }
                        if(temp2.get(i) == ((pid_to_send+2)%5))
                        {
                            keys[2] = "T";
                        }
                    }

                    temp2.clear();
                    waiting_queueC.put(pid_to_send,temp2);

                    filekeys[index][pid_to_send%5]= "F";
                    filekeys[index][(pid_to_send+1)%5]= "F";
                    filekeys[index][(pid_to_send+2)%5]= "F";

                    keylentto[index][pid_to_send%5] = pid_to_send;
                    keylentto[index][(pid_to_send+1)%5] = pid_to_send;
                    keylentto[index][(pid_to_send+2)%5] = pid_to_send;

                    timestamp_keylentto[index][pid_to_send%5] = msgrec.timestamp;
                    timestamp_keylentto[index][(pid_to_send+1)%5] = msgrec.timestamp;
                    timestamp_keylentto[index][(pid_to_send+2)%5] = msgrec.timestamp;

                    //System.out.println("Sending Keys from inside reply to : " + pid_to_send);
					System.out.println("Send Acknowledgement form other clients");

                    timestamp++;

                    ////Sends the appropriate Reply to the the process with highest priority in queue
                    Message msg = new Message("Reply", filenames[index], selfId, timestamp, keys[0], keys[1], keys[2]);
                    Socket servertosend = Comm.socketlist.get(pid_to_send);
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

        if(msgrec.type.equals("Yield"))/////Handles the Message when Client yields a key
        {
            System.out.println("YIELDED");
            timestamp = Math.max(timestamp,(msgrec.timestamp+1));
            int index = findIndex(filenames, msgrec.filename);
            filekeys[index][msgrec.retKey]= "T";
            keylentto[index][msgrec.retKey] = 9;

            ArrayList<Integer> temp = waiting_queueF.get(msgrec.filename);
            int index_to_remove=0;

            int pid_to_send = 0;
            int min_time = 1000;

            ///Finds the process with the highest priority in queue for the key
            for (int i = 0; i < temp.size(); i++)
            {
                if (pid_tmestamp.get(temp.get(i)) < min_time) {
                    min_time = pid_tmestamp.get(temp.get(i));
                    pid_to_send = temp.get(i);
                    index_to_remove = i;
                } else if (pid_tmestamp.get(temp.get(i)) == min_time) {
                    if (temp.get(i) < pid_to_send) {
                        min_time = pid_tmestamp.get(temp.get(i));
                        pid_to_send = temp.get(i);
                        index_to_remove = i;
                    }
                }
            }

            ////Puts the request which yielded the key in the queue

            if(waiting_queueF.containsKey(msgrec.filename))
            {
                ArrayList<Integer> temp2= waiting_queueF.get(msgrec.filename);
                if(!temp.contains(msgrec.selfId))
                {
                    temp2.add(msgrec.selfId);
                    waiting_queueF.put(msgrec.filename, temp2);
                    pid_tmestamp.put(msgrec.selfId, msgrec.timestamp);
                }
            }
            else
            {
                ArrayList<Integer> temp2 = new ArrayList<Integer>();
                temp2.add(msgrec.selfId);
                waiting_queueF.put(msgrec.filename, temp2);
                pid_tmestamp.put(msgrec.selfId, msgrec.timestamp);
            }
            if(waiting_queueC.containsKey(msgrec.selfId))
            {
                ArrayList<Integer> temp2= waiting_queueC.get(msgrec.selfId);
                temp2.add(msgrec.retKey);
                waiting_queueC.put(msgrec.selfId, temp2);
                pid_tmestamp.put(msgrec.selfId, msgrec.timestamp);
            }
            else
            {
                ArrayList<Integer> temp2 = new ArrayList<Integer>();
                temp2.add(msgrec.retKey);
                waiting_queueC.put(msgrec.selfId, temp2);
                pid_tmestamp.put(msgrec.selfId, msgrec.timestamp);
            }

            int vremove = 0;
            ArrayList<Integer> temp2 = waiting_queueC.get(pid_to_send);

            if(temp2.size() == 1)
            {
                temp.remove(index_to_remove);
                waiting_queueF.put(msgrec.filename, temp);
                pid_tmestamp.remove(pid_to_send);
            }

            timestamp++;
            String keys[] = {"Null", "Null", "Null"};
            for (int i=0; i<temp2.size(); i++)
            {
                if(temp2.get(i) == msgrec.retKey)
                {
                    vremove = i;


                    //keys[pid_to_send-temp2.get(i)] = "T";
                    if(temp2.get(i) == pid_to_send)
                    {
                        keys[0] = "T";
                    }
                    if(temp2.get(i) == ((pid_to_send+1)%5))
                    {
                        keys[1] = "T";
                    }
                    if(temp2.get(i) == ((pid_to_send+2)%5))
                    {
                        keys[2] = "T";
                    }

                    ////Sends the key to the process which has highest priority in queue
                    Message msg = new Message("Reply", filenames[index], selfId, timestamp, keys[0], keys[1], keys[2]);
                    Socket servertosend = Comm.socketlist.get(pid_to_send);
                    SendMsg sendingobj = new SendMsg(servertosend);
                    try
                    {
                        sendingobj.send(msg);
                    }
                    catch(IOException e)
                    {
                        System.out.println(e);
                    }
                    break;
                }
            }
            temp2.remove(vremove);
            waiting_queueC.put(pid_to_send, temp2);

        }


        if(msgrec.type.equals("Read") || msgrec.type.equals("Write")) /// Handles the Read/Write Requests
        {
            System.out.println("REQUEST");
            int index = findIndex(filenames, msgrec.filename);
            timestamp = Math.max(timestamp,(msgrec.timestamp+1));
            //System.out.println("Timestamp : " + timestamp);
            if(filekeys[index][msgrec.selfId%5].equals("T") && filekeys[index][(msgrec.selfId+1)%5].equals("T") && filekeys[index][(msgrec.selfId+2)%5].equals("T")) ////If the key is available then provide the key
            {
                

                ///updates the keys to keep track which process has the keys
                filelentto[index] = msgrec.selfId;

                keylentto[index][msgrec.selfId%5] = msgrec.selfId;
                keylentto[index][(msgrec.selfId+1)%5] = msgrec.selfId;
                keylentto[index][(msgrec.selfId+2)%5] = msgrec.selfId;

                filekeys[index][msgrec.selfId%5]= "F";
                filekeys[index][(msgrec.selfId+1)%5]= "F";
                filekeys[index][(msgrec.selfId+2)%5]= "F";

                timestamp_keylentto[index][msgrec.selfId%5] = msgrec.timestamp;
                timestamp_keylentto[index][(msgrec.selfId+1)%5] = msgrec.timestamp;
                timestamp_keylentto[index][(msgrec.selfId+2)%5] = msgrec.timestamp;

                ///Sends the key to the process
                timestamp++;
                //System.out.println("Timestamp : " + timestamp);
                Message msg = new Message("Reply", "None", selfId, timestamp, "T", "T", "T");
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
            else        ////handles the request when all keys are not available
            {
                
                String key1 = "F";
                String key2 = "F";
                String key3 = "F";
                String[] keys = {"F","F","F"};
                if(filekeys[index][msgrec.selfId%5].equals("T"))
                {
                    key1 = "T";
                    keys[0] = "T";
                    filekeys[index][msgrec.selfId%5]= "F";
                    keylentto[index][msgrec.selfId%5] = msgrec.selfId;
                    timestamp_keylentto[index][msgrec.selfId%5] = msgrec.timestamp;
                }
                if(filekeys[index][msgrec.selfId%5].equals("F"))
                {
                    //if(msgrec.selfId < keylentto[index][msgrec.selfId%5]) ////priority check
                    if(msgrec.timestamp < timestamp_keylentto[index][msgrec.selfId%5]) ///Checks the priority for Sending Enquire Message
                    {
                        key1 = "W";
                        keys[0] = "W";
                    }
                }
                if(filekeys[index][(msgrec.selfId+1)%5].equals("T"))
                {
                    key2 = "T";
                    keys[1] = "T";
                    filekeys[index][(msgrec.selfId+1)%5]= "F";
                    keylentto[index][(msgrec.selfId+1)%5] = msgrec.selfId;
                    timestamp_keylentto[index][(msgrec.selfId+1)%5] = msgrec.timestamp;
                }
                if(filekeys[index][(msgrec.selfId+1)%5].equals("F"))
                {
                    //if(msgrec.selfId < keylentto[index][(msgrec.selfId+1)%5])
                    if(msgrec.timestamp < timestamp_keylentto[index][(msgrec.selfId+1)%5])
                    {
                        key2 = "W";
                        keys[1] = "W";
                    }
                }
                if(filekeys[index][(msgrec.selfId+2)%5].equals("T"))
                {
                    key3 = "T";
                    keys[2] = "T";
                    filekeys[index][(msgrec.selfId+2)%5]= "F";
                    keylentto[index][(msgrec.selfId+2)%5] = msgrec.selfId;
                    timestamp_keylentto[index][(msgrec.selfId+2)%5] = msgrec.timestamp;
                }
                if(filekeys[index][(msgrec.selfId+1)%5].equals("F"))
                {
                    //if(msgrec.selfId < keylentto[index][(msgrec.selfId+2)%5])
                    if(msgrec.timestamp < timestamp_keylentto[index][(msgrec.selfId+2)%5])
                    {
                        key3 = "W";
                        keys[2] = "W";
                    }
                }
                //System.out.println("Keys : "+keys[0] + " " + keys[1] + " " + keys[2]);
                //////Sending Initial Reply to the requesting client
                timestamp++;
                Message msg = new Message("Reply", filenames[index], selfId, timestamp, key1, key2, key3);
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

                /////Add to queue & Send Enquiry if any key has W(Wait)

                if(waiting_queueF.containsKey(msgrec.filename))
                {
                    ArrayList<Integer> temp= waiting_queueF.get(msgrec.filename);
                    temp.add(msgrec.selfId);
                    waiting_queueF.put(msgrec.filename, temp);
                    pid_tmestamp.put(msgrec.selfId, msgrec.timestamp);
                }
                else
                {
                    ArrayList<Integer> temp = new ArrayList<Integer>();
                    temp.add(msgrec.selfId);
                    waiting_queueF.put(msgrec.filename, temp);
                    pid_tmestamp.put(msgrec.selfId, msgrec.timestamp);
                }

                timestamp++;
                for (int i=0; i<keys.length; i++) ////Adds the keys required in queue and sends Enquire if Required based on priority
                {
                    if(!keys[i].equals("T"))
                    {
                        if(waiting_queueC.containsKey(msgrec.selfId))
                        {
                            ArrayList<Integer> temp= waiting_queueC.get(msgrec.selfId);
                            temp.add((msgrec.selfId+i)%5);
                            waiting_queueC.put(msgrec.selfId, temp);
                            //pid_tmestamp.put(msgrec.selfId, msgrec.timestamp);
                        }
                        else
                        {
                            ArrayList<Integer> temp = new ArrayList<Integer>();
                            temp.add((msgrec.selfId+i)%5);
                            waiting_queueC.put(msgrec.selfId, temp);
                            //pid_tmestamp.put(msgrec.selfId, msgrec.timestamp);
                        }

                        if(keys[i].equals("W")) ////Sends Enquire if the priority of the process is higher
                        {
                            
                            Message msgenq = new Message("Enquire", filenames[index], selfId, timestamp, ((msgrec.selfId+i)%5));
                            servertosend = Comm.socketlist.get(keylentto[index][(msgrec.selfId+i)%5]); ////check before running
                            sendingobj = new SendMsg(servertosend);
                            try
                            {
                                sendingobj.send(msgenq);
                            }
                            catch(IOException e)
                            {
                                System.out.println(e);
                            }
                        }
                    }
                }

            }
        }
    }
}