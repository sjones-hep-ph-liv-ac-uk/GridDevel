import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GuestBookServlet extends HttpServlet {
    private Properties connectionProperties = new Properties();
    private Driver     driver               = null;
    private String     driverName           = null;
    private String     jdbcURL              = null;
    private Random     random               = new Random();
    
    /**
     * Provides the servlet with the chance to get runtime configuration
     * values and initialize itself.  For a database servlet, you want
     * to grab the driver name, URL, and any connection information.
     * For this example, I assume a driver that requires a user name
     * and password.  For an example of more database independent
     * configuration, see Chapter 4.
     * @param cfg the servlet configuration information
     * @throws javax.servlet.ServletException could not load the specified
     * JDBC driver
     */
    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);
        {
            String user, pw;

            driverName = cfg.getInitParameter("gb.driver");
            jdbcURL = cfg.getInitParameter("gb.jdbcURL");
            user = cfg.getInitParameter("gb.user");
            if( user != null ) {
                connectionProperties.put("user", user);
            }
            pw = cfg.getInitParameter("gb.pw");
            if( pw != null ) {
                connectionProperties.put("password", pw);
            }
            try {
                driver = (Driver)Class.forName(driverName).newInstance();
            }
            catch( Exception e ) {
                throw new ServletException("Unable to load driver: " +
                                           e.getMessage());
            }
        }
    }

    /**
     * Performs the HTTP GET.  This is where we print out a form and
     * a random sample of the comments.
     * @param req the servlet request information
     * @param res the servlet response information
     * @throws javax.servlet.ServletException an error occurred talking to
     * the database
     * @throws java.io.IOException a socket error occurred
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        Locale loc = getLocale(req);

        res.setContentType("text/html");
        printCommentForm(out, loc);
        printComments(out, loc);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        Locale loc = getLocale(req);
        String name, email, comment;
        Connection conn = null;
        Exception err = null;
        int id = -1;
        String[] tmp;

        // get the form values
        tmp = req.getParameterValues("name");
        if( tmp == null || tmp.length != 1 ) {
            name = null;
        }
        else {
            name = tmp[0];
        }
        tmp = req.getParameterValues("email");
        if( tmp == null || tmp.length != 1 ) {
            email = null;
        }
        else {
            email = tmp[0];
        }
        tmp = req.getParameterValues("comments");
        if( tmp == null || tmp.length != 1 ) {
            comment = null;
        }
        else {
            comment = tmp[0];
        }
        res.setContentType("text/html");
        // validate values
        if( name.length() < 1 ) {
            out.println("You must specify a valid name!");
            printCommentForm(out, loc);
            return;
        }
        if( email.length() < 3 ) {
            out.println("You must specify a valid email address!");
            printCommentForm(out, loc);
            return;
        }
        if( email.indexOf("@") < 1 ) {
            out.println("You must specify a valid email address!");
            printCommentForm(out, loc);
            return;
        }
        if( comment.length() < 1 ) {
            out.println("You left no comments!");
            printCommentForm(out, loc);
            return;
        }
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd",
                                                        Locale.US);
            java.util.Date date = new java.util.Date();
            ResultSet result;
            Statement stmt;
        
            conn = DriverManager.getConnection(jdbcURL, connectionProperties);
            // remove the "setAutoCommit(false)" line for mSQL or MySQL
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            // generate a new comment ID
            // more on ID generation in Chapter 4
            result = stmt.executeQuery("SELECT NEXT_SEQ " +
                                       "FROM SEQGEN " +
                                       "WHERE NAME = 'COMMENT_ID'");
            if( !result.next() ) {
                throw new ServletException("Failed to generate id.");
            }
            id = result.getInt(1) + 1;
            stmt.close();
            // closing the statement closes the result
            stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE SEQGEN SET NEXT_SEQ = " + id +
                               " WHERE NAME = 'COMMENT_ID'");
            stmt.close();
            stmt = conn.createStatement();
            comment = fixComment(comment);
            stmt.executeUpdate("INSERT INTO COMMENT " +
                               "(COMMENT_ID, EMAIL, NAME, COMMENT, " +
                               "CMT_DATE) "+
                               "VALUES (" + id +", '" + email +
                               "', '" + name + "', '" +
                               comment + "', '" + fmt.format(date) +
                               "')");
            conn.commit();
            stmt.close();
        }
        catch( SQLException e ) {
            e.printStackTrace();
            err = e;
        }
        finally {
            if( conn != null ) {
                try { conn.close(); }
                catch( Exception e ) { }
            }
        }
        if( err != null ) {
            out.println("An error occurred on save: " + err.getMessage());
        }
        else {
            printCommentForm(out, loc);
            printComments(out, loc);
        }
    }

    /**
     * Find the desired locale from the HTTP header.
     * @param req the servlet request from which the header is read
     * @return the locale matching the first accepted language
     */
    private Locale getLocale(HttpServletRequest req) {
        String hdr = req.getHeader("Accept-Language");
        StringTokenizer toks;
        
        if( hdr == null ) {
            return Locale.getDefault();
        }
        toks = new StringTokenizer(hdr, ",");
        if( toks.hasMoreTokens() ) {
            String lang = toks.nextToken();
            int ind = lang.indexOf(';');
            Locale loc;
                
            if( ind != -1 ) {
                lang = lang.substring(0, ind);
            }
            lang = lang.trim();
            ind = lang.indexOf("-");
            if( ind == -1 ) {
                loc = new Locale(lang, "");
            }
            else {
                loc = new Locale(lang.substring(0, ind),
                                 lang.substring(ind+1));
            }
            return loc;
        }
        return Locale.getDefault();
    }
    
    public String getServletInfo() {
        return "Guest Book Servlet\nFrom Database Programming with JDBC " +
            "and Java";
    }

    private void printCommentForm(PrintWriter out, Locale loc)
        throws IOException {
        out.println("<div class=\"gbform\">");
        out.println("<form action=\"personal/guestbook.shtml\" " +
                    "method=\"POST\">");
        out.println("<table>");
        out.println("<tr>");
        out.println("<td>Name:</td>");
        out.println("<td><input type=\"text\" name=\"name\" " +
                    "size=\"30\"/></td>");
        out.println("<td><input type=\"submit\" value=\"Save\"/></td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>Email:</td>");
        out.println("<td><input type=\"text\" name=\"email\" " +
                    "size=\"30\"/></td>");
        out.println("<tr>");
        out.println("<tr>");
        out.println("<td>Comments:</td>");
        out.println("<tr>");
        out.println("<tr>");
        out.println("<td colspan=\"3\">");
        out.println("<textarea name=\"comments\" cols=\"40\" rows=\"7\">");
        out.println("</textarea></td>");
        out.println("<tr>");
        out.println("</table>");
        out.println("</form>");
    }

    private void printComments(PrintWriter out, Locale loc)
        throws IOException {
        Connection conn = null;
    
        try {
            DateFormat fmt = DateFormat.getDateInstance(DateFormat.FULL, loc);
            ResultSet results;
            Statement stmt;
            int rows, count;
        
            conn = DriverManager.getConnection(jdbcURL, connectionProperties);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY);
            results = stmt.executeQuery("SELECT NAME, EMAIL, CMT_DATE, " +
                                        "COMMENT, COMMENT_ID " +
                                        "FROM COMMENT " +
                                        "ORDER BY CMT_DATE");
            out.println("<dl>");
            results.last();
            results.next();
            rows = results.getRow();
            // pick a random row
            rows = random.nextInt()%rows;
            if( rows < 4 ) {
                // if the random row is less than 4, print the first 4 rows
                results.afterLast();
            }
            else {
                // otherwise go to the specified row, print the prior 5 rows
                results.absolute(rows);
            }
            count = 0;
            // print up to 5 rows going backwards from the randomly
            // selected row
            while( results.previous() && (count < 5) ) {
                String name, email, cmt;
                Date date;

                count++;
                name = results.getString(1);
                if( results.wasNull() ) {
                    name = "Unknown User";
                }
                email = results.getString(2);
                if( results.wasNull() ) {
                    email = "user@host";
                }
                date = results.getDate(3);
                if( results.wasNull() ) {
                    date = new Date((new java.util.Date()).getTime());
                }
                cmt = results.getString(4);
                if( results.wasNull() ) {
                    cmt = "No comment.";
                }
                out.println("<dt><b>" + name + "</b> (" + email + ") on " +
                            fmt.format(date) + "</dt>");
                cmt = noXML(cmt);
                out.println("<dd> " + cmt + "</dd>");
            }
            out.println("</dl>");
        }
        catch( SQLException e ) {
            out.println("A database error occurred: " + e.getMessage());
        }
        finally {
            if( conn != null ) {
                try { conn.close(); }
                catch( SQLException e ) { }
            }
        }
    }

    /**
     * Removes any XML-sensitive characters from a comment and
     * replaces them with their character entities.
     * @param cmt the raw comment
     * @return the XML-safe comment
     */
    private String noXML(String cmt) {
        StringBuffer buff = new StringBuffer();

        for(int i=0; i<cmt.length(); i++) {
            char c = cmt.charAt(i);
            
            switch(c) {
            case '<':
                buff.append("&lt;");
                break;
            case '>':
                buff.append("&gt;");
                break;
            case '&':
                buff.append("&amp;");
                break;
            case '"':
                buff.append("&quot;");
                break;
            default:
                buff.append(c);
                break;
            }
        }
        return buff.toString();
    }

    /**
     * This method escapes single quotes so that database statements are
     * not messed up.
     * @param comment the raw comment
     * @return a comment with any quotes escaped
     */
    private String fixComment(String comment) {
        if( comment.indexOf("'") != -1 ) {
            String tmp = "";
        
            for(int i=0; i<comment.length(); i++) {
                char c = comment.charAt(i);
        
                if( c == '\'' ) {
                    tmp = tmp + "\\'";
                }
                else {
                    tmp = tmp + c;
                }
            }
            comment = tmp;
        }
        return comment;
    }
}
