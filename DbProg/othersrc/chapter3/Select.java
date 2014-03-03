import java.sql.*;

/**
 * Example 3.1.
 */
public class Select {
  public static void main(String args[]) {
    String url = "jdbc:msql://carthage.imaginary.com/ora";
    Connection con = null;

    try {
      String driver = "com.imaginary.sql.msql.MsqlDriver";

      Class.forName(driver).newInstance();
    }
    catch( Exception e ) {
      System.out.println("Failed to load mSQL driver.");
      return;
    }
    try {
      con = DriverManager.getConnection(url, "borg", "");
      Statement select = con.createStatement();
      ResultSet result = select.executeQuery
                          ("SELECT test_id, test_val FROM test");         

      System.out.println("Got results:");
      while(result.next()) { // process results one row at a time
        int key = result.getInt(1);
        String val = result.getString(2);

        System.out.println("key = " + key);
        System.out.println("val = " + val);
      }
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
    finally {
      if( con != null ) {
        try { con.close(); }
        catch( Exception e ) { e.printStackTrace(); }
      }
    }
  }
}
