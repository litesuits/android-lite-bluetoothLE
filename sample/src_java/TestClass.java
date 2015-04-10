/**
 * @author MaTianyu
 * @date 2015-03-21
 */
public class TestClass {

    public static void main(String[] args) {
        A a = new A();
        B b = new B();

        a.p();
        b.p();
        //((B)a).p();
        ((A) b).p();

        System.out.println(a(0));
    }

    static int a(int i) {
        System.out.println(i++);
        if (i < 7) {
            return a(i);
        }
        return i;
    }

    static class A {
        public void p() {
            System.out.println("a");
        }
    }

    static class B extends A {

        @Override
        public void p() {
            System.out.println("b");
        }
    }
}
