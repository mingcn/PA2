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
			stmt.executeUpdate("DROP TABLE IF EXISTS graduated_students;");
			stmt.executeUpdate("DROP TABLE IF EXISTS not_taken;");
			stmt.executeUpdate("DROP TABLE IF EXISTS courses_to_take;");

			stmt.executeUpdate("CREATE TABLE QuartersToGraduation (student VARCHAR(20), QuartersToGraduation int);");
			stmt.executeUpdate("CREATE TABLE zero (zero int);");
			stmt.executeUpdate("INSERT INTO zero VALUES(0);");

			int i = stmt.executeUpdate("INSERT INTO QuartersToGraduation select distinct r.student, z.zero from record r, zero z;");

            // create a table with the names of every student, and every course available
			stmt.executeUpdate("CREATE TABLE all_courses (Course VARCHAR(20));");
			stmt.executeUpdate("INSERT INTO all_courses SELECT * FROM Core;");
			stmt.executeUpdate("INSERT INTO all_courses SELECT * FROM Elective;");

			stmt.executeUpdate("CREATE TABLE all_names (Student VARCHAR(20));");
			stmt.executeUpdate("INSERT INTO all_names SELECT DISTINCT Student FROM Record");

			stmt.executeUpdate("CREATE TABLE all_names_and_courses (Student VARCHAR(20), Course VARCHAR(20));");
			stmt.executeUpdate("INSERT INTO all_names_and_courses SELECT * FROM all_names, all_courses;");

			while(numberOfUpdates != 0)
			{

			stmt.executeUpdate("DROP TABLE IF EXISTS graduated_students;");
			stmt.executeUpdate("CREATE TABLE graduated_students (Student VARCHAR(20));");
			stmt.executeUpdate("DROP TABLE IF EXISTS not_taken;");
			stmt.executeUpdate("CREATE TABLE not_taken (Student VARCHAR(20), Course VARCHAR(20));");
			stmt.executeUpdate("DROP TABLE IF EXISTS courses_to_take;");
			stmt.executeUpdate("CREATE TABLE courses_to_take (Student VARCHAR(20), Course VARCHAR(20));");

			stmt.executeUpdate("INSERT INTO graduated_students " +
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

			/*stmt.executeUpdate("CREATE view graduated_students as " +
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
									 	" (select course from core)) = (select count(*) from core);");*/

			stmt.executeUpdate("INSERT INTO not_taken " +
							   	"SELECT * FROM all_names_and_courses " +
							   	"EXCEPT SELECT * FROM record;");

			/*stmt.executeUpdate("CREATE VIEW not_taken AS " +
							   	"SELECT * FROM all_names_and_courses " +
							   	"EXCEPT SELECT * FROM record;");*/

			stmt.executeUpdate("INSERT INTO courses_to_take " +
								" SELECT DISTINCT nt.student, nt.course " +
								" FROM Prerequisite p1, not_taken nt " + 
								" WHERE nt.course NOT IN (select course from prerequisite) OR nt.student NOT IN graduated_students AND p1.course = nt.course AND NOT EXISTS " + 
									" (SELECT * " + 
									" FROM Prerequisite p2 " +
									" WHERE p2.course = p1.course AND NOT EXISTS " +
										" (SELECT * " + 
										" FROM Record r " + 
										" WHERE nt.student = r.student AND p2.Prereq = r.course));");

			/*stmt.executeUpdate("CREATE VIEW courses_to_take AS " +
								" SELECT DISTINCT nt.student, nt.course " +
								" FROM Prerequisite p1, not_taken nt " + 
								" WHERE nt.student NOT IN graduated_students AND p1.course = nt.course AND NOT EXISTS " + 
									" (SELECT * " + 
									" FROM Prerequisite p2 " +
									" WHERE p2.course = p1.course AND NOT EXISTS " +
										" (SELECT * " + 
										" FROM Record r " + 
										" WHERE nt.student = r.student AND p2.Prereq = r.course));");*/

			/*ResultSet rset = stmt.executeQuery("SELECT * FROM courses_to_take");
			System.out.println("Statement Query result");
			while (rset.next()) {
			System.out.println("" + rset.getString("student") + " " + rset.getString("course"));
			}*/

			stmt.executeUpdate("INSERT INTO record SELECT * FROM courses_to_take;");

			numberOfUpdates = stmt.executeUpdate("UPDATE QuartersToGraduation SET QuartersToGraduation = QuartersToGraduation + 1 " + 
													"WHERE student IN (SELECT student FROM courses_to_take);");

			//System.out.println(numberOfUpdates);

			/*ResultSet rset = stmt.executeQuery("SELECT * FROM QuartersToGraduation");
			System.out.println("Statement Query result");
			while (rset.next()) {
			System.out.println("" + rset.getString("student") + " " + rset.getInt("QuartersToGraduation"));
			}*/

			stmt.executeUpdate("DROP TABLE IF EXISTS graduated_students;");
			stmt.executeUpdate("DROP TABLE IF EXISTS not_taken;");
			stmt.executeUpdate("DROP TABLE IF EXISTS courses_to_take;");
			}


		}
		catch(SQLException e) {e.printStackTrace();}
	
		stmt.close();

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







