import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.SQLException; 
import java.sql.Statement; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 

public class PA2 {
public static void main(String[] args) {
	Connection conn = null;
	int numberOfUpdates = 1;
	try 
	{
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:pa2.db");
		System.out.println("opened database successfully");
		System.out.println();

		Statement stmt = conn.createStatement();
		try
		{
			System.out.println("Creating QuartersToGraduation table");
			stmt.executeUpdate("Drop TABLE IF EXISTS QuartersToGraduation");
			stmt.executeUpdate("DROP TABLE IF EXISTS zero;");
			stmt.executeUpdate("DROP TABLE IF EXISTS all_courses;");
			stmt.executeUpdate("DROP TABLE IF EXISTS all_names;");
			stmt.executeUpdate("DROP TABLE IF EXISTS all_names_and_courses;");
			stmt.executeUpdate("DROP VIEW IF EXISTS graduated_students;");
			stmt.executeUpdate("DROP VIEW IF EXISTS not_taken;");
			stmt.executeUpdate("DROP VIEW IF EXISTS courses_to_take;");

			stmt.executeUpdate("CREATE TABLE QuartersToGraduation (student VARCHAR(20), QuartersToGraduation int);");
			stmt.executeUpdate("CREATE TABLE zero (zero int);");
			stmt.executeUpdate("INSERT INTO zero VALUES(0);");

			int i = stmt.executeUpdate("INSERT INTO QuartersToGraduation select distinct r.student, z.zero from record r, zero z;");


			stmt.executeUpdate("CREATE TABLE all_courses (course char(32));");
			stmt.executeUpdate("INSERT INTO all_courses SELECT * FROM core;");
			stmt.executeUpdate("INSERT INTO all_courses SELECT * FROM elective;");

			stmt.executeUpdate("CREATE TABLE all_names (student char(32));");
			stmt.executeUpdate("INSERT INTO all_names SELECT DISTINCT STUDENT FROM RECORD");

			stmt.executeUpdate("CREATE TABLE all_names_and_courses (student char(32), course char(32));");
			stmt.executeUpdate("INSERT INTO all_names_and_courses SELECT * FROM all_names, all_courses;");



			stmt.executeUpdate("CREATE view graduated_students as " +
								" SELECT distinct student " +
								" from record r1 " +
								" where " +
									" (select count(*) " +
									" from record r2 " +
									" where r1.student = r2.student " +
									" and r2.course in " +
										" (select course from elective)) > 4" +
									" AND " +
									" (select count(*) " +
									 " from record r2 " +
									 " where r1.student = r2.student " +
									 " and r2.course in " +
									 	" (select course from core)) = (select count(*) from core);");

			stmt.executeUpdate("CREATE VIEW not_taken AS " +
							   	"SELECT * FROM all_names_and_courses " +
							   	"EXCEPT SELECT * FROM record;");

			stmt.executeUpdate("CREATE VIEW courses_to_take AS " +
								" SELECT DISTINCT nt.student, nt.course " +
								" FROM Prerequisite p1, not_taken nt " + 
								" WHERE nt.student NOT IN graduated_students AND p1.course = nt.course AND NOT EXISTS " + 
									" (SELECT * " + 
									" FROM Prerequisite p2 " +
									" WHERE p2.course = p1.course AND NOT EXISTS " +
										" (SELECT * " + 
										" FROM Record r " + 
										" WHERE nt.student = r.student AND p2.Prereq = r.course));");

			stmt.executeUpdate("INSERT INTO record SELECT * FROM courses_to_take;");

			numberOfUpdates = stmt.executeUpdate("UPDATE QuartersToGraduation SET QuartersToGraduation = QuartersToGraduation + 1 " + 
													"WHERE student IN (SELECT student FROM courses_to_take);");

			System.out.println(numberOfUpdates);


		}
		catch(SQLException e) {e.printStackTrace();}
	

		/*System.out.println("this is i " + i);

		try 
		{
			i = stmt.executeUpdate("DELETE FROM Core where course = 'CSE101'");
			System.out.println("this is i " + i);
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}*/

		//execute update returns an int of the number of rows you updated, who cares

		System.out.println();

		// query the database
		ResultSet rset = stmt.executeQuery("SELECT * FROM all_courses");
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

		//TODO: Create Helper Tables
		//Students - Copy distinct name from Records, delete name from table as we are doing calculations
		//courses_taken - find the courses a student has taken using Records and Students
		//not_taken_core - find the core classes the student has not taken from Core and courses_taken
		//not_taken_elective - 

		//TODO: Write a loop 

			//how to choose one random student : "select * from students limit 1"

		//TODO: Drop Tables
		

	} 
	catch (Exception e) 
	{
		try 
		{
			conn.close();
		} 
		catch(SQLException e1) 
		{
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






