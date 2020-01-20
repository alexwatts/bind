import com.sun.tools.attach.VirtualMachine;
import java.lang.management.ManagementFactory;

public class MyJavaAgentLoader {

    private static final String jarFilePath = "/Users/alewatts/development/bind/target/bind-1.0-SNAPSHOT.jar";
    public static void loadAgent() {

        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);

        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(jarFilePath, "");
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}