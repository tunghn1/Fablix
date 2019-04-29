function callCartServlet(){
	
	jQuery.ajax({
		type:"GET",
		url:"api/cart",
		success:(resultDataString) => handleCartArray(resultDataString)
		
	});	
}

function handleCartArray(resultDataString){
	
	 $("#purchase").html("");
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
			    $("#purchase").append(res);
	
			}
				    
		}
		
		console.log(map);
	}
}
