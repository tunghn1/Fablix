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
import java.io.*;

@WebServlet(name = "MovieSearchServlet", urlPatterns = "/api/movieSearch")
public class MovieSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    
    /*
     * Autocomplete Search
     * 
     */
    protected void AutocompleteSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	response.setContentType("application/json"); 
        String urlTitle = request.getParameter("title");

        PrintWriter out = response.getWriter();
        
        try {
        	
            Connection dbcon = dataSource.getConnection();
            
            String dataQuery = 	"SELECT m.id, title, year, director, GROUP_CONCAT(DISTINCT g.name) AS genre, GROUP_CONCAT(DISTINCT s.name) AS stars, r.rating\r\n" + 
            		"FROM movies m LEFT JOIN ratings r ON m.id = r.movieId, genres_in_movies gn INNER JOIN genres g ON g.id=gn.genreId, stars_in_movies st INNER JOIN stars s ON st.starId=s.id \r\n" + 
            		"WHERE MATCH (m.title) AGAINST (? IN BOOLEAN MODE) and m.id = gn.movieID AND st.movieID = m.id\r\n" + 
            		"GROUP BY m.id, r.rating ORDER BY m.title;";
            
            PreparedStatement autocompleteStatement = dbcon.prepareStatement(dataQuery);
            
            // Split tokens, append + to the beginning, and append * space to the end of each token
            String[] tokens 	= urlTitle.split(",");
            String newUrlTitle 	= "";
            
            for (int i = 0; i < tokens.length; i++)
            {
            	newUrlTitle += " +";
            	newUrlTitle += tokens[i];
            	newUrlTitle += "*";
            }
            System.out.println("newUrlTitle " + newUrlTitle);
            
            autocompleteStatement.setString(1, newUrlTitle);
            
            ResultSet rs = autocompleteStatement.executeQuery();
            
            JsonArray jsonArray = new JsonArray();
            
            while (rs.next()) {
            	String id = rs.getString("id");
            	String title = rs.getString("title");
            
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("data", id);
                jsonObject.addProperty("value", title);
                System.out.println(jsonObject);
                jsonArray.add(jsonObject);

            }
                        
            out.write(jsonArray.toString());
            
            response.setStatus(200);
            
            rs.close();
            autocompleteStatement.close();
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
    /*
     * Advanced Search
     */
    protected void AdvancedSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
    	response.setContentType("application/json"); 
        String urlTitle = request.getParameter("title");
        String urlYear = request.getParameter("year");
        String urlDirector = request.getParameter("director");
        String urlActor = request.getParameter("actor");
        String urlLimit = request.getParameter("limit");
        String urlOffset = request.getParameter("offset");
        String urlSortBy = request.getParameter("sortby");
        
        if (urlSortBy == null) ;
        else if(urlSortBy.equals("title ASC") || urlSortBy.equals("title DESC")) urlSortBy = "m." + urlSortBy;
        else urlSortBy = "r." + urlSortBy;
        

        int limitInput = Integer.parseInt(urlLimit);
        int offsetInput = Integer.parseInt(urlOffset);
        int count 		= 1;
        PrintWriter out = response.getWriter();

        try {
        	
            Connection dbcon = dataSource.getConnection();

            String totalQuery = "SELECT COUNT(DISTINCT m.title) As total \n"+
            "FROM movies m, stars_in_movies st JOIN stars s ON st.starId=s.id \n" +
            "WHERE st.movieId=m.id \n ";
            
            
            
            String dataQuery = 	"SELECT m.id, title, year, director, GROUP_CONCAT(DISTINCT g.name) AS genre, GROUP_CONCAT(DISTINCT s.name) AS stars, r.rating \r\n" + 
    				"FROM movies m LEFT JOIN ratings r ON m.id = r.movieId,\r\n" + 
    				"genres_in_movies gn INNER JOIN genres g ON g.id=gn.genreId, \r\n" + 
    				" stars_in_movies st INNER JOIN stars s ON st.starId=s.id \r\n" + 
    				"WHERE m.id = gn.movieID AND st.movieID = m.id ";
            
            String searchQuery = "";
            
            if (urlTitle != null && urlTitle.length() != 0)
            {
            	searchQuery 	+= 	"AND (LOWER(title) 	like LOWER(?)"
		            			+ 	" or LOWER(title) 	like LOWER(?)"
		            			+ 	" or LOWER(title) 	like LOWER(?)) ";
            }
            if (urlYear != null && urlYear.length() != 0)
            {
            	searchQuery 	+= 	"AND (LOWER(m.year) like LOWER(?))";
            }
            if (urlDirector != null && urlDirector.length() != 0)
            {
            	searchQuery 	+= 	"AND (LOWER(director) like 	LOWER(?)"
		            			+ 	" or LOWER(director) like 	LOWER(?)"
		            			+ 	" or LOWER(director) like 	LOWER(?)) ";
            }
            if (urlActor != null && urlActor.length() != 0)
            {
            	searchQuery 	+= 	"AND (LOWER(s.name) like 	LOWER(?)"
		            			+ 	" or LOWER(s.name) like 	LOWER(?)"
		            			+ 	" or LOWER(s.name) like 	LOWER(?)) ";
            }
            
            totalQuery += searchQuery;
            dataQuery += searchQuery + " GROUP BY m.id, r.rating ORDER BY " + urlSortBy + " LIMIT ? OFFSET ? ";
            
            PreparedStatement totalRowQuery = dbcon.prepareStatement(totalQuery);
            PreparedStatement queryStatement = dbcon.prepareStatement(dataQuery);
            
            if (urlTitle != null && urlTitle.length() != 0)
            {
            	totalRowQuery.setString(count, "%" + urlTitle + "%");
            	queryStatement.setString(count, "%" + urlTitle + "%");
            	++count;
            	totalRowQuery.setString(count, urlTitle + "%");
            	queryStatement.setString(count, urlTitle + "%");
            	++count;
            	totalRowQuery.setString(count, "%" + urlTitle);
            	queryStatement.setString(count, "%" + urlTitle);
            	++count;
            }
            if (urlYear != null && urlYear.length() != 0)
            {
            	totalRowQuery.setString(count, "%" + urlYear + "%");
            	queryStatement.setString(count, "%" + urlYear + "%");
            	++count;
            }
            if (urlDirector != null && urlDirector.length() != 0)
            {
            	totalRowQuery.setString(count, "%" + urlDirector + "%");
            	queryStatement.setString(count, "%" + urlDirector + "%");
            	++count;
            	totalRowQuery.setString(count, urlDirector + "%");
            	queryStatement.setString(count, urlDirector + "%");
            	++count;
            	totalRowQuery.setString(count, "%" + urlDirector);
            	queryStatement.setString(count, "%" + urlDirector);
            	++count;
            }
            if (urlActor != null && urlActor.length() != 0)
            {
            	totalRowQuery.setString(count, "%" + urlActor + "%");
            	queryStatement.setString(count, "%" + urlActor + "%");
            	++count;
            	totalRowQuery.setString(count, urlActor + "%");
            	queryStatement.setString(count, urlActor + "%");
            	++count;
            	totalRowQuery.setString(count, "%" + urlActor);
            	queryStatement.setString(count, "%" + urlActor);
            	++count;
            }
            
            queryStatement.setInt(count, limitInput);
            ++count;
            queryStatement.setInt(count, offsetInput);
         
            ResultSet totalRowReturned = totalRowQuery.executeQuery();
            ResultSet rs = queryStatement.executeQuery();
            
            JsonArray jsonArray = new JsonArray();
            
            
            while(totalRowReturned.next()) {
				String total_row = totalRowReturned.getString("total");
				
				JsonObject totalJsonObject = new JsonObject();
				totalJsonObject.addProperty("total",  total_row);
				
				jsonArray.add(totalJsonObject);
				
			}
            
            while (rs.next()) {
            	String id = rs.getString("id");
            	String title = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String genre = rs.getString("genre");
                String stars = rs.getString("stars");
                String rating = rs.getString("rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", id);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("genre", genre);
                jsonObject.addProperty("stars", stars);
                jsonObject.addProperty("rating", rating);
                jsonArray.add(jsonObject);

            }

            out.write(jsonArray.toString());
            
            response.setStatus(200);
            
            rs.close();
            queryStatement.close();
            totalRowQuery.close();
            totalRowReturned.close();
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
    /*
     * Full-text Search
     */
    
    protected void FullTextSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	response.setContentType("application/json"); 
        String urlTitle = request.getParameter("movie");
        PrintWriter out = response.getWriter();

        if (urlTitle.isEmpty())
        {
        	JsonObject jsonObject = new JsonObject();
        	jsonObject.addProperty("status", "fail");
			jsonObject.addProperty("message", "No Star");
			out.write(jsonObject.toString());
        }
        else 
        {
	        String urlLimit = request.getParameter("limit");
	        String urlOffset = request.getParameter("offset");
	        String urlSortBy = request.getParameter("sortby");
	        
	        if (urlSortBy == null) ;
	        else if(urlSortBy.equals("title ASC") || urlSortBy.equals("title DESC")) urlSortBy = "m." + urlSortBy;
	        else urlSortBy = "r." + urlSortBy;
	        
	
	        int limitInput = Integer.parseInt(urlLimit);
	        int offsetInput = Integer.parseInt(urlOffset);
	
	        try {
	        	
	            Connection dbcon = dataSource.getConnection();
	
	            String totalQuery = "SELECT COUNT(DISTINCT title) AS total "
	            		+ "FROM movies WHERE MATCH (movies.title)  AGAINST (? IN BOOLEAN MODE);";
	            
	            
	            String dataQuery = 	"SELECT m.id, title, year, director, GROUP_CONCAT(DISTINCT g.name) AS genre, GROUP_CONCAT(DISTINCT s.name) AS stars, r.rating\r\n" + 
	            		"FROM movies m LEFT JOIN ratings r ON m.id = r.movieId, genres_in_movies gn INNER JOIN genres g ON g.id=gn.genreId, stars_in_movies st INNER JOIN stars s ON st.starId=s.id \r\n" + 
	            		"WHERE MATCH (m.title) AGAINST (? IN BOOLEAN MODE) and m.id = gn.movieID AND st.movieID = m.id\r\n" + 
	            		"GROUP BY m.id, r.rating ORDER BY " + urlSortBy + " LIMIT ? OFFSET ? ";
	                        
	            PreparedStatement totalRowQuery = dbcon.prepareStatement(totalQuery);
	            PreparedStatement queryStatement = dbcon.prepareStatement(dataQuery);
	            
	            // Split tokens, append + to the beginning, and append * space to the end of each token
	            String[] tokens 	= urlTitle.split(" ");
	            String newUrlTitle 	= "";
	            
	            for (int i = 0; i < tokens.length; i++)
	            {
	            	newUrlTitle += " +";
	            	newUrlTitle += tokens[i];
	            	newUrlTitle += "*";
	            }
	            
            	totalRowQuery.setString(1, newUrlTitle);
            	queryStatement.setString(1, newUrlTitle);

	            queryStatement.setInt(2, limitInput);
	            queryStatement.setInt(3, offsetInput);
		            
	            ResultSet totalRowReturned = totalRowQuery.executeQuery();
	            ResultSet rs = queryStatement.executeQuery();
	            
	            JsonArray jsonArray = new JsonArray();
	            
	            
	            while(totalRowReturned.next()) {
					String total_row = totalRowReturned.getString("total");
					
					JsonObject totalJsonObject = new JsonObject();
					totalJsonObject.addProperty("total",  total_row);
					
					jsonArray.add(totalJsonObject);
					
				}
	            
	            while (rs.next()) {
	            	String id = rs.getString("id");
	            	String title = rs.getString("title");
	                String year = rs.getString("year");
	                String director = rs.getString("director");
	                String genre = rs.getString("genre");
	                String stars = rs.getString("stars");
	                String rating = rs.getString("rating");
	                
	                JsonObject jsonObject = new JsonObject();
	                jsonObject.addProperty("id", id);
	                jsonObject.addProperty("title", title);
	                jsonObject.addProperty("year", year);
	                jsonObject.addProperty("director", director);
	                jsonObject.addProperty("genre", genre);
	                jsonObject.addProperty("stars", stars);
	                jsonObject.addProperty("rating", rating);
	                jsonArray.add(jsonObject);
	
	            }
	
	            out.write(jsonArray.toString());
	            
	            response.setStatus(200);
	            
	            rs.close();
	            queryStatement.close();
	            totalRowQuery.close();
	            totalRowReturned.close();
	            dbcon.close();
	            
	        } catch (Exception e) {
	        	
				// write error message JSON object to output
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("errorMessage", e.getMessage());
				out.write(jsonObject.toString());
	
				// set reponse status to 500 (Internal Server Error)
				response.setStatus(500);
	
	        }
        }
        out.close();
    }
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	String urlfullText = request.getParameter("fullText");
    	String urlAutocomplete = request.getParameter("autocomplete");
    	
    	PrintWriter out = response.getWriter();
    	
    	try
    	{
	    	if (urlfullText == null)
	    	{
	    		if (urlAutocomplete == null || !urlAutocomplete.contains("yes"))
	    		{
	    			JsonObject jsonObject = new JsonObject();
		        	jsonObject.addProperty("status", "fail");
					jsonObject.addProperty("message", "Empty selection");
					out.write(jsonObject.toString());
	    		}
	    		
	    		if (urlAutocomplete.contains("yes"))
	    		{
	    			AutocompleteSearch(request,response);
	    		}

	    	}
	    	else if (urlfullText.contains("no"))
	    	{
	    		AdvancedSearch(request, response);
	    	}
	    	else 
	    	{
	    		System.out.println("Begin fulltext search");
	    		FullTextSearch(request,response);
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

// No need to worry about casing in fulltext search since it does it for me already. 
// No need for ; in preparedstatement
/* Why can't I use setString in preparedStatement? There may be something wrong in the 
 * autocomplete query when I edit. So I just copy the query from fulltext search to fix it. 
 */

/*
 * Using NATURAL LANGUAGE MODE does not recognize +dar* +kn* to search for dark knight. We should use boolean mode. 
 */
