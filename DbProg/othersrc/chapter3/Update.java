import java.sql.*;

/**
 * Example 3.3
 */
public class Update {
  public static void main(String args[]) {
    Connection con = null;

    if( args.length != 2 ) {
      System.out.println("Syntax: <java UpdateApp [number] [string]>");
      return;
    }
    try {
      String driver = "com.imaginary.sql.msql.MsqlDriver";

      Class.forName(driver).newInstance();
      String url = "jdbc:msql://carthage.imaginary.com/ora";
      con = DriverManager.getConnection(url, "borg", "");
      Statement s = con.createStatement();
      String test_id = args[0];
      String test_val = args[1];
      int update_count = 
        s.executeUpdate("INSERT INTO test (test_id, test_val) " +
                        "VALUES(" + test_id + ", '" + test_val + "')");

      System.out.println(update_count + " rows inserted.");
      s.close();
    }
    catch( Exception e ) {
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
