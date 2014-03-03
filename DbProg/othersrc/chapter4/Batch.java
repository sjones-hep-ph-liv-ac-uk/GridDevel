import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Example 4.1.
 */
public class Batch {
    static public void main(String[] args) {
        Connection conn = null;
        
        try {
            ArrayList breakable = new ArrayList();
            PreparedStatement stmt;
            Iterator users;
            ResultSet rs;
            
            Class.forName(args[0]).newInstance();
            conn = DriverManager.getConnection(args[1], args[2], args[3]);
            stmt = conn.prepareStatement("SELECT user_id, password " +
                                         "FROM user");
            rs = stmt.executeQuery();
            while( rs.next() ) {
                String uid = rs.getString(1);
                String pw = rs.getString(2);

                // Assume PasswordCracker is some class that provides
                // a single static method called crack() that attempts
                // to run password cracking routines on the password
                if( PasswordCracker.crack(uid, pw) ) {
                    breakable.add(uid);
                }
            }
            stmt.close();
            if( breakable.size() < 1 ) {
                return;
            }
            stmt = conn.prepareStatement("UPDATE user " +
                                         "SET bad_password = 'Y' " +
                                         "WHERE uid = ?");
            users = breakable.iterator();
            while( users.hasNext() ) {
                String uid = (String)users.next();

                stmt.setString(1, uid);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        finally {
            if( conn != null ) {
                try { conn.close(); }
                catch( Exception e ) { }
            }
        }
    }
}
