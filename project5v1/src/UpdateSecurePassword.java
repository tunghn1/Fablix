import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class UpdateSecurePassword {

    /*
     * 
     * This program updates your existing moviedb customers table to change the
     * plain text passwords to encrypted passwords.
     * 
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     * 
     * Customers table
     * Change the customers table password column from VARCHAR(20) to VARCHAR(128)
     * Get the ID and password for each customer
     * Use the StrongPasswordEncryptor from jasypt library to calculate the encrypted password
     * Execute the statement
     * Traverse through the result
     * 		Get the ID and plain text password from current table
     * 		Encrypt the password using StrongPasswordEncryptor
     * 		Generate the update query and put it onto an array list of update queries
     * Traverse through the update query array list
     * 		Execute the update queries to update the password
     * Close result set and statement
     * 
     * Employees table: email varchar(50) and password varchar(20)
     * Change the employees table password column from VARCHAR(20) to VARCHAR(128)            
     * Get the email and password for each employees                                              
     * Use the StrongPasswordEncryptor from jasypt library to calculate the encrypted password
     * Execute the statement                                                                  
     * Traverse through the result                                                            
     * 		Get the email and plain text password from current table                             
     * 		Encrypt the password using StrongPasswordEncryptor                                
     * 		Generate the update query and put it onto an array list of update queries         
     * Traverse through the update query array list                                           
     * 		Execute the update queries to update the password                                 
     * Close result set, statement, and connection
     * 
     * 
     */
    public static void main(String[] args) throws Exception {

        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        
        Statement statement = connection.createStatement();
        
        String alterQuery = "ALTER TABLE customers MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        
        System.out.println("altering customers table schema completed, " + alterResult + " rows affected");
        
        String query = "SELECT id, password from customers";

        ResultSet rs = statement.executeQuery(query);
        
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        
        ArrayList<String> updateQueryList = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        
        while (rs.next()) {
            String id = rs.getString("id");
            String password = rs.getString("password");
            
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            String updateQuery = String.format("UPDATE customers SET password='%s' WHERE id=%s;", encryptedPassword, id);
            updateQueryList.add(updateQuery);
        }
        rs.close();

        System.out.println("updating password");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");

        statement.close();
        
        Statement statementEmployees = connection.createStatement();
        String alterEmployees = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
        int employeesResult = statementEmployees.executeUpdate(alterEmployees); 
        System.out.println("altering employees table schema completed, " + employeesResult + " rows affected");
        String employeesQuery = "SELECT * from employees";
        ResultSet employeesRS = statementEmployees.executeQuery(employeesQuery);
        PasswordEncryptor employeesPasswordEncryptor = new StrongPasswordEncryptor();
        ArrayList<String> employeesUpdateQueryList = new ArrayList<>();
        
        System.out.println("encrypting password (this might take a while)");
        
        while (employeesRS.next()) {
            String email = employeesRS.getString("email");
            String password = employeesRS.getString("password");
            
            String encryptedPassword = employeesPasswordEncryptor.encryptPassword(password);

            String updateQuery = String.format("UPDATE employees SET password='%s' WHERE email='%s';", encryptedPassword, email);
            employeesUpdateQueryList.add(updateQuery);
        }
        employeesRS.close();

        System.out.println("updating password");
        int employeesCount = 0;
        for (String updateQuery : employeesUpdateQueryList) {
            int updateResult = statementEmployees.executeUpdate(updateQuery);
            employeesCount += updateResult;
        }
        System.out.println("updating password completed, " + employeesCount + " rows affected");
        statementEmployees.close();

        connection.close();

        System.out.println("finished");

    }

}
