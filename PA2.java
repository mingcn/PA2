import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.SQLException; 
import java.sql.Statement; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 

public class PA2 {
public static void main(String[] args) {
	Connection conn = null;
	try {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:pa2.db");
		System.out.println("opened database successfully");

		Statement stmt = conn.createStatement();
		stmt.executeUpdate("Drop TABLE IF EXISTS Core");
		stmt.executeUpdate("CREATE TABLE Core (course VARCHAR(20))");
		stmt.executeUpdate("INSERT INTO Core VALUES ('CSE101'), ('CSE132A');");

		stmt.executeUpdate("Drop TABLE IF EXISTS Elective");
		stmt.executeUpdate("CREATE TABLE Elective (course VARCHAR(20))");
		stmt.executeUpdate("INSERT INTO Elective VALUES ('ECE101'), ('ECE100');");

		stmt.executeUpdate("Drop TABLE IF EXISTS Prereq");
		stmt.executeUpdate("CREATE TABLE Prereq (course VARCHAR(20), prereq VARCHAR(20))");
		stmt.executeUpdate("INSERT INTO Prereq VALUES ('CSE132a, CSE101'), ('ECE101, ECE100');");

		//execute update returns an int of the number of rows you updated, who cares

		// query the database
		ResultSet rset = stmt.executeQuery("SELECT * FROM Core");
		System.out.println("Statement Query result");
		while (rset.next()) {
			System.out.println("" + rset.getString("course"));
		}
		// prepared statement
		/*PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM aa WHERE a == ? OR b == ?");
		pstmt.setString(1, "1");
		pstmt.setString(2, "200");
		rset = pstmt.executeQuery();
		System.out.println("Prepared Statement Query Result");
		while(rset.next()) {
			System.out.println("" + rset.getInt("a") + "---" +rset.getInt("b"));
		}*/


		stmt.close();
		rset.close();
		//pstmt.close();
		

	} catch (Exception e) {
		try {
			conn.close();
		} catch(SQLException e1) {
			e1.printStackTrace();
		}
	}

}
}

/* "sqlite pa2.db" loads it as a database 
     useful for debugging

    can assume that the data bases are already initialized

    don't print out the lines to console except for debugging

	Compile
	java -cp .:sqlite-jdbc-3.7.15-M1.jar PA2

 */






