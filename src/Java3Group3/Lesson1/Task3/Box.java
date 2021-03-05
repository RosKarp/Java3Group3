package Java3Group3.Lesson1.Task3;

import java.util.ArrayList;

public class Box<T extends Fruit> {

    private ArrayList<T> fruits = new ArrayList<>();

    public void addFruitToBox (T fruit) {
        fruits.add(fruit);
    }

    public float getWeight () {                                     //основной вариант getWeight ()
       try{
            return fruits.get(0).getWeight() * fruits.size();
        } catch (NullPointerException e) { return 0.0f;}
    }

    public float getWeight (T fruit, int quantityOfFruits) {        // перегруженный вариант getWeight
        try {
            return fruit.getWeight() * quantityOfFruits;
        } catch (NullPointerException e) { return 0.0f;}
    }

    public boolean compare (Box<?> anotherBox) {
        return this.getWeight() - anotherBox.getWeight() < 0.0001f;
    }

    public void pourFruits (Box<T> newBox) {
        newBox.fruits.addAll(this.fruits);
        this.fruits = null;
    }
}