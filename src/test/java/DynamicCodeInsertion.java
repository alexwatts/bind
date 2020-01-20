import java.io.IOException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class DynamicCodeInsertion {

    public void dynamicallyInsertCode() throws CannotCompileException, NotFoundException, IOException {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get("Calculate");
        cc.defrost();
        // Without the call to "makePackage()", package information is lost
        //cp.makePackage(cp.getClassLoader(), "");
        CtMethod m = cc.getDeclaredMethod("addTogether");


        m.insertBefore("{System.out.print(\"Oh, say no to \");}");
        m.insertAfter("{System.out.print(\"And say - Hi World\");}");
        // Changes are not persisted without a call to "toClass()"
        cc.toBytecode();

        (new Calculate()).add(1,2);
    }
}








