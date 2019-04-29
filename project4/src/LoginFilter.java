import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet Filter implementation class: LoginFilter.
 * All URL patterns will go through the LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if the URL is allowed to be accessed without log in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) 
        {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        /* If request is not equal login.html or dashboardLogin.html
         * 		If request is not equal dashboard.js, html, css, and servlet, then I need to check if user has logged in
         * 			If user has logged in, then chain.doFilter(request, response)
         * 			Else, redirect user to login.html
         * 		Else, then I need to check employee has logged in
         * 			If employee has logged in, then chain.doFilter(request,response)
         * 			Else, redirect user to dashboardLogin.html
         * Else
         * 		Do nothing
         */
        if (!httpRequest.getRequestURI().contains("dashboardLogin.html") || !httpRequest.getRequestURI().equals("/project4/login.html"))
        {
        	if (!(httpRequest.getRequestURI().contains("dashboard.html") ||
        			httpRequest.getRequestURI().contains("dashboard.js") ||
        			httpRequest.getRequestURI().contains("dashboard.css")||
        			httpRequest.getRequestURI().contains("api/dashboard"))) 
        	{
        		if (httpRequest.getSession().getAttribute("user") == null) 
                {
                    httpResponse.sendRedirect("login.html");
                } 
                else 
                {
                    // If the user exists in current session, redirects the user to the corresponding URL
                    chain.doFilter(request, response);
                }
        	}
        	else 
        	{
        		if (httpRequest.getSession().getAttribute("employee") == null) 
                {
                    httpResponse.sendRedirect("dashboardLogin.html");
                } 
                else 
                {
                    // If the user exists in current session, redirects the user to the corresponding URL
                    chain.doFilter(request, response);
                }
        	}
        }
            
    }

    // Setup your own rules here to allow accessing some resources without logged in
    // Always allow your own login related requests (html, js, servlet, etc..)
    // You might also want to allow some CSS files, etc..
    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        requestURI = requestURI.toLowerCase();

        return requestURI.endsWith("style.css") || requestURI.endsWith("login.html") || requestURI.endsWith("login.js")
                || requestURI.endsWith("api/login") || requestURI.endsWith("login1.css") || requestURI.endsWith("dashboardLogin.html")
                || requestURI.endsWith("dashboardLogin.js");
    }

    /**
     * This class implements the interface: Filter. In Java, a class that implements an interface
     * must implemented all the methods declared in the interface. Therefore, we include the methods
    * below.
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) {}

    public void destroy() {}
}
