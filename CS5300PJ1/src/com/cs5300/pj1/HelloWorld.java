package com.cs5300.pj1;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
//import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.*;


class SessionData {
    public SessionData(int version, String message, Timestamp timestamp) {
       this.version = version;
       this.message = message;
       this.timestamp = timestamp;
    }

    //public String getSessionID() { return this.sessionID; }
    public int getVersion() { return this.version; }
    public String getMessage() { return this.message; }
    public Timestamp getTimestamp() { return this.timestamp; }
    
    //public void setSessionID(String sessionID) { this.sessionID = sessionID; }
    public void setVersion(int version) { this.version = version; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    
    //private String sessionID;
    private int version;
    private String message;
    private Timestamp timestamp;
}

class ServerStatus {
    public ServerStatus(String status, long time_stamp) {
       this.status = status;
       this.time_stamp = time_stamp;
    }

    public String getStatus() { return this.status; }
    public long getTimeStamp() { return this.time_stamp; }
    
    public void setStatus(String status) { this.status = status; }
    public void setTimeStamp(long time_stamp) { this.time_stamp = time_stamp; }
    
    private String status;
    private long time_stamp;
}
/**
 * Servlet implementation class HelloWorld
 */
@WebServlet("/HelloWorld")
public class HelloWorld extends HttpServlet {
    //private static final long serialVersionUID = 1L;
    
