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
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import javax.servlet.http.HttpSession;
import java.util.Date;


@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * handles GET requests to add and show the item list information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
    	// Get parameters from request
    	String title = request.getParameter("title");
    	String option = request.getParameter("option");
    	
    	System.out.println("title" + title);
    	System.out.println("option" + option);
    	// Get a session object and previous items in a ArrayList
        HttpSession session = request.getSession();

//        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        ArrayList<String> previousItems1 = (ArrayList<String>) session.getAttribute("previousItems");
        if (previousItems1 == null) 
        {
        	if (option == null)
        	{
        		
        	}
        	else if (option.equals("add"))
    		{
	    		previousItems1 = new ArrayList<>();
	            previousItems1.add(title);
	            session.setAttribute("previousItems", previousItems1);
    		}
        	else 
        	{
        		
        	}
        } 
        else 
        {
        	synchronized (previousItems1) 
        	{
	        	if (option == null)
	        	{
	        		
	        	}
	        	else if (option.equals("add"))
	    		{
                    previousItems1.add(title);
	    		}
	        	else if (option.equals("subtract"))
	        	{
        			previousItems1.remove(title);
	        	}
	        	else if (option.equals("remove"))
	        	{
	        		int count = 0; 
	        		for (int i = 0; i < previousItems1.size(); i++)
					{
						if (previousItems1.get(i).toString().equals(title))
						{
							++count;
						}
					}
	        		while (count != 0)
	        		{
	        			previousItems1.remove(title);
	        			--count; 
	        		}
	        	}
	        	else 
	        	{
	        		
	        	}
        	}
        }
        response.getWriter().write(String.join("|", previousItems1));
    }
}
/*
 * When iterate and remove, there is an inconsistent in the result. 
 * Remove supposes to remove all, but in this case, it only removes until the item is one. 
 * 
 * Thus I use a counter to specify how many time the item needs to be removed. 
 * 
 * */
