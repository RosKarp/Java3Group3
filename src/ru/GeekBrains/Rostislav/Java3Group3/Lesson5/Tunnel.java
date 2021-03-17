package ru.GeekBrains.Rostislav.Java3Group3.Lesson5;

public class Tunnel extends Stage{
    public Tunnel() {
        this.length = 80;
        this.description = "Тоннель " + length + " метров";
    }
    @Override
    public void go(Car c) {
        try {
            try {
                if(MainClass.semaphore.getQueueLength() > 0) { // если можно, едет в тоннель без ожидания
                    System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                }
                MainClass.semaphore.acquire(); // в тоннеле
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " закончил этап: " + description);
                MainClass.semaphore.release(); // выехал из тоннеля
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}