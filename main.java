package com.example.demo45;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.lang.System.*;


class Generator implements Runnable {

    private Simulation sim;


    public Generator(Simulation sim) {
        this.sim = sim;
    }


    @Override
    public void run() {

        int i = 0;
        while (i < 10) {
            synchronized (sim) {
                try {
                    while (!sim.array.isEmpty()) {
                        sim.wait();
                    }
                    Random rand = new Random();
                    for (int j = 0; j < 10; j++) sim.array.add(rand.nextInt(100));

                    System.out.println("Generator filled the array:");
                    sim.print();

                    sim.notifyAll();

                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            i++;
        }
    }
}


class Sorter implements Runnable {

    private Simulation sim;


    public Sorter(Simulation sim) {
        this.sim = sim;
    }


    @Override
    public void run() {

        IntStream.range(0, 10).forEach(i -> {
            synchronized (sim) {

                try {

                    while (true) {
                        if (!sim.array.isEmpty() && !sim.sorted) break;
                        sim.wait();
                    }

                    Collections.sort(sim.array);

                    sim.sorted = true;

                    out.println("Sorter sorted the array:");
                    sim.print();
                    sim.notifyAll();

                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}


class Drainer implements Runnable {

    private Simulation sim;


    public Drainer(Simulation sim) {
        this.sim = sim;
    }


    @Override
    public void run() {

        IntStream.range(0, 10).forEach(i -> {
            synchronized (sim) {

                try {

                    while (sim.array.isEmpty() || !sim.sorted) {
                        sim.wait();
                    }

                    sim.array.clear();
                    sim.sorted = false;
                    out.println("Drainer cleared the array.");
                    out.println();
                    sim.notifyAll();
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}


class Simulation {
    public ArrayList<Integer> array;
    public boolean sorted;


    public Simulation() {
        this.array = new ArrayList<Integer>();
        this.sorted = false;
    }


    public synchronized void print() {
        array.stream().map(num -> num + " ").forEach(out::print);
        out.println();
    }


    public void runsim() {

        Generator gen = new Generator(this);
        Sorter sort = new Sorter(this);
        Drainer drain = new Drainer(this);


        ExecutorService executor = Executors.newFixedThreadPool(3);


        executor.execute(gen);
        executor.execute(sort);
        executor.execute(drain);


        executor.shutdown();
    }
}


public class Main {
    public static void main(String[] args) {

        var sim = new Simulation();

        sim.runsim();
    }
}
