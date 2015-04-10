import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author MaTianyu
 * @date 2015-03-21
 */
public class TestNewInstance {

    public static void main(String[] args) {
        A a = new A();
        Field[] fs = A.class.getDeclaredFields();
        try {
            for (Field field : fs) {
                System.out.println(field.getType());
                System.out.println(Collection.class.isAssignableFrom(field.getType()));
                System.out.println(Object[].class.isAssignableFrom(field.getType()));
                System.out.println(field.getType().isArray());
                //Object aa =null;
                //if(aa instanceof Collection){
                //
                //}
                //aa.getClass().isArray();
                //if(aa instanceof Object[]){
                //
                //}
                if (Collection.class.isAssignableFrom(field.getType())) {
                    Collection collection = (Collection) field.getType().newInstance();
                    B b = B.class.newInstance();
                    collection.add(b);
                    field.setAccessible(true);
                    field.set(a, collection);
                } else if (Object[].class.isAssignableFrom(field.getType())) {
                    ArrayList<Object> list = new ArrayList<Object>();
                    B b = B.class.newInstance();
                    list.add(b);
                    Object[] array = list.toArray();
                    System.out.println(array.getClass().getComponentType());

                    array = (Object[]) Array.newInstance(field.getType().getComponentType(), list.size());
                    System.out.println(array.getClass().getComponentType());
                    array[0] = b;
                    field.setAccessible(true);
                    field.set(a, array);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(a);

        ArrayList<B> bList = new ArrayList<B>();
        bList.add(new B());
        Object[] objArr = bList.toArray();
        System.out.println(objArr instanceof Object[]);
        System.out.println(objArr instanceof B[]);
        System.out.println(objArr.getClass());
        System.out.println(objArr.getClass().getComponentType());
        System.out.println(objArr[0].getClass());
        System.out.println(B.class.isAssignableFrom(objArr[0].getClass()));
        System.out.println(objArr[0] instanceof B);
        ((B) objArr[0]).bm();
        System.out.println(Arrays.toString(objArr));


        Object[] bArr = new B[1];
        bArr[0] = new B();
        List<?> objList = Arrays.asList(bArr);
        System.out.println(objList instanceof List);
        System.out.println(objList.getClass());
        System.out.println(Arrays.toString(objList.getClass().getGenericInterfaces()));
        System.out.println(objList.getClass().getGenericSuperclass());
        System.out.println(Arrays.toString(((ParameterizedType) objList.getClass().getGenericSuperclass())
                .getActualTypeArguments()));
        System.out.println(objList.get(0).getClass());
        System.out.println(B.class.isAssignableFrom(objList.get(0).getClass()));
        System.out.println(objList.get(0) instanceof B);


    }

    static class A {
        private B[] bArray;
        private ArrayList<B> aList;
        private Vector<B> vList;
        private ConcurrentLinkedQueue<B> cQueue;

        @Override
        public String toString() {
            return "A{" +
                    "bArray=" + Arrays.toString(bArray) +
                    ", aList=" + aList +
                    ", vList=" + vList +
                    ", cQueue=" + cQueue +
                    '}';
        }
    }

    static class B {
        private String b = "b";
        private int bb = 3;

        public void bm() {
            System.out.println("呵呵 bm执行了");
        }

        @Override
        public String toString() {
            return "B{" +
                    "b='" + b + '\'' +
                    ", bb=" + bb +
                    '}';
        }
    }
}
