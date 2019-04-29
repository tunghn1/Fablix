import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
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

/**
 * Servlet implementation class CheckOutServlet
 */
@WebServlet(name = "CheckOutServlet", urlPatterns = "/api/checkout")
public class CheckOutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource (name = "jdbc/moviedb")
	private DataSource dataSource;
       
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String firstname = request.getParameter("firstname").trim();
		String lastname = request.getParameter("lastname").trim();
		String email = request.getParameter("email").trim();
		String ccNum = request.getParameter("ccNum").trim();
		String ccDate = request.getParameter("ccDate").trim();
	

		PrintWriter out = response.getWriter();
		
		try {
			
			Context initCtx = new InitialContext();
            
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if(envCtx == null) 
            	out.println("envCtx is Null");
            
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");
            
            if(ds == null)
            	out.println("ds is null.");
            
            Connection dbcon = ds.getConnection();
            if(dbcon == null) out.println("dbcon is null");	
		
		String customerInfoQuery = "SELECT * \n" +
				"FROM customers c, creditcards cd \n" +
				"WHERE c.ccId = cd.id \n" +
				"AND c.email=? AND c.firstName =? \n" +
				"AND c.lastName=? AND cd.id=? \n" +
			    "AND cd.expiration=?;";
			
		PreparedStatement customerInfoCheckQuery = dbcon.prepareStatement(customerInfoQuery);
		
		customerInfoCheckQuery.setString(1, email);
		customerInfoCheckQuery.setString(2, firstname);
		customerInfoCheckQuery.setString(3, lastname);
		customerInfoCheckQuery.setString(4, ccNum);
		customerInfoCheckQuery.setString(5,  ccDate);
		
		
		ResultSet matchResult = customerInfoCheckQuery.executeQuery();
		
		
		
		JsonObject jsonObject = new JsonObject();
		
		if(matchResult.next()) {
			if(matchResult.getString("id") != null)
			{	
			
			jsonObject.addProperty("result", "success");
			
			}
			else {
				jsonObject.addProperty("result", "fail");
				
			}
			
		}
		else {
			
			
			jsonObject.addProperty("result", "fail");
		
			
		}
		
		
		
		out.write(jsonObject.toString());
		response.setStatus(200);
		matchResult.close();
		customerInfoCheckQuery.close();
		dbcon.close();
	
		
		}
		catch(Exception e) {
			
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
			
			response.setStatus(500);
			
		}
		
		out.close();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
		response.setContentType("application/json");
		
		String total = request.getParameter("total").trim();
		String email = request.getParameter("email").trim();
		int intTotal = Integer.parseInt(total);
		
		HashMap<String, Integer> hashmap = new HashMap<String, Integer>();
		
		for(int i = 1; i <= intTotal; i++) {
			hashmap.put(request.getParameter("title"+i), Integer.parseInt(request.getParameter("quantity"+i)));
		}
		
		
		PrintWriter out = response.getWriter();
		
		try {
			Context initCtx = new InitialContext();
            
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if(envCtx == null) 
            	out.println("envCtx is Null");
            
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb_write");
            
            if(ds == null)
            	out.println("ds is null.");
            
            Connection dbcon = ds.getConnection();
            if(dbcon == null) out.println("dbcon is null");
			
			String title_query = "";
			String[] titleArray = new String[intTotal];
			
			int i = 0;
			for(String key : hashmap.keySet())
			{
				titleArray[i] = key;
				i++;
			}
			
			title_query += "m.title='" + titleArray[0]+"'";
			
			for(int j = 1; j < titleArray.length; j++) {
				title_query += " OR m.title='" + titleArray[j] + "'";
			}
			
			String movieIdQuery = "SELECT m.id, m.title\n" + 
					"FROM movies m \n" + 
					"WHERE " + title_query + ";";
			
			
			PreparedStatement movieIdPreparedStatement = dbcon.prepareStatement(movieIdQuery);
			ResultSet movieIdExecute = movieIdPreparedStatement.executeQuery();
			
			HashMap<String, String> movieIdNTitleMap= new HashMap<String,String>();
		
			while(movieIdExecute.next())
			{
				String id = movieIdExecute.getString("id");
				String title = movieIdExecute.getString("title");
				movieIdNTitleMap.put(title, id);
								
			}
			// movieId returned
			
			String customerIdQuery = "SELECT c.id as id FROM customers c WHERE c.email=?";
			
			PreparedStatement customerIdPreparedStatement = dbcon.prepareStatement(customerIdQuery);
			customerIdPreparedStatement.setString(1, email);
			ResultSet customerIdExecute = customerIdPreparedStatement.executeQuery();
			
			String customerId = "";
			if(customerIdExecute.next())
			{
				customerId = customerIdExecute.getString("id");
			}
			//customerid returned 
			
			//movieId and title // hashmap -> movieTitle and quantity 
			//customerid 
			
			JsonArray jsonArray = new JsonArray();
			
			for(Map.Entry<String, Integer> entry : hashmap.entrySet()) {
			    System.out.println(entry.getKey()+" : "+entry.getValue());
			    
			    String movieTitle = entry.getKey();
			    int quantity = entry.getValue();
			    String movieId = movieIdNTitleMap.get(movieTitle);
			    System.out.println(movieTitle);
			    System.out.println(quantity);
			    System.out.println(movieId);
			    for(int q = 0; q < quantity; q++)
			    {
			    	String query = "INSERT INTO sales (id, customerId, movieId, saleDate) VALUES (null, '" + customerId + "', '" + movieId + "', CURDATE() );";
			    	PreparedStatement insertQuery = dbcon.prepareStatement(query);
			    	insertQuery.executeUpdate();
			    	System.out.print("successfully inserted");
			    
			    }
			    JsonObject inserted = new JsonObject();
			    inserted.addProperty("title", movieTitle);
			    inserted.addProperty("id", movieId);
			    inserted.addProperty("quantity", quantity);
			    
			    jsonArray.add(inserted);
			}
			
			
			out.write(jsonArray.toString());
			response.setStatus(200);
			movieIdExecute.close();
			customerIdExecute.close();
			dbcon.close();
			
			

			
			
			
			
		}
		catch(Exception e){
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
			
		}
		out.close();
		
		
		
		
	}

}
