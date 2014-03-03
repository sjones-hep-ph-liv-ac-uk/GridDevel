import java.sql.*;
import javax.naming.*;
import javax.sql.*;

/**
 * Example 5.1.
 */
public class Interest {
    static public void main(String[] args) {
        try {
            RowSet rs = new com.imaginary.sql.ImaginaryRowSet();

            rs.setDataSourceName("jdbc/oraxa");
            rs.setUsername("borg");
            rs.setPassword("womble");
            rs.setCommand("SELECT acct_id, balance, cust_id " +
                          "FROM account");
            rs.execute();

            Context ctx = new InitialContext();
            // this data source is pooled and distributed
            // all distributed data sources are pooled data sources
            DataSource ds = (DataSource)ctx.lookup("jdbc/oraxa");
            Connection con = ds.getConnection("borg", "");
            PreparedStatement acct, cust;

            // the account and customer tables are in two different
            // databases, but this application does not need to care
            acct = con.prepareStatement("UPDATE account " +
                                        "SET balance = ? " +
                                        "WHERE acct_id = ?");
            cust = con.prepareStatement("UPDATE customer " +
                                        "SET last_interest = ? " +
                                        "WHERE cust_id = ?");
            while( rs.next() ) {
                long acct_id, cust_id;
                double balance, interest;

                acct.clearParameters();
                cust.clearParameters();
                acct_id = rs.getLong(1);
                balance = rs.getDouble(2);
                cust_id = rs.getLong(3);
                interest = balance * (0.03/12);
                balance = balance + interest;
                acct.setDouble(1, balance);
                acct.setLong(2, acct_id);
                acct.executeUpdate();
                cust.setDouble(1, interest);
                cust.setLong(2, cust_id);
                cust.executeUpdate();
            }
            rs.close();
            con.close();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }
}

                
        
