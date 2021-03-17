package ru.GeekBrains.Rostislav.Java3Group3.Lesson5;

public class Car implements Runnable {
    private static int CARS_COUNT;
    static {
        CARS_COUNT = 0;
    }
    private Race race;
    private int speed;
    private String name;
    public String getName() {
        return name;
    }
    public int getSpeed() {
        return speed;
    }
    public Car(Race race, int speed) {
        this.race = race;
        this.speed = speed;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
    }
    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int)(Math.random() * 800));
            System.out.println(this.name + " готов");
            MainClass.cyclicBarrier.await();        // все собираются на старте
            MainClass.cyclicBarrier.await();        // старт, чтобы никто досрочно не стартовал

        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);
        }
        if(!MainClass.isWinner) {       // проверка наличия победителя, и печать чемпиона
            MainClass.isWinner = true;
            System.out.println(this.name + " победил!");
        }
        MainClass.countDownLatch.countDown(); // подсчет финишировавших
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}