    private static Map<String, SessionData> data_table = new HashMap<String, SessionData>();
    private static Map<String, ServerStatus> group_view = new HashMap<String, ServerStatus>();
    private static String local_IP = null;
    private static String meta_primary = "192.168.0.1"; // test use, should be delete for deploy
    private static String meta_backup  = "192.168.0.2"; // test use, should be delete for deploy
    private static int sess_num = 0;
    private static int sess_timeout_secs = 1800000;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public HelloWorld() {
        super();
        // TODO Newly Booted Servers
        /*
         A newly-booted server does not yet know about
        any servers other than itself. The server should immediately try to 
        initialize its membership view from a well-known SimpleDB View database
        as described in Section 3.8 below. If the server receives a new-session 
        client request (a client request without a session cookie) before it has 
        initialized its View, the server will be unable to store two copies of 
        the new session data, so 1-resilience will not be guaranteed. You might 
        be tempted to return an error in this case, or wait until the view becomes
        nonempty. However, such a policy would prevent you from running and 
        debugging a system configuration that had only a single server instance.
        Since that is a very convenient thing to do, you should just create a 
        non-replicated session cookie in this case.
         */

        // TODO garbage collection
        ScheduledExecutorService garbageCollector = Executors.newSingleThreadScheduledExecutor();
        garbageCollector.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // Iterate through session table
                // collect data which has discard time < now
            }
        }, 0, 600, TimeUnit.SECONDS);

        // TODO View exchange
        ScheduledExecutorService viewExchanger = Executors.newSingleThreadScheduledExecutor();
        viewExchanger.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // TODO
                // Randomly choose a server
                // 1. Normal Server
                // RPC : hisView = exchangeViews(myView)
                // Merge hisView and MyView
                // Replace MyView with Merged view

                // 2. SimpleDB
                // Note
                // store the View in SimpleDB as a character string, using an encoding
                // similar to the way metadata is encoded in a session cookie. That way,
                // an entire View can be read from or written back to SimpleDB using a 
                // single API call.

                // Read ViewSDB from SimpleDB.
                // Compute a new merged View, Viewm, from ViewSDB and the current View.
                // Store Viewm back into SimpleDB.
                // Replace the current View with Viewm.

                /* avoid convoys
                Random generator = new Random();
                    ...
                  while(true) {
                    ... gossip with another site chosen at random ...
                    sleep( (GOSSIP_SECS/2) + generator.nextInt( GOSSIP_SECS ) )
                  }
                */


                
            }
        }, 0, 600, TimeUnit.SECONDS);

        // Get server IP
        // please change this boolean when deployed onto EB
        boolean isLocal = true;
        
        // Method #1: local IP
        if (isLocal == true) {
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    // filters out 127.0.0.1 and inactive interfaces
                    if (iface.isLoopback() || !iface.isUp())
                        continue;
    
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    InetAddress addr = addresses.nextElement(); // Get first element (mac address)
                    addr = addresses.nextElement(); // Get second element (ip address)
                    local_IP = addr.getHostAddress();
                    System.out.println(local_IP);
                    /*
                    while(addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        ip = addr.getHostAddress();
                        System.out.println(iface.getDisplayName() + " " + ip);
                    }
                    */
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
        // Method #2: Runtime.exec()
        else {
            try {
                String[] cmd = new String[3];
                cmd[0] = "/opt/aws/bin/ec2-metadata";
                cmd[1] = "--public-ipv4";
                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec(cmd);
                InputStream stdin = proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdin);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ( (line = br.readLine()) != null) {
                    // example output = public-ipv4: ww.xx.yy.zz
                    local_IP = line.substring(13);
                }
                System.out.println(local_IP);
                int exitVal = proc.waitFor();
                System.out.println("Process exitValue: " + exitVal);
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // initialization
        String cookie_str = "";
        String cookie_timeout = "";
        String test = "";
        Cookie c = getCookie(request, "CS5300PJ1ERIC");
        
        if (c == null) {
            // No cookie --> first time user
            // SessID = < sess_num, SvrID >
            // Cookie value = < SessID, version, < SvrIDprimary, SvrIDbackup > >
            String cookie_value = Integer.toString(sess_num) + "_" + local_IP + "_0_" 
                    + meta_primary + "_" + meta_backup;
            
            test = "first time user, on " + local_IP + ", sess_num = " + String.valueOf(sess_num);
            String id_str = String.valueOf(sess_num);
            sess_num++; // increase session number for future session
            Cookie returnVisitorCookie = new Cookie("CS5300PJ1ERIC", id_str); // create cookie
            returnVisitorCookie.setMaxAge(60*60); // set timeout to one hour
            response.addCookie(returnVisitorCookie); // add cookie to data_table
            
            data_table.put(id_str, new SessionData(0, "Hello new user!!", // put tuple in table 
                    new Timestamp(System.currentTimeMillis()  + sess_timeout_secs)));
            
            cookie_str = data_table.get(id_str).getMessage();
            cookie_timeout = data_table.get(id_str).getTimestamp().toString();
        
        } else { // returned user
            
            // TODO remember to update cookie value for session version number
            
            // action control
            String act = request.getParameter("act");
            String sessionID = c.getValue();
            String[] sessionID_token = sessionID.split("_");
            // token format: < sess_num, SvrID, version, SvrIDprimary, SvrIDbackup >
            if (sessionID_token[3].equals(local_IP) || sessionID_token[3].equals(local_IP)) {
                // session data is in local table
                
            }
            else { // session data not exists in local sessio table
                
                // TODO Perform RPC sessionRead to primary and backup concurrently
                
                // TODO If request failed, return an HTML page with a message "session timeout or failed"
                
                // TODO Delete cookie for the bad session
            }
            // Get session data from table by ID
            SessionData sessionData = data_table.get(sessionID);
            
            
            
            
            if (sessionData == null) {
                test = "sessionData is null"; // Error occur!
            
            } else { // sessionData exist
                
                // Increment version number
                sessionData.setVersion(sessionData.getVersion() + 1);
                
                // Store new session state into local session table


                // RPC, store new session state into remote server
                // SessionWrite( SessID, new_version, new_data, discard_time )
                // discard time = System.currentTimeMillis() + sess_timeout_secs + delta

                // Try primary or backup server in session ID to avoid obselete copy
                
                // Response timeout
                // 1. Choose different server for backup
                // 2. No backup can be found, abort
                
                // Repunse success
                // Update view with < SvrID, up, now >
                // Construct new session cookie

                // Response fail
                // Update view with < SvrID, down, now >
                // Create new cookie with backup = NULL 

                if (act == null) {
                    test = "revisit";
                    c.setMaxAge(60 * 30); // reset cookie timeout to one hour
                    sessionData.setTimestamp(new Timestamp(System.currentTimeMillis() + sess_timeout_secs));
                    cookie_str = sessionData.getMessage();
                    cookie_timeout = sessionData.getTimestamp().toString();
                }
                else if (act.equals("Refresh")) { // Refresh button was pressed
                    test = "Refresh";
                    // Redisplay the session message, with an updated session expiration time;
                    c.setMaxAge(60 * 30); // reset cookie timeout to one hour
                    sessionData.setTimestamp(new Timestamp(System.currentTimeMillis() + sess_timeout_secs));
                    cookie_str = sessionData.getMessage();
                    cookie_timeout = sessionData.getTimestamp().toString();
                }
                else if (act.equals("Replace")) { // Replace button was pressed
                    test = "Replace";
                    // Replace the message with a new one (that the user typed into an HTML form field)
                    // and display the (new) message and expiration time;
                    String message = request.getParameter("message");
                    final byte[] utf8Bytes = message.getBytes("UTF-8"); // get message byte length
                    Scanner sc = new Scanner(message);
                    //c.setValue(message);
                    if (utf8Bytes.length > 512) {
                        test = "string too long";
                    }
                    else if (!sc.hasNext("[A-Za-z0-9\\.-_]+")) {
                        test = "Invalid string! only allow [A-Za-z.-_]";
                    }
                    else { // error message
                        // is safe characters and length < 512 bytes
                        test = "valid replace";
                        sessionData.setTimestamp(new Timestamp(System.currentTimeMillis() + sess_timeout_secs));
                        sessionData.setMessage(message);
                        sc.close();
                    }
                    c.setMaxAge(60 * 30); // reset cookie timeout to one hour
                    cookie_str = sessionData.getMessage();
                    cookie_timeout = sessionData.getTimestamp().toString();
                }
                else if (act.equals("Logout")) {    // LogOut button was pressed
                    test = "Logout";
                    data_table.remove(sessionID); // remove cookie information from table
                    c.setMaxAge(0); // Terminate the session
                    cookie_str = "Goodbye";
                    cookie_timeout = "Expired";
                }
                
                data_table.put(sessionID, sessionData); // replace old data with same key
                response.addCookie(c); // put cookie in response
                
            }
        }
        
        // for page display
        request.setAttribute("test", test);
        request.setAttribute("cookie_str", cookie_str);
        request.setAttribute("cookie_timeout", cookie_timeout);
        request.getRequestDispatcher("/HelloWorld.jsp").forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
    }
    
    // helper function
    public static Cookie getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

}
