import com.google.gson.JsonArray;
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
import java.sql.Statement;
import java.sql.CallableStatement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare our query
            String query = 	"SELECT TABLE_NAME as tables, GROUP_CONCAT(COLUMN_NAME) as columns, GROUP_CONCAT(COLUMN_TYPE) as types\r\n" + 
            				"FROM INFORMATION_SCHEMA.COLUMNS\r\n" + 
        					"WHERE TABLE_SCHEMA = 'moviedb'\r\n" + 
        					"GROUP BY TABLE_NAME;";
            // Declare a preparedStatement object
            PreparedStatement preparedStatement = dbcon.prepareStatement(query);
            
            // Perform the query
            ResultSet rs = preparedStatement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String tables = rs.getString("tables");
                String columns = rs.getString("columns");
                String types = rs.getString("types");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("tables", tables);
                jsonObject.addProperty("columns", columns);
                jsonObject.addProperty("types", types);

                jsonArray.add(jsonObject);
            }
            
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            preparedStatement.close();
            dbcon.close();
        } catch (Exception e) {
        	
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);

        }
        out.close();

    }
    /* *************************************** Add movie ***********************************************************
     * Get parameters
     * Get a connection from dataSource 
     * Declare callableStatement object 
     * Set parameter
     * Execute the statement
     * Read the result
     * Pass to frontend 
     * Close connection
     */
    protected void AddMovie(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
    	String title 		= request.getParameter("title");
    	String year 		= request.getParameter("year");
    	String director 	= request.getParameter("director");
    	String star 		= request.getParameter("star");
    	String genre 		= request.getParameter("genre");
    	
    	// Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        try
        {	
    		// Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare callableStatement object 
            CallableStatement callableStatement = dbcon.prepareCall("{Call add_movie(?, ?, ?, ?, ?, ?)}");

            // Set parameter
            if (title.isEmpty())
            {
            	callableStatement.setString(1, "");
            }
            else 
            {
            	callableStatement.setString(1, title);
            }

            if (year.isEmpty())
            {
            	callableStatement.setInt(2, 0);
            }
            else 
            {
            	callableStatement.setInt(2, Integer.parseInt(year));
            }
            
            if (director.isEmpty())
            {
            	callableStatement.setString(3, "");
            }
            else 
            {
            	callableStatement.setString(3, director);
            }
            
            if (star.isEmpty())
            {
            	callableStatement.setString(4, "");
            }
            else 
            {
            	callableStatement.setString(4, star);
            }
            
            if (star.isEmpty())
            {
            	callableStatement.setString(5, "");
            }
            else 
            {
                callableStatement.setString(5, genre);
            }

            callableStatement.registerOutParameter(6, java.sql.Types.VARCHAR);
            
            // Execute the statement
            callableStatement.execute();

            // Read the result
            String message = callableStatement.getString(6);
	        
            // Pass to frontend 
            JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("message", message);
			out.write(jsonObject.toString());
			response.setStatus(200);
			
	        // Close connection
            callableStatement.close();
	        dbcon.close();
	            
	    	
        }
        catch (Exception e) 
        {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
			response.setStatus(500);
        }
        out.close();
    }
    /* ************************************************* Add star *************************************************
     * If star is null, then return the error message
     * else, do the following
    	 * Create a database connection
    	 * Declare statement for getting maximum id in star table
    	 * Execute the statement
    	 * Store the result into a string
    	 * Parse the string into an array where array[0] = "nm" and array[1] = "Some number here"
    	 * Convert the tail into an integer
    	 * Increment tail by 1 
    	 * Convert the tail into the string, concatenate with head, and store entire string in the previous placeholder
    	 * close the statement
    	 * 
    	 * Declare a preparedStatement for inserting stars into star table for id and star
    	 * If birthday is not null, then 
    	 * 		Concatenate a clause for birthday with placeholder into the preparedStatement
    	 * 		Set id, star, and birthday
    	 * Else 
    	 * 		Set id and star
    	 * Execute the statement
    	 * close the preparedStatement
    	 * close connection
    	 * Close the database
     * 
     */
    protected void AddStar(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        // Get ulr parameter
    	String star 	= request.getParameter("star");
    	String birthday 	= request.getParameter("birthday");
    	
    	// Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        try
        {
	    	if (star.isEmpty())
	    	{
	    		JsonObject jsonObject = new JsonObject();
	        	jsonObject.addProperty("status", "fail");
				jsonObject.addProperty("message", "No Star");
				out.write(jsonObject.toString());
	    	}
	    	else 
	    	{
	    		String id = "";
	    			        		
	    		// Get a connection from dataSource
	            Connection dbcon = dataSource.getConnection();
	
	            // Declare query for getting max id 
	            String query = 	"SELECT MAX(id) AS id FROM stars;";
	
	            // Declare a preparedStatement object
	            PreparedStatement preparedStatementId = dbcon.prepareStatement(query);
	
	            // Perform the query
	            ResultSet rs = preparedStatementId.executeQuery();
	            
	            // Getting max id
	            if (!rs.next()) 
	            {
	            	JsonObject jsonObject = new JsonObject();
	            	jsonObject.addProperty("status", "fail");
	    			jsonObject.addProperty("message", "Unable to select maxId");
	    			out.write(jsonObject.toString());		
	            }
	            else 
	            {
	            	id = rs.getString("id");
	            	
	            	// Parse the string, increment the currentMaxId, convert newMaxId into a string, and concatenate with head
	            	String[] 	parseId 		= id.split("(?<=\\D)(?=\\d)"); 
	            	String 	 	head 			= parseId[0];
	            	int			currentMaxId 	= Integer.parseInt(parseId[1]);
	            	int 		newMaxId		= currentMaxId + 1;
	            	String 		newMaxIdString  = Integer.toString(newMaxId);
	            				id				= head + newMaxIdString;
	            	String 		insertStar		= "";
	            			            	
	            	if (birthday.isEmpty())
	            	{
		            	// Declare a query
		            	insertStar = "INSERT INTO stars (id, name) VALUES(?,?);";
	            	}
	            	else 
	            	{
	            		insertStar = "INSERT INTO stars (id, name, birthYear) VALUES(?,?,?);";
	            	}
	            	
	            	// Declare a preparedStatement
	            	PreparedStatement preparedStatementStarWithBirth =  dbcon.prepareStatement(insertStar);
	            	PreparedStatement preparedStatementStarWithoutBirth = dbcon.prepareStatement(insertStar);
	            	
	            	// Set parameters
	            	if (birthday.isEmpty())
	            	{
	            		preparedStatementStarWithoutBirth.setString(1, id);
	            		preparedStatementStarWithoutBirth.setString(2, star);
		            	
		            	// Execute the preparedStatement
		            	if (preparedStatementStarWithoutBirth.executeUpdate() >= 1)
		            	{
		            		JsonObject jsonObject = new JsonObject();
			            	jsonObject.addProperty("status", "success");
			    			jsonObject.addProperty("message", "Star inserted");
			    			out.write(jsonObject.toString());
			    			response.setStatus(200);
			    			
		            	}
		            	else 
		            	{
		            		JsonObject jsonObject = new JsonObject();
			            	jsonObject.addProperty("status", "fail");
			    			jsonObject.addProperty("message", "No row is updated");
			    			out.write(jsonObject.toString());
		            	}
	            	}
	            	else 
	            	{
	            		preparedStatementStarWithBirth.setString(1, id);
	            		preparedStatementStarWithBirth.setString(2, star);
	            		preparedStatementStarWithBirth.setInt(3, Integer.parseInt(birthday));
		            	
		            	// Execute the preparedStatement
		            	if (preparedStatementStarWithBirth.executeUpdate() >= 1)
		            	{
		            		JsonObject jsonObject = new JsonObject();
			            	jsonObject.addProperty("status", "success");
			    			jsonObject.addProperty("message", "Star inserted");
			    			out.write(jsonObject.toString());
			    			response.setStatus(200);
			    			
		            	}
		            	else 
		            	{
		            		JsonObject jsonObject = new JsonObject();
			            	jsonObject.addProperty("status", "fail");
			    			jsonObject.addProperty("message", "No row is updated");
			    			out.write(jsonObject.toString());
		            	}
	            	}
		
	            	preparedStatementStarWithoutBirth.close();
	            	preparedStatementStarWithBirth.close();
	            	rs.close();
	            	preparedStatementId.close();
	            	dbcon.close();
	            }
	    	}
        }
        catch (Exception e) 
        {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
			response.setStatus(500);
        }
        out.close();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	String add 			= request.getParameter("add");
    	
    	// Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        try {
            
        	if (add == null)
        	{
        		JsonObject jsonObject = new JsonObject();
            	jsonObject.addProperty("status", "fail");
    			jsonObject.addProperty("message", "Empty selection");
    			out.write(jsonObject.toString());
        	}
        	else if (add.equals("star"))
        	{
        		AddStar(request,response);      		
        	}
        	else 
        	{
        		AddMovie(request,response);
        	}
        } 
        catch (Exception e) 
        {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
			response.setStatus(500);
        }
        out.close();

    }
}


/*Note: 
 * When sending json object back to the js file, if contain special character such as in "There is no star's name", then the 
 * js file may not display the error message
 * 
 * Cannot use one preparedStatement object for different query since the form is stored inside of the database. 
 * 
 * If I leave setStatus 500, then the browser will display error even if I have no error.  
 * # Mysql is case-insensitive; thus, TITLE and title are the same. When I set TITLE to empty, then parameter title is also empty. 
 # Deal with foreign key constraints problem by SET FOREIGN_KEY_CHECKS=0; and later SET FOREIGN_KEY_CHECKS=1; 
 */