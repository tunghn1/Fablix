/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    resultDataJson = JSON.parse(resultDataString);

    if (resultDataJson["status"] === "success") 
    {
        window.location.replace("index.html");
    } 
    else 
    {
    	window.alert(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {

    formSubmitEvent.preventDefault();
    $.post(
        "api/login",
        $("#login_form").serialize() + "&customer=yes",
        (resultDataString) => handleLoginResult(resultDataString)
    );
}

// Bind the submit action of the form to a handler function
$("#login_form").submit((event) => submitLoginForm(event));


/*
 * Process 1:
 * When users fill out the forms and click on submit button, 
 * 		Call SubmitLoginForm
 * 			Prevent browser from refresing.
 * 			Make an POST request to the server with three parameters, including
 * 				URL
 * 				Data attached to the URL in the form of name1=value1&name2=value2&.....
 * 				Function callback HandleLoginResult
 * When the server receives the post request,
 * 		Get all values of the fill-in form through request.getParameter("name")
 * 		Compare all values with the database
 * 			If values are present, then 
 * 				Log users into session 
 * 				Make responseJsonObject
 * 				Add attribute to it
 * 				Return the responseJsonObject as a string
 * 			Else
 * 				Make responseJsonObject
 * 				Add attribute to it to show either wrong username or password
 * 				Return the responseJsonObject as a string
 * When the result comes back to the HandleLoginResult
 * 		Parse the result into a Json object
 * 		If the result is success, then direct users to index.html page
 * 		Else display the incorrect message to div tag. 
 * 
 * 				
 * 		
 *
 */

/*
Implement log out by removing all the previousItems when login
*/