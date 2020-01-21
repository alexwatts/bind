import java.lang.annotation.Annotation;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import static junit.framework.TestCase.fail;

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

            Object instance = testClass.newInstance();

            methodDescriptions.forEach((method, description) ->
            {
                try {

                    runNotifier.fireTestStarted(description);
                    method.invoke(instance);

                    MyJavaAgent.redefineClass(
                            new ClassDefinition(
                                    Calculate.class,
                                    new DynamicCodeInsertion().getBytes(
                                            "Calculate",
                                            "addTogether")
                            )
                    );

                    try {
                        method.invoke(instance);
                        fail();
                    } catch (InvocationTargetException e) {
                        if (!(e.getCause() instanceof AssertionError)) {
                            throw e;
                        }
                    }

                    runNotifier.fireTestFinished(description);
                }
                catch(AssumptionViolatedException e) {
                    Failure failure = new Failure(description, e.getCause() != null ? e.getCause(): e.fillInStackTrace());
                    runNotifier.fireTestAssumptionFailed(failure);
                }
                catch(Throwable e) {
                    Failure failure = new Failure(description, e.getCause() != null ? e.getCause(): e.fillInStackTrace());
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
