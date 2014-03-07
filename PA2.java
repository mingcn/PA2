/*
 * JDBC Project, March 7, 2014
 * Group Members:
 * (1) Thaddeus Trinh, A09412517 
 * (2) Jayon Huh, 
 * (3) David Muller, A09252886
 */


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
		
        Statement stmt = conn.createStatement();
		try
		{
            // drop all of our helper tables
			stmt.executeUpdate("Drop TABLE IF EXISTS QuartersToGraduation");
			stmt.executeUpdate("DROP TABLE IF EXISTS zero;");
			stmt.executeUpdate("DROP TABLE IF EXISTS all_courses;");
			stmt.executeUpdate("DROP TABLE IF EXISTS all_names;");
			stmt.executeUpdate("DROP TABLE IF EXISTS all_names_and_courses;");
			stmt.executeUpdate("DROP TABLE IF EXISTS graduated_students;");
			stmt.executeUpdate("DROP TABLE IF EXISTS not_taken;");
			stmt.executeUpdate("DROP TABLE IF EXISTS courses_to_take;");

            
            // create QuartersToGraduation Table. initialize with (Student, 0) for all Students
			stmt.executeUpdate("CREATE TABLE QuartersToGraduation (Student char(32), Quarters int);");
			stmt.executeUpdate("CREATE TABLE zero (zero int);");
			stmt.executeUpdate("INSERT INTO zero VALUES(0);");
			int i = stmt.executeUpdate("INSERT INTO QuartersToGraduation select distinct r.Student, z.zero from Record r, zero z;");

            
            // create a table with the names of every student, and every course available
			stmt.executeUpdate("CREATE TABLE all_courses (Course char(32));");
			stmt.executeUpdate("INSERT INTO all_courses SELECT * FROM Core;");
			stmt.executeUpdate("INSERT INTO all_courses SELECT * FROM Elective;");

			stmt.executeUpdate("CREATE TABLE all_names (Student char(32));");
			stmt.executeUpdate("INSERT INTO all_names SELECT DISTINCT Student FROM Record");

			stmt.executeUpdate("CREATE TABLE all_names_and_courses (Student char(32), Course char(32));");
			stmt.executeUpdate("INSERT INTO all_names_and_courses SELECT * FROM all_names, all_courses;");

                
			while(numberOfUpdates != 0)
			{
                // drop/create all our helper tables, so we have clean versions for this loop iteration
			    stmt.executeUpdate("DROP TABLE IF EXISTS graduated_students;");
			    stmt.executeUpdate("CREATE TABLE graduated_students (Student char(32));");
			    stmt.executeUpdate("DROP TABLE IF EXISTS not_taken;");
			    stmt.executeUpdate("CREATE TABLE not_taken (Student char(32), Course char(32));");
			    stmt.executeUpdate("DROP TABLE IF EXISTS courses_to_take;");
			    stmt.executeUpdate("CREATE TABLE courses_to_take (Student char(32), Course char(32));");

			    // find all graduated students
                stmt.executeUpdate("INSERT INTO graduated_students " +
								    " SELECT distinct student " +
								    " from Record r1 " +
								    " where " +
									    " (select count(*) " +
									    " from Record r2 " +
									    " where r1.Student = r2.Student " +
									    " and r2.Course in " +
										    " (select Course from Elective)) > 4" +
									" AND " +
									    " (select count(*) " +
									    " from Record r2 " +
									    " where r1.Student = r2.Student " +
									    " and r2.course in " +
									 	    " (select Course from core)) = (select count(*) from Core);");

                // find all courses that haven't been taken this quarter
			    stmt.executeUpdate("INSERT INTO not_taken " +
							   	    "SELECT * FROM all_names_and_courses " +
							   	    "EXCEPT SELECT * FROM record;");

                // insert into courses_to_take (Student, Course) tuples where the Student
                // has not Graduated and the course Prereqs are satisified (or there are no Prereqs)
			    stmt.executeUpdate("INSERT INTO courses_to_take " +
								    " SELECT DISTINCT nt.Student, nt.Course " +
								    " FROM Prerequisite p1, not_taken nt " + 
								    " WHERE nt.Course NOT IN (select Course from Prerequisite)" +
                                    " OR nt.Student NOT IN graduated_students" +
                                    " AND p1.Course = nt.Course AND NOT EXISTS " + 
									    " (SELECT * " + 
									    " FROM Prerequisite p2 " +
									    " WHERE p2.Course = p1.Course AND NOT EXISTS " +
										    " (SELECT * " + 
										    " FROM Record r " + 
										    " WHERE nt.student = r.Student AND p2.Prereq = r.Course));");


                // update Record with all courses to be taken this quarter
			    stmt.executeUpdate("INSERT INTO record SELECT * FROM courses_to_take;");

                // add 1 to Quarters to graduate for every Student who took courses this quarter
			    numberOfUpdates = stmt.executeUpdate("UPDATE QuartersToGraduation SET Quarters = Quarters + 1 " + 
													"WHERE Student IN (SELECT Student FROM courses_to_take);");

                // drop helper tables
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



