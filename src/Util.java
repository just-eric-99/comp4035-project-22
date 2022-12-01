public class Util {
    public static <T> void shiftLeft(T[] array) {
        shiftLeft(array, 0);
    }

    public static <T> void shiftLeft(T[] array, int start) {
        for (int i = start; i < array.length; i++) {
            if (array[i] == null) break;

            if (i == array.length - 1) {
                array[i] = null;
                break;
            }

            array[i] = array[i + 1];
        }
    }

    public static <T> void shiftRight(T[] array) {
        shiftRight(array, 0);
    }

    public static <T> void shiftRight(T[] array, int start) {
        for (int i = array.length - 2; i >= start; i--) {
            array[i + 1] = array[i];
            if (i == start)
                array[i] = null;
        }
    }

    public static <T> int findFirstSpace(T[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) return i;
        }
        return -1;
    }

    public static <T> void mergeArray(T[] array1, T[] array2) {
        for (int i = findFirstSpace(array1), j = 0; i < array2.length; i++) {
            if (array2[j] == null) break;
            array1[i] = array2[j++];
        }
    }

    public static void main(String[] args) {
        Integer[] a = {1, 2, 3, 4};
        shiftLeft(a, 0);
        for (Integer i : a) {
            System.out.print(i + " ");
        }

        System.out.println("bab".compareTo("xa"));
    }
}
