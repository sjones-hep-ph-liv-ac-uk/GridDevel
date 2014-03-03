import java.sql.*;
import java.io.*;

/**
 * Example 4.2.
 */
public class Blobs {
    public static void main(String args[]) {
        if( args.length != 1 ) {
            System.err.println("Syntax: <java Blobs [driver] [url] " +
                               "[uid] [pass] [file]");
            return;
        }
        try {
            Class.forName(args[0]).newInstance();
            Connection con = DriverManager.getConnection(args[1], args[2],
                                                         args[3]);
            File f = new File(args[4]);
            PreparedStatement stmt;

            if( !f.exists() ) {
                // if the file does not exist
                // retrieve it from the database and write it to the named file
                ResultSet rs;
                
                stmt = con.prepareStatement("SELECT blobData " +
                                              "FROM BlobTest " +
                                              "WHERE fileName = ?");

                stmt.setString(1, args[0]);
                rs = stmt.executeQuery();
                if( !rs.next() ) {
                    System.out.println("No such file stored.");
                }
                else {
                    Blob b = rs.getBlob(1);
                    BufferedOutputStream os;

                    os = new BufferedOutputStream(new FileOutputStream(f));
                    os.write(b.getBytes(0, (int)b.length()), 0,
                             (int)b.length());
                    os.flush();
                    os.close();
                }
            }
            else {
                // otherwise read it and save it to the database
                FileInputStream fis = new FileInputStream(f);
                byte[] tmp = new byte[1024];
                byte[] data = null;
                int sz, len = 0;
                
                while( (sz = fis.read(tmp)) != -1 ) {
                    if( data == null ) {
                        len = sz;
                        data = tmp;
                    }
                    else {
                        byte[] narr;
                        int nlen;
                        
                        nlen = len + sz;
                        narr = new byte[nlen];
                        System.arraycopy(data, 0, narr, 0, len);
                        System.arraycopy(tmp, 0, narr, len, sz);
                        data = narr;
                        len = nlen;
                    }
                }
                if( len != data.length ) {
                    byte[] narr = new byte[len];

                    System.arraycopy(data, 0, narr, 0, len);
                    data = narr;
                }
                stmt = con.prepareStatement("INSERT INTO BlobTest(fileName, " +
                                            "blobData) VALUES(?, ?)");
                stmt.setString(1, args[0]);
                stmt.setObject(2, data);
                stmt.executeUpdate();
                f.delete();
            }
            con.close();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }
}
