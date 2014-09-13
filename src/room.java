import java.util.ArrayList;

/**
 * Created by davidwoldenberg on 9/11/14.
 */
public class room {
    public String topic;
    public String title;
    public ArrayList<String> admins= new ArrayList<String>();
    private ArrayList<String> members = new ArrayList<String>();

    public room(String title) {
        this.title = title;
        this.topic = title;

    }

    public void addMember(String newMem) {
        members.add(newMem);
    }

    public void removeClient(String oldMem) {
        for(int x=0; x<members.size(); x++){
            if(members.get(x).equalsIgnoreCase(oldMem)) {
                members.remove(x);
            }
        }
    }

    public void addTopic(String topic) {
        this.topic = topic;
    }

    public void addAdmin(String a){
        admins.add(a);
    }

    public void removeAdmin(String a){
        if(admins.contains(a)) { this.admins.remove(a); }
    }

    public void kick(String u) { if(members.contains(u)) { this.members.remove(u); } }

    public ArrayList<String> getMembers() {
        return members;
    }

    public ArrayList<String> getAdmins() { return admins; }

    public boolean hasClient(String id) {
        return members.contains(id);
    }

    public boolean hasAdmin(String id) {
        return admins.contains(id);
    }
}
