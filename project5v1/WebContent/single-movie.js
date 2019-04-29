/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
	console.log("here");
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleResult(resultData) {
	
    let SingleMovieTableElement = jQuery("#single_movie_table_body");

    for (let i = 0; i < Math.min(20, resultData.length); i++) 
    {
    	var getStarArray = resultData[i]["stars"];
    	getStarArray = getStarArray.split(",");
    	var getGenreArray = resultData[i]["genre"];
    	getGenreArray = getGenreArray.split(",");
    	
    	
    	let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" 					+ resultData[i]["id"] 		+ "</th>";
        rowHTML += "<th>" 				 	+ resultData[i]["title"] 	+ "</th>";
        rowHTML += "<th>" 					+ resultData[i]["year"] 	+ "</th>";
        rowHTML += "<th>" 					+ resultData[i]["director"] + "</th>";
        
        rowHTML += "<th> <a href=\"movie.html?genre="+ getGenreArray[0] + "\">" + getGenreArray[0] +"</a>";
        
        for(let k = 1; k < getGenreArray.length; k++)
        {
        	rowHTML += ", <a href=\"movie.html?genre=" + getGenreArray[k] + "\">" + getGenreArray[k] + "</a>";
        }
        rowHTML += "</th>";
        
        rowHTML += "<th>"+'<a href="single-star.html?name=' + getStarArray[0] + '">' + getStarArray[0] + '</a>';
       
        
        for(let j = 1; j < getStarArray.length; j++)
        {
        	rowHTML += ', <a href="single-star.html?name=' + getStarArray[j] + '">'  + getStarArray[j] + '</a>';
        }
        rowHTML += "</th>";
        rowHTML += "<th>" 					+ resultData[i]["rating"] 	+ "</th>";
        rowHTML += "<th>"+'<a href="cart.html?title=' + resultData[i]['title'] + '&option=add">' + "Add To Cart" + '</a>';
        rowHTML += "</tr>";

        SingleMovieTableElement.append(rowHTML);
    }
}

let movieName = getParameterByName('title');

jQuery.ajax({
    dataType: "json",  
    method: "GET",
    url: "api/single-movie?title=" + movieName, 
    success: (resultData) => handleResult(resultData) 
});