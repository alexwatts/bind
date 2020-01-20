import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class MyJavaAgent {

    private static Instrumentation instrumentation;

    /**
     * JVM hook to statically load the javaagent at startup.
     *
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     *
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void premain(String args, Instrumentation inst) throws Exception {

        instrumentation = inst;
    }

    /**
     * JVM hook to dynamically load javaagent at runtime.
     *
     * The agent class may have an agentmain method for use when the agent is
     * started after VM startup.
     *
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void agentmain(String args, Instrumentation inst) throws Exception {
        instrumentation = inst;
    }

    public static void addTransformer(ClassFileTransformer transformer) {
        instrumentation.addTransformer(transformer);
    }

    public static void removeTransformer(ClassFileTransformer transformer) {
        instrumentation.removeTransformer(transformer);
    }

    /**
     * Programmatic hook to dynamically load javaagent at runtime.
     */
    public static void initialize() {
        if (instrumentation == null) {
            MyJavaAgentLoader.loadAgent();
        }
    }

}