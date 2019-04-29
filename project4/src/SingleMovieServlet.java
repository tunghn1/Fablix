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

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 2L;

	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("application/json"); 
		
		String id = request.getParameter("title");
		
		PrintWriter out = response.getWriter();

		try {
			Connection dbcon = dataSource.getConnection();

			String query = "SELECT m.id,title, year, director, GROUP_CONCAT(DISTINCT g.name) AS genre, GROUP_CONCAT(DISTINCT s.name) AS stars, r.rating \n"+ 
					"FROM movies m LEFT JOIN ratings r ON m.id = r.movieId, \n"+
					"genres_in_movies gn INNER JOIN genres g ON g.id=gn.genreId, \n"+
					 "stars_in_movies st INNER JOIN stars s ON st.starId=s.id \n"+
					"WHERE m.id = gn.movieID AND st.movieID = m.id AND m.title = ? \n" +
					 "GROUP BY m.id, r.rating \n" + 
					 "ORDER BY r.rating DESC;";

			// Declare our statement
			PreparedStatement statement = dbcon.prepareStatement(query);

			statement.setString(1, id);

			ResultSet rs = statement.executeQuery();

			JsonArray jsonArray = new JsonArray();

			// Iterate through each row of rs
			while (rs.next()) {

				String movieId = rs.getString("id");
				String starId = rs.getString("stars");
				String movieTitle = rs.getString("title");
				String movieYear = rs.getString("year");
				String movieDirector = rs.getString("director");
				String movieGenre = rs.getString("genre");
				String movieRating = rs.getString("rating");

				// Create a JsonObject based on the data we retrieve from rs

				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("id", movieId);
				jsonObject.addProperty("title", movieTitle);
				jsonObject.addProperty("year", movieYear);
				jsonObject.addProperty("director", movieDirector);
				jsonObject.addProperty("genre", movieGenre);
				jsonObject.addProperty("stars", starId);
				jsonObject.addProperty("rating", movieRating);
	
				jsonArray.add(jsonObject);
			}

            out.write(jsonArray.toString());
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
