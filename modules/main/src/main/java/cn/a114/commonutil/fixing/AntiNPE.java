package cn.a114.commonutil.fixing;

public class AntiNPE {


    /**
     * @apiNote null check
     * @throws NullPointerException
     */
    public static void checkNonNull(Object... object_s) throws NullPointerException {
        for (Object o : object_s) {
            if (o == null) {
                throw new NullPointerException("Null Object!!!");
            }
        }
    }
}
