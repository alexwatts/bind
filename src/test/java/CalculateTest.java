import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BindRunner.class)
public class CalculateTest {

    //@TestOf(clazz = Calculate.class, name = "addTogether")
    @Test
    public void testAdd() {
        int one = 1;
        int two = 2;

        Calculate calculate = new Calculate();

        assertThat(calculate.add(one, two), equalTo(3));
    }

}
