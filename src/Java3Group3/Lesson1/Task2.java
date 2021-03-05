package Java3Group3.Lesson1;

import java.util.ArrayList;
import java.util.Arrays;

public class Task2<T> {

    public ArrayList<T> convertArrayToArrayList (T[] array) {
        return new ArrayList<>(Arrays.asList(array));
    }

    public static void main(String[] args) {
        String[] strings = new String[]{"cat", "dog", "hat", "bed"};
        Task2 b = new Task2();
        ArrayList c = b.convertArrayToArrayList(strings);
        for (Object x:c) {
            System.out.println(x.toString());
        }
    }
}