import javassist.CannotCompileException;
import javassist.CtMethod;

public class IntTransformer {

    public CtMethod transform(CtMethod method) throws CannotCompileException {
        method.setBody("{ return 0;}");
        return method;
    }

}








