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
/**
 * Servlet implementation class BrowseServlet
 */
@WebServlet(name = "BrowseServlet", urlPatterns="/api/browse-movie")
public class BrowseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    
	@Resource(name="jdbc/moviedb")
	private DataSource dataSource;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
		response.setContentType("application/json");
		
		
		String genre = "";
		String startsWith = "";
		
		
		if(request.getParameter("genre") != null && request.getParameter("title") == null) {
			genre = request.getParameter("genre");
		}
		else if(request.getParameter("genre") == null && request.getParameter("title") != null){
			startsWith = request.getParameter("title");
		}
		else {
			;
		}
		
		String input = (request.getParameter("genre") != null)? request.getParameter("genre") : request.getParameter("title");
		
		String limit = request.getParameter("limit");
		String offset = request.getParameter("offset");
		String sortBy = request.getParameter("sortby");
		
		
		if (sortBy == null)
		{
			
		}
		else if(sortBy.equals("title ASC") || sortBy.equals("title DESC")) 
		{ 
			sortBy = "m." + sortBy;
			
		}
		else 
		{ 
			sortBy = "r." + sortBy;
			
		}
		
		
		int limitInput = Integer.parseInt(limit);
		int offsetInput = Integer.parseInt(offset);
		
		
			
		PrintWriter out = response.getWriter();
		
		try {
			Connection dbcon = dataSource.getConnection();
			
		
			String movieListTotalQuery = "SELECT COUNT(*) AS total FROM movies m;";	
		
			String genreTotal = "SELECT COUNT(*) AS total \n" +
					"FROM genres_in_movies gn LEFT JOIN genres g ON gn.genreId = g.id \n" + 
					"WHERE g.name=?;" ; 
			
			String titleTotal = "SELECT COUNT(*) AS total \n" +
					"FROM movies m WHERE m.title LIKE ? ;";
			
			String movieListQuery = "SELECT m.id, title, year, director, GROUP_CONCAT(DISTINCT g.name) AS genre, GROUP_CONCAT(DISTINCT s.name) AS stars, r.rating\n" +
					"FROM movies m LEFT JOIN ratings r ON m.id = r.movieId, genres_in_movies gn INNER JOIN genres g ON g.id=gn.genreId, stars_in_movies st INNER JOIN stars s ON st.starId=s.id \n" +
					"WHERE m.id = gn.movieID AND st.movieID = m.id GROUP BY m.id, r.rating \n" +
					"ORDER BY " + sortBy + 
					" LIMIT ? \n" +
					"OFFSET ? ;" ;
			
			String genreQuery = "SELECT m.id, title, year, director, GROUP_CONCAT(DISTINCT g.name) AS genre, GROUP_CONCAT(DISTINCT s.name) AS stars, r.rating\n" + 
					"FROM movies m LEFT JOIN ratings r ON m.id = r.movieId, genres_in_movies gn INNER JOIN genres g ON g.id=gn.genreId, stars_in_movies st INNER JOIN stars s ON st.starId=s.id \n" + 
					"WHERE m.id = gn.movieID AND st.movieID = m.id AND g.name =?\n" + 
					"GROUP BY m.id, r.rating\n" + 
					"ORDER BY " + sortBy +
					" LIMIT ? \n" +
					"OFFSET ? ;";
			
			String titleQuery = "SELECT m.id, title, year, director, GROUP_CONCAT(DISTINCT g.name) AS genre, GROUP_CONCAT(DISTINCT s.name) AS stars, r.rating\n" +
					"FROM movies m LEFT JOIN ratings r ON m.id = r.movieId, genres_in_movies gn INNER JOIN genres g ON g.id=gn.genreId, stars_in_movies st INNER JOIN stars s ON st.starId=s.id \n" +
					"WHERE m.id = gn.movieID AND st.movieID = m.id AND m.title LIKE ? GROUP BY m.id, r.rating \n" +
					"ORDER BY " + sortBy +
					" LIMIT ? \n" +
					"OFFSET ? ;" ;
			
			
			
			String totalQuery = "";
			String dataQuery = "";
			
			if(startsWith == "" && genre == "") {
				totalQuery = movieListTotalQuery;
				dataQuery = movieListQuery;
			
			}
			else {
				totalQuery = (startsWith != "" && genre == "") ? titleTotal : genreTotal;
				dataQuery = (startsWith != "" && genre == "") ? titleQuery : genreQuery;
			}
		
			
			PreparedStatement totalRowQuery = dbcon.prepareStatement(totalQuery);
			PreparedStatement statement = dbcon.prepareStatement(dataQuery);
			
			
			
			
			if(genre != "" && startsWith == "") {
				totalRowQuery.setString(1, input);
				statement.setString(1, input);
				statement.setInt(2, limitInput);
				statement.setInt(3, offsetInput);
				
			}
			else if(genre =="" && startsWith != ""){
				totalRowQuery.setString(1, input+"%");
				statement.setString(1, input+"%");
				statement.setInt(2, limitInput);
				statement.setInt(3, offsetInput);
				
			}
			else {
				statement.setInt(1, limitInput);
				statement.setInt(2, offsetInput);
				
			}
			
			
			
			ResultSet totalRowReturned = totalRowQuery.executeQuery();
			ResultSet rs = statement.executeQuery();
			
			
			
			JsonArray jsonArray = new JsonArray();
			
			while(totalRowReturned.next()) {
				String total_row = totalRowReturned.getString("total");
				
				JsonObject totalJsonObject = new JsonObject();
				totalJsonObject.addProperty("total",  total_row);
				
				jsonArray.add(totalJsonObject);
				
			}
			
			while(rs.next()){
				String id = rs.getString("id");
				String title = rs.getString("title");
				String year = rs.getString("year");
				String director = rs.getString("director");
				String a_genre = rs.getString("genre");
				String stars = rs.getString("stars");
				String rating = rs.getString("rating");
				
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("id", id);
				jsonObject.addProperty("title", title);
				jsonObject.addProperty("year", year);
				jsonObject.addProperty("director", director);
				jsonObject.addProperty("genre", a_genre);
				jsonObject.addProperty("stars", stars);
				jsonObject.addProperty("rating", rating);
				jsonArray.add(jsonObject);
				
				
			}
			out.write(jsonArray.toString());
    
            response.setStatus(200);
            
            rs.close();
            statement.close();
            totalRowQuery.close();
            totalRowReturned.close();
            dbcon.close();
			
		}catch(Exception e){
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
			
			response.setStatus(500);
		}
		
		out.close();
		
	}
	

}
