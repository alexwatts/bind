import javassist.*;
import java.io.IOException;

public class DynamicCodeInsertion {

    public byte[] getBytes(String clazz, String methodName)
            throws NotFoundException, CannotCompileException, IOException {

        ClassPool cp = ClassPool.getDefault();
        cp.importPackage(".");
        CtClass cc = cp.get(clazz);
        CtMethod method = cc.getDeclaredMethod(methodName);

        new IntTransformer().transform(method);

        return cc.toBytecode();
    }

}








