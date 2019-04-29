var limit;
var offset;
var totalNumButton;
var sortBy;

function getParameterByName(target) {
    // Get request URL
	
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

function browseMovie(){
	let genreType = getParameterByName('genre');
	let titleType = getParameterByName('title');
	
	
	if(limit == null){
		limit = 20;
	}
	if(offset == null){
		offset = 0;
	}
	if(sortBy == null){
		sortBy = "rating DESC";
	}
	
	var urlString = "api/browse-movie?";

	

	if(genreType != null && titleType == null){
		
		urlString += "genre=" + genreType + "&limit=" + limit + "&offset=" + offset + "&sortby=" +sortBy;
		
		
	}
	else if(genreType == null && titleType != null){
		
		urlString += "title=" + titleType +"&limit=" + limit + "&offset=" + offset + "&sortby=" + sortBy;
		
		
	}
	else{
		
		urlString += "limit=" + limit + "&offset=" + offset + "&sortby=" + sortBy;
		
	}
	
	console.log(urlString);
	
	jQuery.ajax({
	    dataType: "json",  
	    method: "GET",
	    url: urlString,
	    success: (resultData) => handleResult(resultData) 
	});
	
	
	
}

function handleResult(resultData){
	
	
	let GenreMovieTableElement = jQuery("#movie_table_body");
	
	for (let i = 1; i < resultData.length; i++) {

    	var getStarArray = resultData[i]["stars"];
    	getStarArray = getStarArray.split(",");
    	var getGenreArray = resultData[i]["genre"];
    	getGenreArray = getGenreArray.split(",");
 
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]['id']+"</th>"
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
        { rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        }
        
        rowHTML += "<th>"+'<a href="cart.html?title=' + resultData[i]['title'] + '&option=add">' + "Add To Cart" + '</a>';
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        GenreMovieTableElement.append(rowHTML);
    }
	
	let pageButton = jQuery("#page_buttons");
	
	var totalPageButton = Number(resultData[0]["total"]);
	var totalRow = totalPageButton/limit;
	totalNumButtons = totalRow
	
	pageButton.append("<button id=\"page\" value='prev' onClick=toNextPage(this.value)>Prev</button>");
	
	for(let i = 1; i < totalRow+1; i++){
		pageButton.append("<button id=\"page\" value=" + i + " onClick=toNextPage(this.value)>" + i +"</button>");
	}
	
	pageButton.append("<button id=\"page\" value=\"next\" onClick=toNextPage(this.value)>Next</button>");

	
}





function changeViewCount(selectObject){
	var limitChosen = selectObject.value;
	limit = limitChosen;
	$("#movie_table_body").empty();
	$("#page_buttons").empty();
	browseMovie();
	
	
}




function toNextPage(val){
	var current_offset = offset;
	var pageNum = val;
	if(pageNum == 'prev'){
		if(offset == 0){
			;
		}
		else{
		offset = offset - limit;
		}
	}
	else if(pageNum == 'next'){
		if(current_offset >= ((totalNumButtons - 1) * limit)){
			;
		}
		else{
		offset = offset + limit;
		}
	}
	else {offset = (pageNum - 1) * limit;}
	
	
	$("#movie_table_body").empty();
	$("#page_buttons").empty();

	browseMovie();
	
}

function sortTable(sortSelection){
	var genre = getParameterByName('genre');
	var title = getParameterByName('title');
	sortBy = sortSelection;
	
	$("#movie_table_body").empty();
	$("#page_buttons").empty();
	

	browseMovie();
}




browseMovie();

