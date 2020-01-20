import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import javassist.CannotCompileException;
import javassist.NotFoundException;
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
        try {
            new DynamicCodeInsertion().dynamicallyInsertCode();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
