package ru.GeekBrains.Rostislav.Java3Group3.Lesson5;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class MainClass {
    static final int CARS_COUNT = 4;
    static CyclicBarrier cyclicBarrier = new CyclicBarrier(CARS_COUNT+1); // + main поток для объявлений
    static Semaphore semaphore = new Semaphore((int)CARS_COUNT/2); // в тоннеле не более половины машин
    static CountDownLatch countDownLatch = new CountDownLatch(CARS_COUNT); // подсчет финишировавших
    static boolean isWinner = false; // для объявления чемпиона

    public static void main(String[] args) {

        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];
        for (int i = 0; i < cars.length; i++) {
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10));
        }
        try {
            for (Car car : cars) {
                new Thread(car).start();
            }
            cyclicBarrier.await(); // ожидание подготовки
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
            cyclicBarrier.await(); // общий старт
            countDownLatch.await(); // ожидание финиша всех машин
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}