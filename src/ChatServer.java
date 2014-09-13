import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import org.apache.commons.lang3.StringUtils;

class ChatServerThread implements Runnable {
    
    /* The client socket and IO we are going to handle in this thread */
    protected Socket         socket;
    protected PrintWriter    out;
    protected BufferedReader in;

    public static ArrayList<ChatServerThread> clientlist = new ArrayList<ChatServerThread>();
    private static int nextid=1;

    public static ArrayList<room> rooms =  new ArrayList<room>();

    public String id;
    
    public ChatServerThread(Socket socket) {
        /* Assign local variable */
        this.socket = socket;
        this.id = "" + nextid++;
        
        /* Create the I/O variables */
        try {

            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in  = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            
            /* Some debug */
            for (int x=0; x<clientlist.size(); x++) {
                if (this != clientlist.get(x)){ clientlist.get(x).out.println("Client " + this.id + " connected!"); }
            }
            
            /* Say hi to the client */
            this.out.println("Welcome to the Chat server client " + this.id +"!");
            this.out.println("You are in the the lobby room");
            this.out.print("You are in the server with clients: ");
            this.addLobby(this.id);
            for(int x=0; x<clientlist.size(); x++){
                if (x==(clientlist.size()-1)) {
                    this.out.print(clientlist.get(x).id);
                }
                else {
                    this.out.print(clientlist.get(x).id + ", ");
                }
            }
            this.out.println("");
            
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            System.exit(1);
        }

    }

    public static void addClient(ChatServerThread a){
        clientlist.add(a);
    }

    public static void addRoom(room r){
        rooms.add(r);
    }

    public String changeID(String newID){
        char[] legal = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q' , 'R', 'S', 'T', 'U' , 'V', 'W', 'X', 'Y', 'Z','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q' , 'r', 's', 't', 'u' , 'v', 'w', 'x', 'y', 'z', '!', '.', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
        for(int x=0; x<clientlist.size(); x++){
            if(newID.equalsIgnoreCase(clientlist.get(x).id)){ return "That id is already taken"; }
        }
        for(int x=0; x< newID.length() ; x++){
            boolean exists=false;
            for(int y=0; y<legal.length; y++){
                if (legal[y] == newID.charAt(x)){
                    exists = true;
                }
            }
            if(!exists) { return "That is not valid"; }
        }

        for(int x=0; x< rooms.size(); x++) {
            if(rooms.get(x).hasClient(this.id)){
                rooms.get(x).addMember(newID);
                rooms.get(x).removeClient(this.id);
            }
        }

        for(int x=0; x< rooms.size(); x++) {
            if(rooms.get(x).hasAdmin(this.id)){
                rooms.get(x).addAdmin(newID);
                rooms.get(x).removeAdmin(this.id);
            }
        }
        this.id = newID;
        return "";
    }
    public String whisper(String whisper, String toSend) {
        boolean exists=false;
        for(int x=0; x<clientlist.size(); x++){
            if(toSend.equalsIgnoreCase(clientlist.get(x).id)){ exists=true; }
        }
        if(!exists){ return "The id you wanted to whisper to was not valid"; }

        return "";
    }

    public String roomChange(String room) {
        String msg;
        for (int x=0; x<rooms.size(); x++) {
            if(rooms.get(x).hasClient(this.id)){
                rooms.get(x).removeClient(this.id);
                for(ChatServerThread client : clientlist)
                    if(rooms.get(x).getMembers().contains(client.id)) {
                        client.out.println(this.id + " has left the room");
                    }
            }
        }

        for (room theRoom : rooms) {
            if(theRoom.title.equalsIgnoreCase(room)){
                theRoom.addMember(this.id);
                msg = "You are in the group " + room + " with: ";
                for (int x=0; x<theRoom.getMembers().size(); x++){
                     if (!theRoom.getMembers().get(x).equalsIgnoreCase(this.id)) {
                         if (x == theRoom.getMembers().size() - 1) {
                             msg += theRoom.getMembers().get(x);
                         } else {
                             msg += theRoom.getMembers().get(x) + " ";
                         }
                     }
                }
            return msg;
            }
        }

        room temp = new room(room);
        temp.addMember(this.id);
        temp.addAdmin(this.id);
        this.addRoom(temp);
        return "You are in the new group " + room;
    }

    public void addLobby(String toAdd) {
        for(room theRoom : rooms){
            if(theRoom.title.equalsIgnoreCase("Lobby")){
                theRoom.addMember(toAdd);
            }
        }
    }

    public void addAdmin (String user, String room) {
        for(room theRoom : rooms){
            ArrayList<String> members = theRoom.getMembers();
            for(String member : members) {
                if (member.equalsIgnoreCase(user) && room.equalsIgnoreCase(theRoom.title)) {
                    theRoom.addAdmin(user);
                }
            }
        }
    }

