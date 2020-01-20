import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class BindRunner extends Runner {

    private Class testClass;
    private HashMap<Method, Description> methodDescriptions;

    public BindRunner(Class testClass) {
        this.testClass = testClass;
        methodDescriptions = new HashMap<>();
        MyJavaAgent.initialize();

    }

    public Description getDescription() {
        Description description =
                Description.createSuiteDescription(
                        testClass.getName(),
                        testClass.getAnnotations());

        for(Method method : testClass.getMethods()) {
            Annotation annotation =
                    method.getAnnotation(Test.class);
            if(annotation != null) {
                Description methodDescription =
                        Description.createTestDescription(
                                testClass,
                                method.getName(),
                                annotation);
                description.addChild(methodDescription);

                methodDescriptions.put(method, methodDescription);
            }
        }

        return description;
    }

    public void run(RunNotifier runNotifier) {

        try {

            ClassPool cp = ClassPool.getDefault();


            Object instance = testClass.newInstance();

            methodDescriptions.forEach((method, description) ->
            {
                try {

                    CtClass cc = cp.get("Calculate");


                    // Without the call to "makePackage()", package information is lost
                    cp.makePackage(cp.getClassLoader(), "");
                    CtMethod m = cc.getDeclaredMethod("addTogether");


                    m.insertBefore("{System.out.print(\"Oh, say no to \");}");
                    m.insertAfter("{System.out.print(\"And say - Hi World\");}");
                    // Changes are not persisted without a call to "toClass()"

                    final byte[] bytes = cc.toBytecode();

                    MyJavaAgent.addTransformer(
                            new ClassFileTransformer() {
                                @Override
                                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                                    if (className != null && className.equals("Calculate")) {
                                        return bytes;
                                    } else {
                                        return classfileBuffer;
                                    }
                                }
                            }

                    );

                    runNotifier.fireTestStarted(description);
                    method.invoke(instance);
                    runNotifier.fireTestFinished(description);
                }
                catch(AssumptionViolatedException e) {
                    Failure failure = new Failure(description, e.getCause());
                    runNotifier.fireTestAssumptionFailed(failure);
                }
                catch(Throwable e) {
                    Failure failure = new Failure(description, e.getCause());
                    runNotifier.fireTestFailure(failure);
                }
                finally {
                    runNotifier.fireTestFinished(description);
                }
            });
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


}
