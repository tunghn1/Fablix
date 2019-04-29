var limit;
var offset;
var totalNumButtons;
var sortBy;

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

function searchMovie(){
	
	var fullText 	= getParameterByName("fullText");
	if (fullText == "no")
	{
		var title 			= getParameterByName("title");
		var year 			= getParameterByName("year");
		var director 		= getParameterByName("director");
		var actor 			= getParameterByName("actor");
		
		// Concatenate their values to a url
		var urlValue 	= 	"title=" 	+ title 	+ "&" + 
							"year=" 	+ year 		+ "&" + 	
							"director=" + director 	+ "&" + 
							"actor=" 	+ actor		+ "&" +
							"fullText=no";
		// Limit and Offset 
		if(limit == null) limit = 20;
		if(offset == null) offset = 0;
		if(sortBy == null) sortBy = "rating DESC";
		
		// Make an ajax call to MovieSearchServlet
		jQuery.ajax({
		    dataType: "json", 
		    method: "GET", 
		    url: "api/movieSearch?" + urlValue + "&limit=" + limit +"&offset=" + offset + "&sortby=" + sortBy,
		    success: (resultData) => handleStarResult(resultData) 
		});
	}
	else 
	{
		var movie = getParameterByName("movie");
		
		// Concatenate to url
		var urlValue = "movie=" + movie + "&" +
					   "fullText=yes";
		
		// Limit and offset
		if(limit == null) limit = 20;
		if(offset == null) offset = 0;
		if(sortBy == null) sortBy = "rating DESC";
		
		// Make an ajax call to MovieSearchServlet
		jQuery.ajax({
		    dataType: "json", 
		    method: "GET", 
		    url: "api/movieSearch?" + urlValue + "&limit=" + limit +"&offset=" + offset + "&sortby=" + sortBy,
		    success: (resultData) => handleStarResult(resultData) 
		});
	}
	
}



function handleStarResult(resultData) {
	
	console.log("handleResult: populating star info from resultData");

    // Populate the star table
    let MovieTableElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 1; i < resultData.length; i++) {

    	var getStarArray = resultData[i]["stars"];
    	getStarArray = getStarArray.split(",");
    	var getGenreArray = resultData[i]["genre"];
    	getGenreArray = getGenreArray.split(",");
 
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + '<a href="single-movie.html?title=' + resultData[i]['title'] + '">' 	+ resultData[i]["title"] + '</a>' + "</th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";
        
        rowHTML += "<th> <a href=\"movie.html?genre="+ getGenreArray[0] + "\">" + getGenreArray[0] +"</a>";
        
        for(let k = 1; k < getGenreArray.length; k++)
        {
        	rowHTML += ", <a href=\"movie.html?genre=" + getGenreArray[k] + "\">" + getGenreArray[k] + "</a>";
        }
        rowHTML += "</th>";
        
        
        
        rowHTML += "<th>" + '<a href="single-star.html?name=' + getStarArray[0] + '">' + getStarArray[0] + '</a>';
        
        for(let j = 1; j < getStarArray.length; j++)
        {
        	rowHTML += ', <a href="single-star.html?name=' + getStarArray[j] + '">'  + getStarArray[j] + '</a>';
        }
        
        rowHTML += "</th>";
        if(resultData[i]["rating"] == null)
        {
        	rowHTML += "<th>" + "N/A" + "</th>";
        }
        else
        {
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        }
        
        rowHTML += "<th>"+'<a href="cart.html?title=' + resultData[i]['title'] + '&option=add">' + "Add To Cart" + '</a>';
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        MovieTableElement.append(rowHTML);
    }
    
    let pageButton = jQuery("#page_buttons");
    
    var totalPageButtons = Number(resultData[0]["total"]);
    var totalRow = totalPageButtons/limit;
    totalNumButtons = totalRow;
    
	pageButton.append("<button id=\"page\" value='prev' onClick=toNextPage(this.value)>Prev</button>");

    
    for(let i = 1; i < totalRow+1; i++)
    {
    	pageButton.append("<button id=\"page\" value=" + i + " onClick=toNextPage(this.value)>" + i + "</button>");
    }
    
	pageButton.append("<button id=\"page\" value=\"next\" onClick=toNextPage(this.value)>Next</button>");

}

function changeViewCount(selectObject){
	var limitChosen = selectObject.value;
	limit = limitChosen;
	$("#movie_table_body").empty();
	$("#page_buttons").empty();
	
	searchMovie();	
}


function toNextPage(val)
{
	var current_offset = offset;
	var pageNum = val;
	
	if(pageNum == 'prev'){
		if(offset == 0)
		{

		}
		else
		{
			offset = offset - limit;
		}
	}
	else if(pageNum == 'next')
	{
		if(current_offset >= ((totalNumButtons - 1) * limit))
		{

		}
		else
		{
			offset = offset + limit;
		}
	}
	else 
	{
		offset = (pageNum - 1) * limit;
	}
	
	$("#movie_table_body").empty();
	$("#page_buttons").empty();
	searchMovie();
	
	
}

function sortTable(sortSelection){
	sortBy = sortSelection;
	
	$("#movie_table_body").empty();
	$("#page_buttons").empty();
	
	searchMovie();
	
}

searchMovie();