    public void removeAdmin (String user) {
        for(room theRoom : rooms){
            ArrayList<String> admins = theRoom.getAdmins();
            boolean beRemoved=false;
            for(String admin : admins) {
                if (admin.equalsIgnoreCase(user)) {
                    beRemoved=true;
                }
            }
            if (beRemoved){ theRoom.removeAdmin(user); }
        }
    }

    public void kick (String user) {
        for(room theRoom : rooms){
            ArrayList<String> admins = theRoom.getAdmins();
            for(String admin : admins) {
                if (admin.equalsIgnoreCase(user)) {
                    theRoom.kick(user);
                    addLobby(user);
                }
            }
        }
    }

    public void addTopic (String topic) {
        for(room theRoom : rooms){
            ArrayList<String> admins = theRoom.getAdmins();
            for(String admin : admins) {
                if (admin.equalsIgnoreCase(this.id)) {
                    theRoom.addTopic(topic);
                }
            }
        }
    }

    public boolean isAdmin(){
        for(room theRoom : rooms){
            ArrayList<String> admins = theRoom.getAdmins();
            for(String admin : admins) {
                if (admin.equalsIgnoreCase(this.id)) {
                    return true;
                }
            }
        }
        return false;
    }

    
    public void run() {
        /* Our thread is going to read lines from the client and tell them to teh chat.
           It will continue to do this until an exception occurs or the connection ends
           */
        while (true) {
            try {
                /* Get string from client */
                String fromClient = this.in.readLine();
                
                /* If null, connection is closed, so just finish */
                if (fromClient == null) {
                    for (int x=0; x<clientlist.size(); x++) {
                        if (this == clientlist.get(x)){ clientlist.get(x).out.println("Client " + this.id + " disconnected"); }
                    }
                    this.in.close();
                    this.out.close();
                    this.socket.close();
                    return;
                }
                
                /* If the client said "bye", close the connection */
                if (fromClient.equals("bye")) {
                    for (int x=0; x<clientlist.size(); x++) {
                        if (this != clientlist.get(x)){ clientlist.get(x).out.println("Client " + this.id + " said bye, disconnecting"); }
                    }
                    this.in.close();
                    this.out.close();
                    this.socket.close();
                    return;
                }

                if(fromClient.startsWith("/id ")) {
                    String oldID=this.id;
                    String msg = this.changeID(fromClient.substring(4));
                    if (msg.equals("")) {
                        for (int x=0; x<clientlist.size(); x++) {
                            if (this != clientlist.get(x)){ clientlist.get(x).out.println("Client " + oldID + " changed his/her name to " + this.id); }
                        }
                    }
                    else {
                        this.out.println(msg);
                    }
                }
                else if(fromClient.startsWith("/whisper ")){
                    String msg = this.whisper(fromClient.substring(StringUtils.ordinalIndexOf(fromClient, " ", 2)+1), fromClient.substring(9, StringUtils.ordinalIndexOf(fromClient, " ", 2)));
                    String user = fromClient.substring(9, StringUtils.ordinalIndexOf(fromClient, " ", 2));
                    for (int x=0; x<clientlist.size(); x++) {
                        if (user.equalsIgnoreCase(clientlist.get(x).id) && !user.equalsIgnoreCase(this.id)){
                                clientlist.get(x).out.println(this.id + " whispered : " + (fromClient.substring(StringUtils.ordinalIndexOf(fromClient, " ", 2)+1)).toUpperCase() );
                                return;
                        }
                    }
                    this.out.println(msg);
                }
                else if(fromClient.startsWith("/room ")){
                    String msg = this.roomChange(fromClient.substring(6));
                    String roomName = fromClient.substring(6);
                    for (int x=0; x<rooms.size(); x++) {
                        for (int y=0; y<clientlist.size(); y++){
                            if (rooms.get(x).title.equalsIgnoreCase(roomName) && rooms.get(x).hasClient(clientlist.get(y).id) && !clientlist.get(y).id.equalsIgnoreCase(this.id)) {
                                clientlist.get(x).out.println(this.id + " has joined room "+ roomName );
                            }
                        }
                    }
                    this.out.println(msg);

                    room aRoom = new room("x");
                    for(room theRoom : rooms){
                        ArrayList<String> admins = theRoom.getAdmins();
                        for(String admin : admins) {
                            if (admin.equalsIgnoreCase(this.id)) {
                                aRoom = theRoom;
                            }
                        }
                    }

                    this.out.print("The admins in the group are: ");
                    for(String admin: aRoom.getAdmins()){
                        this.out.print(admin + " ");
                    }
                    this.out.println("");
                    this.out.println("The topic of the room is " + aRoom.topic);
                }
                else if(fromClient.startsWith("/+admin ")){
                    if (!this.isAdmin()){
                        this.out.println("You don't have permission to use that command");
                    }
                    String roomName="";
                    room aRoom = new room("x");
                    String user = fromClient.substring(8);
                    for(room theRoom : rooms){
                        ArrayList<String> admins = theRoom.getAdmins();
                        for(String admin : admins) {
                            if (admin.equalsIgnoreCase(this.id)) {
                                roomName = theRoom.title;
                                aRoom = theRoom;
                            }
                        }
                    }
                    this.addAdmin(user, roomName);
                    for (int x=0; x<clientlist.size(); x++) {
                        if (aRoom.getMembers().contains(clientlist.get(x).id) && !user.equalsIgnoreCase(this.id)){
                            clientlist.get(x).out.println(user + " was made an admin of " + roomName);
                            return;
                        }
                    }
                }
                else if(fromClient.startsWith("/-admin ")){
                    if (!this.isAdmin()){
                        this.out.println("You don't have permission to use that command");
                    }
                    String roomName="";
                    room aRoom = new room("x");
                    String user = fromClient.substring(8);
                    for(room theRoom : rooms){
                        ArrayList<String> admins = theRoom.getAdmins();
                        for(String admin : admins) {
                            if (admin.equalsIgnoreCase(this.id)) {
                                roomName = theRoom.title;
                                aRoom = theRoom;
                            }
                        }
                    }
                    this.removeAdmin(user);
                    for (int x=0; x<clientlist.size(); x++) {
                        if (aRoom.getMembers().contains(clientlist.get(x).id) && !user.equalsIgnoreCase(this.id)){
                            clientlist.get(x).out.println(this.id + " was removed from the admin position in " + roomName);
                            return;
                        }
                    }
                }
                else if(fromClient.startsWith("/kick ")){
                    if (!this.isAdmin()){
                        this.out.println("You don't have permission to use that command");
                    }
                    String user = fromClient.substring(6);
                    String roomName="";
                    room aRoom = new room("x");
                    for(room theRoom : rooms){
                        ArrayList<String> admins = theRoom.getAdmins();
                        for(String admin : admins) {
                            if (admin.equalsIgnoreCase(this.id)) {
                                roomName = theRoom.title;
                                aRoom = theRoom;
                            }
                        }
                    }
                    for (int x=0; x<clientlist.size(); x++) {
                        if (aRoom.getMembers().contains(clientlist.get(x).id) && !user.equalsIgnoreCase(this.id) && !clientlist.get(x).id.equalsIgnoreCase(user)){
                            clientlist.get(x).out.println(user + " was kicked from " + roomName);
                            return;
                        }
                        else if (aRoom.getMembers().contains(clientlist.get(x).id) && clientlist.get(x).id.equalsIgnoreCase(user)) {
                            clientlist.get(x).out.println("You are being kicked from " + roomName + " by " + this.id + ", and being placed in Lobby.");
                        }
                    }
                    this.kick(user);
                }
                else if(fromClient.startsWith("/topic ")){
                    if (!this.isAdmin()){
                        this.out.println("You don't have permission to use that command");
                    }
                    String topic = fromClient.substring(7);
                    String roomName="";
                    room aRoom = new room("x");
                    for(room theRoom : rooms){
                        ArrayList<String> admins = theRoom.getAdmins();
                        for(String admin : admins) {
                            if (admin.equalsIgnoreCase(this.id)) {
                                roomName = theRoom.title;
                                aRoom = theRoom;
                            }
                        }
                    }
                    this.addTopic(topic);
                    for (int x=0; x<clientlist.size(); x++) {
                        if (aRoom.getMembers().contains(clientlist.get(x).id) && !clientlist.get(x).id.equalsIgnoreCase(this.id)) {
                            clientlist.get(x).out.println(this.id + " changed the topic of the room to " + topic);
                            return;
                        }
                    }
                }
                
                /* Otherwise send the text to the server*/

                else {
                    for (int y=0; y < rooms.size(); y++) {
                        for (int x = 0; x < clientlist.size(); x++) {
                            if (this != clientlist.get(x)) {
                                if(rooms.get(y).hasClient(this.id) && rooms.get(y).hasClient(clientlist.get(x).id)) {
                                    clientlist.get(x).out.println("Client " + this.id + " said: " + fromClient);
                                }
                            }
                        }
                    }
                }
                
            } catch (IOException e) {
                /* On exception, stop the thread */
                System.out.println("IOException: " + e);
                return;
            }
        }
    }
    
}

class ChatServerRun {

    public static void main(String[] args) {
        
        /* Check port exists */
        
        /* This is the server socket to accept connections */
        ServerSocket serverSocket = null;
        
        /* Create the server socket */
        try {
            serverSocket = new ServerSocket(9999);
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            System.exit(1);
        }
        room r = new room("Lobby");
        ChatServerThread.addRoom(r);
        /* In the main thread, continuously listen for new clients and spin off threads for them. */
        while (true) {
            try {
                /* Get a new client */
                Socket clientSocket = serverSocket.accept();
                
                /* Create a thread for it and start! */
                ChatServerThread clientThread = new ChatServerThread(clientSocket);
                ChatServerThread.addClient(clientThread);
                new Thread(clientThread).start();

            } catch (IOException e) {
                System.out.println("Accept failed: " + e);
                System.exit(1);
            }
        }
    }
}
