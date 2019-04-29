var fullData = [], cachedData = [], slicedFullData = [];


/** HandleFormInfo
 * This function will 
 * 1. Prevent default action
 * 2. Serialize the form inputs and concatenate them into a string
 * 3. Redirect to the movie.html page 
 * @param cartEvent
 */
function handleFormInfo(formSubmitEvent) {
	
	formSubmitEvent.preventDefault();
    window.location.replace("movieSearch.html?"+ $("#search-form").serialize() + "&fullText=no");
}


// Bind the submit action of the form to handleFormInto function
$("#search-form").submit((event) => handleFormInfo(event));

// Handle normal search
function handleNormalSearch(query) {
	window.location.replace("movieSearch.html?movie=" + query +"&fullText=yes");
}

// Bind the enter action of user to handleNormalSearch function
$('#autocomplete').keypress(function(event) {
	if (event.keyCode == 13) {
		handleNormalSearch($('#autocomplete').val())
	}
})

// HandleLookupAjaxSuccess
function handleLookupAjaxSuccess(data, query, doneCallback) 
{
	let input = escape(query).toLowerCase().split("%20");
	let length = input.length;
	
	// Clear previous data
	fullData.length = 0;
	cachedData.length = 0;
	slicedFullData.length = 0;
	
	// Populate fullData array with new Json {value:id , data:title}
	for (let i = 0; i < data.length; i++) 
	{
		fullData[i] = data[i];
	}
	
	// Clear the cache
	cachedData.length = 0; 
	
	// Chose only relevance elements 
	for (let i = 0, j = 0; i < data.length; i++)
	{
		let newData = (data[i]["value"]).toLowerCase();
		// If newData includes all title in the input, then put it to the cache
		for (let k = 0; k < input.length; k++)
		{
			if (newData.includes(input[k]) && length > 0)
			{
				--length;
			}
			if (newData.includes(input[k]) && length == 0)
			{
				cachedData[j] = data[i];
				j++;
			}
			
		}
		length = input.length;
		
	}
	
	slicedFullData = cachedData.slice(0,10);
	// Display on the suggestion list
	doneCallback( { suggestions: slicedFullData} );
	
	// Log the new data out
	console.log("fullData" + fullData);
}

// Lookup function
function handleLookup(query, doneCallback) 
{
	console.log("autocomplete initiated");
	
	let input = escape(query).toLowerCase().split("%20");
	let movieTitle = input.join(" ");
	let length = input.length;
	
	if (fullData != null && fullData.length != 0 && Array.isArray(fullData))
	{
		// Clear the previous cached and slicedFullData
		cachedData.length = 0;
		slicedFullData.length = 0;
		
		// Search the fullData array to see if the input matches one of the data
		for (let i = 0, j = 0; i < fullData.length; i++)
		{
			var newData = (fullData[i]["value"]).toLowerCase();
			// If newData includes all title in the input, then put it to the cache
			for (let k = 0; k < input.length; k++)
			{
				if (newData.includes(input[k]) && length > 0)
				{
					--length;
				}
				if (newData.includes(input[k]) && length == 0)
				{
					cachedData[j] = fullData[i];
					j++;
				}
				
			}
			length = input.length;
		}
		
		// If there is no match, then do an ajax call
		if (cachedData.length == 0)
		{
			// Do an ajax call to MovieSearchServlet
			jQuery.ajax({
				"method": "GET",
				"url": "api/movieSearch?autocomplete=yes&title=" + input,
				"success": function(data){handleLookupAjaxSuccess(data, query, doneCallback);},
				"error": function(errorData){console.log("lookup ajax error");console.log(errorData);}
			})
		}
		else 
		{
			slicedFullData = cachedData.slice(0,10);
			doneCallback( { suggestions: slicedFullData} );
			console.log("Use cached data");
		}
	}
	else 
	{
		// Do an ajax call to MovieSearchServlet
		jQuery.ajax({
			"method": "GET",
			"url": "api/movieSearch?autocomplete=yes&title=" + input,
			"success": function(data){handleLookupAjaxSuccess(data, query, doneCallback);},
			"error": function(errorData){console.log("lookup ajax error");console.log(errorData);}
		})
	}
		
}

// Handle selection
function handleSelectSuggestion(suggestion) 
{
	window.location.replace("single-movie.html?title=" + suggestion["value"]);
}

// Autocomplete call-back 
$('#autocomplete').devbridgeAutocomplete({
	lookup: function (query, doneCallback) {
    		handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion)
    },
    deferRequestBy: 300, 
    minChars: 3
});

