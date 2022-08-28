package ca.camosun.ICS226;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // System.out.println( "Hello World!" );
        Server s = new Server(12345);

        while(true)
        {
            s.getConnections();
        }
    }
}
