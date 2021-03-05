package Java3Group3.Lesson1.Task3;

public class RunApp {
    public static void main(String[] args) {

        Box<Apple> apples1 = new Box<>();
        Box<Apple> apples2 = new Box<>();
        Box<Orange> oranges1 = new Box<>();
        Box<Orange> oranges2 = new Box<>();

        for (int i = 0; i < 10; i++) {
            apples1.addFruitToBox(new Apple());
            apples2.addFruitToBox(new Apple());
            oranges1.addFruitToBox(new Orange());
            oranges2.addFruitToBox(new Orange());
        }
        System.out.println("Weight of first apple box: " + apples1.getWeight());
        System.out.println("Weight of first orange box: " + oranges1.getWeight());
        System.out.println();
        System.out.println("Weight of first and second apple boxes is equals: " + apples1.compare(apples2));
        System.out.println("Weight of first orange box and first apple box is equals: " + oranges1.compare(apples1));
        System.out.println();

        apples1.pourFruits(apples2);
        oranges1.pourFruits(oranges2);

        System.out.println("Weight of first apple box after pouring to second box: " + apples1.getWeight());
        System.out.println("Weight of second apple box after pouring from first box: " + apples2.getWeight());
        System.out.println("Weight of first orange box after pouring to second box: " + oranges1.getWeight());
        System.out.println("Weight of second orange box after pouring from first box: " + oranges2.getWeight());
    }
}