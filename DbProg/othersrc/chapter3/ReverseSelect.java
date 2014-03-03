import java.sql.*;
import java.util.*;

/**
 * Example 3.5.
 */
public class ReverseSelect {
  public static void main(String argv[]) {
    Connection con = null;

    try {
      String url = "jdbc:msql://carthage.imaginary.com/ora";
      String driver = "com.imaginary.sql.msql.MsqlDriver";
      Properties p = new Properties();
      Statement stmt;
      ResultSet rs;
            
      p.put("user", "borg");
      Class.forName(driver).newInstance();
      con = DriverManager.getConnection(url, "borg", "");
      stmt = 
      con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                          ResultSet.CONCUR_READ_ONLY);
       rs = stmt.executeQuery("SELECT * from test ORDER BY test_id");
      // as a new ResultSet, rs is currently positioned
      // before the first row
      System.out.println("Got results:");
      // position rs after the last row
      rs.afterLast();
      while(rs.previous()) {
        int a= rs.getInt("test_id");
        String str = rs.getString("test_val");

        System.out.print("\ttest_id= " + a);
        System.out.println("/str= '" + str + "'");
      }
      System.out.println("Done.");
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
