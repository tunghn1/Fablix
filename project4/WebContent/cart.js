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

function callCartServlet(title, option)
{
	if (option == null)
	{
		jQuery.ajax(
		{
			type: "GET",
			url: "api/cart",
			success: (resultDataString) => handleCartArray(resultDataString)
		});
	}
	else 
	{
		var urlString = "?title=" + title + "&option=" + option;
		jQuery.ajax(
		{
			type: "GET",
			url: "api/cart" + urlString,
			success: (resultDataString) => handleCartArray(resultDataString)
		});
	}
	
}

function handleCartArray(resultDataString)
{
    $("#cart").html("");
    
	if (resultDataString == null || resultDataString == "")
	{
		window.alert("Cart is empty");
	}
	else
	{
		const resultArray = resultDataString.split("|");
		console.log(resultArray);
		var map = new Map();
		
		if (resultArray[0] == "")
		{
		
		}
		else 
		{
			// Traverse through the array and hash distinct titles and count into a map
			for (let i = 0; i < resultArray.length; i++)
			{
				// If the item existed, then update its value
				if (map.has(resultArray[i]))
				{
					var newValue = map.get(resultArray[i]) + 1;
					map.set(resultArray[i], newValue);
				}
				else 
				{
					map.set(resultArray[i], 1);
				}
				console.log("value of " + resultArray[i] + " is " + map.get(resultArray[i]));
			}
			
			// Traverse the hash map and create the html page for cart
			let get_keys = map.keys();
	
			for (let key of get_keys)
			{
				var count = map.get(key);
				var res = "<ul>";
				res += "<li>" 
					+ "Quantity of " 					+ key + " is " + count + " "
					+ "<button class='add' 		name='" + key + "'> Add 		</button>"
					+ "<button class='subtract' name='" + key + "'> Subtract 	</button>"
					+ "<button class='remove' 	name='" + key + "'> Remove		</button>"
					+ "</li>";
				res += "</ul>";
				console.log("key " + key + " has value of " + count);
			    $("#cart").append(res);
	
			}
				    
		}
	}
}
let option = getParameterByName("option");
let title = getParameterByName("title");


$(document).ready(callCartServlet(title, option));

$("#cart").on("click", ".add", function(event)
{
	let name = $(this).attr("name");
	let operation = "add";
//	event.preventDefault();
	callCartServlet(name, operation);
	
});

$("#cart").on("click", ".subtract", function(event)
{
	let name = $(this).attr("name");
	let operation = "subtract";
//	event.preventDefault();
	callCartServlet(name, operation);
	
});

$("#cart").on("click", ".remove", function(event)
{
	let name = $(this).attr("name");
	let operation = "remove";
//	event.preventDefault();
	callCartServlet(name, operation);
	
});
