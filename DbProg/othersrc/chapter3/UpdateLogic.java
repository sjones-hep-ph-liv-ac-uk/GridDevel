import java.sql.*;

/**
 * Example 3.4.
 */
public class UpdateLogic {
  public static void main(String args[]) {
    Connection con = null;

    if( args.length != 2 ) {
      System.out.println("Syntax: <java UpdateLogic [number] [string]>");
      return;
    }
    try {
      String driver = "com.imaginary.sql.msql.MsqlDriver";

      Class.forName(driver).newInstance();
      String url = "jdbc:msql://carthage.imaginary.com/ora";
      Statement s;

      con = DriverManager.getConnection(url, "borg", "");
      con.setAutoCommit(false);  // make sure auto commit is off!
      s = con.createStatement();// create the first statement
      s.executeUpdate("INSERT INTO test (test_id, test_val) " +
                      "VALUES(" + args[0] + ", '" + args[1] + "')");
      s.close();                    // close the first statement
      s = con.createStatement();    // create the second statement
      s.executeUpdate("INSERT into test_desc (test_id, test_desc) " +
                      "VALUES(" + args[0] + 
                      ", ‘This describes the test.’)");
      con.commit();                 // commit the two statements
      System.out.println("Insert succeeded.");
      s.close();                    // close the second statement
    }
    catch( Exception e ) {
      if( con != null ) {
        try { con.rollback(); }        // rollback on error 
        catch( SQLException e2 ) { }
      }
      e.printStackTrace();
    }
    finally {
      if( con != null ) {
        try { con.close(); }
        catch( SQLException e ) { e.printStackTrace(); }
      }
    }
  }
}