/*
 * DevBridge autocomplete function that has the following functions (Done)
 * 		1. minChars: 3
 * 		2. deferRequestBy: 300
 * 		3. lookup function to 
 * 			1. Console.log("Auto-complete initiated");
 * 			2. Log to the console whether I use cached results or make an ajax call to SearchMovieServlet for full-text search (Done)
 * 				1. Search inside of the fullData array to see if my input matched or not
 * 					1. For every matches, 
 * 						1. Put results in cachedData to be later trim down to 10 results and display with done(cachedData)
 * 						2. Keep count of the matches as well
 * 					2. If count is greater than 0, then
 * 						1. Assign the cachedData to slicedCachedData (let instead of var) after applying .slice(0,10)
 * 						2. Display suggestions on the search box using done(slicedCachedData)
 * 						3. Console.log("Use cached data");
 * 					3. Else, then
 * 						1. Make an ajax call to movieSearchServlet with provided parameter (Done)
 * 							1. On success, call handleLookupAjaxSuccess
 * 								1. Parse the data 
 * 								1.1 Clear the fullData array for new data set
 * 								2. Assign the data to fullData
 * 								3. Assign fullData to slicedFullData after appplying .slice(0,10)
 * 								4. done(slicedFullData)
 * 								4. Console.log("New data");
 * 							2. On failure, console.log(error)
 *  
 * 		4. select function to (Done)
 * 			1. Pass the suggestion["data"] to single-movie.html?title=suggestion["data"]
 * 						
 * 	MovieSearchServlet
 * doGet method (Done)
 * 		1. Get urlAutocomplete parameter
 * 		2. Get urlFullText parameter
 * 		3. If urlFullText is null, then check urlAutocomplete
 * 				1. Call AutocompleteSearch(request, response)
 * AutocompleteSearch (Done)
 * 		1. Get the title parameter from url
 * 		2. Set Json response type
 * 		3. Define a writer
 * 		4. Inside of the try block besides doing all the connection thingy
 * 			1. copy dataQuery string without limit and offset
 * 			2. Create a preparedStatement from that dataQuery
 * 			3. Set the first parameter to whatever input that I have 
 * 			4. Execute the query
 * 			5. Create the jSonArray
 * 			6. Iterate through the result
 * 					1. Get id and title
 * 					2. Set jsonObject as "value": id and "data": title
 * 					3. Set it into the JsonArray
 * 			7. Write the response out
 * 			8. Close all the connection
 * 		5. Catch block 
 * 
 * 						
 */
/*
 * Enter is for single movie page: Up and down and select from the list will jump to a correct single movie page
 * Search button is for normal search: 
 * index.html
 * Have a search box for full-text search
 * index.js
 * $("#fullText-form").submit((event) => handleFullTextFormInfo(event));
 * 		formSubmitEvent.preventDefault();
    	window.location.replace("movieSearch.html?"+ $("#fullText-form").serialize() + "&fullText=yes");
    	
   Let see the behavior of the final product
 */



/*
 * Alternative 1: 
 * What to cache?
 * FullData []: An array to cache all results that is returned from full-text search.
 * Cache 	[]: An array to display ten matching results in the full-text search bar. 
 * How to use the cache?
 * When autocomplete is initiated, the program will use the input to search within FullData array and increase
 * match counter and input similar results back into Temporary cache array. 
 * 		If match counter > 0, then 
 * 			Assign this temporary cache array Cache array for displaying purpose. 
 * 		Else, 
 * 		
 *  
 */
/* 
 * Bind the autocomplete action when users enter the autocomplete form
 * 		Define a function to inititate autocomplete when users first type into the search
 * 			This function will do the following
 * 				Console.log when the autocomplete initiated
 * 				For every element in the cache 
 * 				If you use previous results, then console.log("Use previous results")
 * 				Else, 
 * 					then console.log("Use new result")
 * 					Call MovieSearchServlet with new parameter, handleLookupAjaxSuccess, handleLookupAjaxFail
 * 					
 * 				
 */
/*
 * HandleLookupAjaxSuccess
 * 		Console.log("lookup ajax successful")
 * 		Parse the data
 * 		Cache the data as an array of 
 */

/*
 * Why can't i search for the? because the is the stop word in fulltext search
 * Why do I have error "value is undefined" is fullData, cachedData, and sclicedFullData? Because I start assigning value
 * from let = 1 instead of 0. So the first element is undefined. 
*/