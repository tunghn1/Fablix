// Handle dragging action of headers
$( function() {
  $('#sortable').sortable({
      connectWith: ".column",
      handle: ".widget-header",
      cancel: ".fa-cog",
      placeholder: "portlet-placeholder ui-corner-all"
   });
  $('#sortable').disableSelection();
});

// Handle returned metadata from server
/*
 * Note: If using a straight string, then string may get over border. Thus, it is better to use array for metadata
 */
function handleMetadataResult(resultMetadata)
{
	resultDataJson = JSON.parse(resultMetadata);
	let metadataElement = jQuery("#table_metadata_body");
	for (let i = 1; i < resultDataJson.length; i++)
	{		
		var getColumnsArray = resultDataJson[i]["columns"];
		getColumnsArray = getColumnsArray.split(",");
		var getTypesArray = resultDataJson[i]["types"];
		getTypesArray = getTypesArray.split(",");
		
		let rowHTML = "";
		rowHTML += "<tr>";
		rowHTML += "<th>" + resultDataJson[i]["tables"] + "</th>";
		
		rowHTML += "<th>" + getColumnsArray[0];        
        for(let k = 1; k < getColumnsArray.length; k++)
        {
        	rowHTML += ", " + getColumnsArray[k];
        }
        rowHTML += "</th>";
        
        rowHTML += "<th>" + getTypesArray[0];        
        for(let k = 1; k < getTypesArray.length; k++)
        {
        	rowHTML += ", " + getTypesArray[k];
        }
        rowHTML += "</th>";

		rowHTML += "</tr>";
		
		metadataElement.append(rowHTML);
	}
}

// Function to handle star result
function handleStarResult(resultDataString)
{
	resultDataJson = JSON.parse(resultDataString);

	window.alert(resultDataJson["message"]);
}

// Function to handle movie result
function handleMovieResult(resultDataString)
{
	resultDataJson = JSON.parse(resultDataString);

	window.alert(resultDataJson["message"]);
}

// Function call for submitting star-form
function submitStarForm(formSubmitEvent) {

    formSubmitEvent.preventDefault();
    $.get(
        "api/dashboard",
        $("#star-form").serialize() + "&add=star",
        (resultDataString) => handleStarResult(resultDataString)
    );
}

// Function call for submitting movie-form
function submitMovieForm(formSubmitEvent)
{
	formSubmitEvent.preventDefault();
    $.get(
        "api/dashboard",
        $("#movie-form").serialize() + "&add=movie",
        (resultDataString) => handleMovieResult(resultDataString)
    );
}
// Function call for showing and hiding in Showing metadata tab
$("#table_metadata").hide();
document.querySelector("#Show").onclick = function() 
{
	$("#table_metadata").show();
	jQuery.ajax({ 
	    method: "POST", 
	    url: "api/dashboard",
	    success: (resultMetadata) => handleMetadataResult(resultMetadata) 
	});
}
document.querySelector("#Hide").onclick = function() 
{
	$("#table_metadata").hide();
}

//Bind the submit action of the form to a handler function
$("#star-form").submit((event) => submitStarForm(event));
$("#movie-form").submit((event) => submitMovieForm(event));

/* Note: If I don't specify the data type in ajax call, then I need to parse the returned object as Json in handler */