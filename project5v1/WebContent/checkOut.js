var map;
var email;

function getParameterByName(target, formSubmit) {
    // Get request URL
	
    let url = formSubmit;
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


function handlePersonalInfoForm(formSubmitEvent){

	var firstname = getParameterByName("firstname", formSubmitEvent);
	var lastname = getParameterByName("lastname", formSubmitEvent);
	email = getParameterByName("email", formSubmitEvent);
	var ccNum = getParameterByName("ccNum", formSubmitEvent);
	var ccDate = getParameterByName("ccDate", formSubmitEvent);
	var address = getParameterByName("address", formSubmitEvent);
	
	if(firstname == null || firstname == "")
	{
		window.alert("Please type in your first name");
		return false;
	}
	if(lastname == null || lastname =="")
	{
		window.alert("Please type in your last name");
		return false;
	}
	if(email == null || email == "")
	{
		window.alert("Please type in your email");
		return false;
	}
	if(ccNum == null || ccNum =="")
	{
		window.alert("Please type in your credit card number");
		return false;
	}
	if(ccDate == null || ccDate =="")
	{
		window.alert("Please type in your credit card expiration date");
		return false;
	}
	
	
	
	
	
	jQuery.ajax({
		
		type:"POST",
		url:"api/checkout" + formSubmitEvent,
		success:(resultDataString) => handleResult(resultDataString, map)
		
	});
	
	
}


function handleResult(resultDataString, cartMap)
{
	 resultDataJson = JSON.parse(resultDataString);
	
	if(resultDataJson["result"] === "fail")
	{
		console.log(resultDataJson["result"]);
		window.alert("Customer information incorrect. Please type exact information");
		return false;
	}
	else{ 
		
	var movieParameter = "";
	
	let get_keys = cartMap.keys();
	
	
	
	movieParameter += "total=" + (cartMap.size);
	
	let i = 1;
	for(let key of get_keys)
	{
		movieParameter += "&title" + i + "=" + key + "&quantity" + i + "=" + cartMap.get(key);
		i++;
	}
	
	movieParameter += "&email=" + email;
	
	console.log("api/checkout?"+movieParameter);
	
	jQuery.ajax({
		
		type:"GET",
		url:"api/checkout?" + movieParameter,
		success:(resultDataString) => saleSuccess(resultDataString)
		
	});
	
	}
	
}

function saleSuccess(resultDataString){
	console.log(resultDataString);
	window.location.replace("confirmation.html");
	
}


function callCartServlet(){
	
	jQuery.ajax({
		type:"GET",
		url:"api/cart",
		success:(resultDataString) => handleCartArray(resultDataString)
		
	});	
}


function handleCartArray(resultDataString){
	
	 $("#cart").html("");
	if (resultDataString == null || resultDataString == "")
	{
		window.alert("Cart is empty");
	}
	else
	{
		const resultArray = resultDataString.split("|");
		map = new Map();
		
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
			}
			
			// Traverse the hash map and create the html page for cart
			let get_keys = map.keys();
	
			for (let key of get_keys)
			{
				var count = map.get(key);
				var res = "<ul>";
				res += "<li>" 
					+ "Quantity of " 					+ key + " : " + count + " "
		
					+ "</li>";
				res += "</ul>";
			    $("#cart").append(res);
	
			}
				    
		}
		
		console.log(map);
	}
}





$("#personalInfo-form").submit(function(formSubmitEvent){
	formSubmitEvent.preventDefault();
	
	var firstname = formSubmitEvent["firstname"];
	
	
	var x = $("#personalInfo-form").serialize();
	x = "?" + x;
	
	handlePersonalInfoForm(x, map);

});