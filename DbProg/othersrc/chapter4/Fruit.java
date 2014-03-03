import java.sql.*;

/**
 * Example 4.3.
 */
public class Fruit implements SQLData {
    private String name;
    private String sqlTypeName;

    public Fruit() {
        super();
    }

    public Fruit(String nom) {
        super();
        name = nom;
    }

    public String getName() {
        return name;
    }

    public String getSQLTypeName() {
        return sqlTypeName;
    }

    public void readSQL(SQLInput is, String type) throws SQLException {
        sqlTypeName = type;
        name = is.readString();
    }

    public void writeSQL(SQLOutput os) throws SQLException {
        os.writeString(name);
    }
}
