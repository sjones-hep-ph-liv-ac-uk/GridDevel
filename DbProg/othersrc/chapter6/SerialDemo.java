import java.io.*;

/**
 * Example 6-2.
 */
public class SerialDemo implements Serializable {
    static public void main(String[] args) {
        try {
            { // Save a SerialDemo object with a value of 5.
                FileOutputStream f = new FileOutputStream("/tmp/testing");
                ObjectOutputStream s = new ObjectOutputStream(f);
                SerialDemo d= new SerialDemo(5);

                s.writeObject(d);
                s.flush();
            }
            { // Now restore it and look at the value.
                FileInputStream f = new FileInputStream("/tmp/testing");
                ObjectInputStream s = new ObjectInputStream(f);
                SerialDemo d = (SerialDemo)s.readObject();

                System.out.println("SerialDemo.getVal() is: " +
                                   d.getVal());
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }

    int test_val= 7; // value defaults to 7

    public SerialDemo() {
        super();
    }

    public SerialDemo(int x) {
        super();
        test_val = x;
    }
    
    public int getVal() {
        return test_val;
    }
}
