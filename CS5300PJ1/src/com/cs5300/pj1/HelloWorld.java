package com.cs5300.pj1;

import java.io.IOException;
//import java.io.PrintWriter;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class Wrapper {
    public Wrapper(int version, String message, Timestamp timestamp) {
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

/**
 * Servlet implementation class HelloWorld
 */
@WebServlet("/HelloWorld")
public class HelloWorld extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static int currentID = 0;
    private static Map<String, Wrapper> data_table = new HashMap<String, Wrapper>();
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HelloWorld() {
        super();
        // TODO Auto-generated constructor stub
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
			test = "first time user, ID = " + String.valueOf(currentID);
			String id_str = String.valueOf(currentID);
			currentID++; // increase ID for other session
			Cookie returnVisitorCookie = new Cookie("CS5300PJ1ERIC", id_str); // create cookie
			returnVisitorCookie.setMaxAge(60*60); // set timeout to one hour
			response.addCookie(returnVisitorCookie); // add cookie to data_table
			
			data_table.put(id_str, new Wrapper(0, "Hello new user!!", // put tuple in table 
					new Timestamp(System.currentTimeMillis()  + 1800000)));
			
			cookie_str = data_table.get(id_str).getMessage();
			cookie_timeout = data_table.get(id_str).getTimestamp().toString();
		} else {
			// action control
        	String act = request.getParameter("act");
        	String sessionID = c.getValue();
        	Wrapper sessionData = data_table.get(sessionID);
        	if (sessionData == null) {
        		test = "sessionData is null";
        	} else {
        		// increment version number
	        	sessionData.setVersion(sessionData.getVersion() + 1);
	        	
	        	if (act == null) {
	        		test = "revisit";
	        		c.setMaxAge(60 * 30); // reset cookie timeout to one hour
	    	        sessionData.setTimestamp(new Timestamp(System.currentTimeMillis() + 1800000));
	        		cookie_str = sessionData.getMessage();
					cookie_timeout = sessionData.getTimestamp().toString();
	        	}
	        	else if (act.equals("Refresh")) { // Refresh button was pressed
	        		test = "Refresh";
	        		// Redisplay the session message, with an updated session expiration time;
	    	        c.setMaxAge(60 * 30); // reset cookie timeout to one hour
	    	        sessionData.setTimestamp(new Timestamp(System.currentTimeMillis() + 1800000));
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
	            		c.setMaxAge(60 * 30); // reset cookie timeout to one hour
	            		sessionData.setTimestamp(new Timestamp(System.currentTimeMillis() + 1800000));
	            		sessionData.setMessage(message);
	                    sc.close();
	            	}
	            	cookie_str = sessionData.getMessage();
    				cookie_timeout = sessionData.getTimestamp().toString();
	        	}
	        	else if (act.equals("Logout")) {	// LogOut button was pressed
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
