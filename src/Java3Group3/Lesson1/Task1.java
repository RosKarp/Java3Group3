package Java3Group3.Lesson1;

public class Task1<T> {

    private void changeElements (T[] arr, int one, int two){
        if (one < 0 || two < 0 || one >= arr.length || two >= arr.length) {
            System.out.println("Index out of bounds");
        } else{
            T temp = arr[one];
            arr[one] = arr[two];
            arr[two] = temp;
        }
    }

    public static void main(String[] args) {
        Task1 a =new Task1();
        String[] strings = new String[] {"cat", "dog", "hat", "bed"};
        for (String x:strings) {
            System.out.println(x);
        }
        System.out.println();
        a.changeElements(strings, 2,5);
        System.out.println();
        a.changeElements(strings, 0, 3);

        for (String x:strings) {
            System.out.println(x);
        }
    }
}