import com.google.gson.JsonArray;
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

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
	private static final long serialVersionUID = 2L;

	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json"); // Response mime type

		// Retrieve parameter id from url request.
		String name = request.getParameter("name");

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {
			// Get a connection from dataSource
			Context initCtx = new InitialContext();
            
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if(envCtx == null) 
            	out.println("envCtx is Null");
            
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");
            
            if(ds == null)
            	out.println("ds is null.");
            
            Connection dbcon = ds.getConnection();
            if(dbcon == null) out.println("dbcon is null");

			// Construct a query with parameter represented by "?"
			String query = "SELECT s.id AS starId, s.name, s.birthYear, GROUP_CONCAT(DISTINCT m.title) as movieList\n" + 
					"FROM stars_in_movies sm JOIN movies m ON sm.movieId = m.id, stars s\n" + 
					"WHERE s.id = sm.starId AND m.id = sm.movieId AND s.name =  ?  \n" + 
					"GROUP BY s.id;";

			// Declare our statement
			PreparedStatement statement = dbcon.prepareStatement(query);

			// Set the parameter represented by "?" in the query to the id we get from url,
			// num 1 indicates the first "?" in the query
			statement.setString(1, name);

			// Perform the query
			ResultSet rs = statement.executeQuery();

			JsonArray jsonArray = new JsonArray();

			// Iterate through each row of rs
			while (rs.next()) {

				String starId = rs.getString("starId");
				String starName = rs.getString("name");
				String starDob = rs.getString("birthYear");
				String movieList = rs.getString("movieList");
				System.out.print(starDob);

				// Create a JsonObject based on the data we retrieve from rs

				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("star_id", starId);
				jsonObject.addProperty("star_name", starName);
				jsonObject.addProperty("star_dob", starDob);
				
				jsonObject.addProperty("star_dob", starDob);
				
				jsonObject.addProperty("movie_list", movieList);

				jsonArray.add(jsonObject);
			}
			
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

			rs.close();
			statement.close();
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

}
