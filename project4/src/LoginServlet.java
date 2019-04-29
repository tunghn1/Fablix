import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jasypt.util.password.StrongPasswordEncryptor;
import java.io.*;
import java.sql.*;
/**
 * This class is declared as LoginServlet in web annotation, 
 * which is mapped to the URL pattern /api/login
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     * This function 
     * 		takes the username and password from the request. 
     * 		Load driver
     * 		Define a connection url
     * 			Use resource annotation for the database 
     * 			Define a data source 
     * 		Establish connection using data source
     * 		Create a string query
     * 		Create a prepared statement and set username and password to the approrpriate ?
     * 		Query the result until there is nothing left
     * 			Get the returned value from the provided key as a string
     * 			Add the string to the Json object
     * 			Add the Json object to the Json object array
     * 		Close the connection, including result set, prepared statement, and database. 
     * 
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
 
    	String username = request.getParameter("username");
        String password = request.getParameter("password");
        String customer = request.getParameter("customer");
//        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
//        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
        PrintWriter out = response.getWriter();
    	
    	// Verify reCAPTCHA
//        try 
//        {
//            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//        } 
//        catch (Exception e) 
//        {
//        	JsonObject jsonObject = new JsonObject();
//        	jsonObject.addProperty("status", "fail");
//			jsonObject.addProperty("message", "Receptcha failed");
//			out.write(jsonObject.toString());
//			out.close();
//			return;
//        }
        
        try
        {
        	
        	if (customer == null)
        	{
        		JsonObject jsonObject = new JsonObject();
            	jsonObject.addProperty("status", "fail");
    			jsonObject.addProperty("message", "Wrong username or password");
    			out.write(jsonObject.toString());
    			response.setStatus(500);
        	}
        	else 
        	{
        		/*
        		 * Select all from customers where email is something
        		 * Create a preparedStatement and fill in the email 
        		 * Execute the preparedStatement
        		 * Create a jSon object
        		 * while the result is not null
        		 * 		Get email and password
        		 * 		Create a customer_password_success and assign it to false
        		 * 		If the newly-retrieved password is similar to the input parameter, then 
        		 * 			Assign true to customer_password_success
        		 * 			If username.equals(email) && customer_password_success, then
        		 * 				Reuse the logic below
        		 * 		Else 
        		 * 			Do nothing 
        		 * Else
        		 * 		Reuse the logic below
        		 */
        		Connection dbCon = dataSource.getConnection();
            	String query = "";
            	if (customer.equals("yes"))
            	{
            		query = "SELECT * From customers where email = ?;";
            		PreparedStatement preparedStatement = dbCon.prepareStatement(query);
                	preparedStatement.setString(1, username);
                	ResultSet rs = preparedStatement.executeQuery();
                	
                	JsonObject jsonObject = new JsonObject();
                	
                	if(rs.next())
                	{
                		String 		email 				= rs.getString("email");
                		String	 	CustomerPassword 	= rs.getString("password");
                		boolean 	successPassword 	= false;     
                					successPassword 	= new StrongPasswordEncryptor().checkPassword(password, CustomerPassword);
                		
                		if (email.equals(username) && successPassword)
                		{
                			request.getSession().setAttribute("user", new User(username));
                			request.getSession().removeAttribute("previousItems");
                			jsonObject.addProperty("status", "success");
                    		jsonObject.addProperty("message", "success");
                		}

                	}
                	else 
                	{
                		jsonObject.addProperty("status", "fail");
                		jsonObject.addProperty("message", "Wrong username or password");
                		      		
                	}
                	
                	out.write(jsonObject.toString());
                	response.setStatus(200);
                	rs.close();
                	preparedStatement.close();
                	dbCon.close();
            	}
            	else 
            	{
            		/*
            		 * Select all from employees where email is something
            		 * Create a preparedStatement and fill in the email 
            		 * Execute the preparedStatement
            		 * Create a jSon object
            		 * If the result is not null
            		 * 		Get email and password
            		 * 		Create a employees_password_success and assign it to false
            		 * 		If the newly-retrieved password is similar to the input parameter, then 
            		 * 			Assign true to employees_password_success
            		 * 			If username.equals(email) && employees_password_success, then
            		 * 				Log the employee into session
            		 * 		Else 
            		 * 			Do nothing 
            		 * Else
            		 * 		Reuse the logic below
            		 */
            		query = "SELECT * From employees where email = ?;";
            		PreparedStatement preparedStatement = dbCon.prepareStatement(query);
                	preparedStatement.setString(1, username);
                	ResultSet rs = preparedStatement.executeQuery();
                	
                	JsonObject jsonObject = new JsonObject();
                	
                	if(rs.next())
                	{
                		String 		email 				= rs.getString("email");
                		String	 	employeePassword 	= rs.getString("password");
                		boolean 	success 			= false;     
                					success 			= new StrongPasswordEncryptor().checkPassword(password, employeePassword);
    		
                		if (email.equals(username) && success)
                		{
                    			request.getSession().setAttribute("employee", new Employee(username));
                    			jsonObject.addProperty("status", "success");
                        		jsonObject.addProperty("message", "success");
                		}

                	}
                	else 
                	{
                		jsonObject.addProperty("status", "fail");
                		jsonObject.addProperty("message", "Wrong username or password");
                		      		
                	}
                	
                	out.write(jsonObject.toString());
                	response.setStatus(200);
                	rs.close();
                	preparedStatement.close();
                	dbCon.close();
            	}
        	}
        	out.close();
        }
        catch(Exception e)
        {
        	JsonObject jsonObject = new JsonObject();
        	jsonObject.addProperty("status", "fail");
			jsonObject.addProperty("message", e.getMessage());
			out.write(jsonObject.toString());
			response.setStatus(500);
			out.close();
        }
        
    }
}

/*
 * If I use this response.setContentType("application/json"); I will get error 
 * VM367:1 Uncaught SyntaxError: Unexpected token o in JSON at position 1
    at JSON.parse (<anonymous>)
    at handleLoginResult (login.js:6)
    at Object.$.post [as success] (login.js:29)
    at i (jquery.min.js:2)
    at Object.fireWith [as resolveWith] (jquery.min.js:2)
    at A (jquery.min.js:4)
    at XMLHttpRequest.<anonymous> (jquery.min.js:4)
    */

/*
 * This project previously does not have context.xml and a part in web.xml for to connect to database
 * This project previusly does not have jar file for StrongPasswordLibrary: jasypt-1.9.2.jar and dependecy
 * in pom.xml file which is 
 *         <dependency>
            <groupId>org.jasypt</groupId>
            <artifactId>jasypt</artifactId>
            <version>1.9.2</version>
        </dependency>
*/