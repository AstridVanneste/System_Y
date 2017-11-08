import NameServer.NameServer;
        import NameServer.ResolverInterface;
        import java.rmi.NotBoundException;
        import java.rmi.RemoteException;
        import java.rmi.registry.LocateRegistry;
        import java.rmi.registry.Registry;

public class SystemYTest {

    public static void main (String[] args) {

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try{
            Registry reg = LocateRegistry.getRegistry("10.0.0.2");
            ResolverInterface resolver = (ResolverInterface) reg.lookup(NameServer.RESOLVER_NAME);

        }
        catch (RemoteException re) {
            System.out.println("RemoteException in LocateRegistry.getRegistry()");
            re.printStackTrace();
        }
        catch (NotBoundException nbe) {
            System.out.println("NotBoundException in lookup()");
            nbe.printStackTrace();
        }
    }
}