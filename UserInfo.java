import java.time.LocalDateTime;

public class UserInfo {
    String userID;
    String IPAddress;
    int PortNumber;
    LocalDateTime timeofconnection;
    LocalDateTime timedisconnected;
    int queries;
    public UserInfo(String ipa, int pnumber, LocalDateTime time, String user){
        this.userID = user;
        this.IPAddress =ipa;
        this.PortNumber = pnumber;
        this.timeofconnection= time;
        this.queries = 0;
    }
    public String getUserID(){
        return userID;
    }
    public String getIPAddress(){
        return IPAddress;
    }
    public int getPortNumber(){
        return PortNumber;
    }
    public LocalDateTime getTimeofconnection(){
        return timeofconnection;
    }
    public LocalDateTime getTimedisconnected() {
        return timedisconnected;
    }
    public int getQueries(){
        return queries;
    }
    public void addquerycount(){
        this.queries++;
    }
    public void setdissconectiontime(){
        this.timedisconnected = LocalDateTime.now();
    }
}
