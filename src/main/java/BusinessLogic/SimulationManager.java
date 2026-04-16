package BusinessLogic;

import Model.Server;
import Model.Task;

import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulationManager implements Runnable {
    private ExecutorService pool;
    private Scheduler scheduler;
    //private SimulationFrame frame;
    private List<Task> tasks;
    private SelectionPolicy selectionPolicy = SelectionPolicy.SHORTEST_TIME;
    public int timeLimit = 30;
    public int maxProcessingTime = 10;
    public int minProcessingTime = 2;
    public int numberOfServers = 2;
    public int numberOfClients = 9;
    public int maxArrivalTime = 20;
    public int minArrivalTime = 2;

    private double totalWaitingTime = 0;
    private double totalServiceTime = 0;
    private int peakHour = 0;
    private int maxClients = -1;

    private PrintWriter writer;

    public SimulationManager(){
        this.scheduler = new Scheduler(numberOfServers, numberOfClients);
        this.scheduler.changeStrategy(selectionPolicy);
        this.pool = Executors.newFixedThreadPool(numberOfServers);
        try {
            this.writer = new PrintWriter("log.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        generateNRandomTasks();
    }

    public void generateNRandomTasks(){
        Random random = new Random();
        this.tasks = new ArrayList<Task>();
        for(int i = 0; i < numberOfClients; i++){
            int id = i;
            int arrivalTime = random.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime;
            int processingTime = random.nextInt(maxProcessingTime - minProcessingTime + 1) + minProcessingTime;
            tasks.add(new Task(id, arrivalTime, processingTime));
            totalServiceTime += processingTime;

        }
        tasks.sort(new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                if(t1.getArrivalTime() > t2.getArrivalTime())
                    return 1;
                else if(t1.getArrivalTime() < t2.getArrivalTime())
                    return -1;
                else
                    return 0;
            }
        });
    }

    @Override
    public void run() {
        for(Server server : scheduler.getServers()) pool.execute(server);
        int currentTime = 0;
        while(currentTime <= timeLimit){
            Iterator<Task> iterator = tasks.iterator();
            while(iterator.hasNext()){
                Task task = iterator.next();
                if(task.getArrivalTime() == currentTime){
                    totalWaitingTime += scheduler.dispatchTask(task);
                    iterator.remove();
                }
            }
            int currentClients = 0;
            for(Server server :  scheduler.getServers())
                currentClients += server.getTasks().length;
            if(currentClients >= maxClients){
                maxClients = currentClients;
                peakHour = currentTime;
            }
            writeLog(currentTime);
            if(tasks.isEmpty() && areAllServersEmpty()) break;
            for(Server server : scheduler.getServers()) server.tick();
            try {
                Thread.sleep(1000L);
            }catch (InterruptedException e){
                break;
            }
            currentTime++;
        }
        String stats = "Average waiting time: " + (totalWaitingTime/numberOfClients) +
                        "\nAverage service time: " + (totalServiceTime/numberOfClients) +
                        "\nPeak hour: " + peakHour;
        System.out.println(stats);

        writer.println(stats);
        writer.close();
        pool.shutdownNow();
    }

    private void writeLog(int time) {
        String log = "Time " + time + "\nWaiting: " + (tasks.isEmpty() ? "none" : tasks) + "\n";
        for (int i = 0; i < scheduler.getServers().size(); i++) {
            Task[] currentTasks = scheduler.getServers().get(i).getTasks();
            log += "Queue " + (i + 1) + ": " + (currentTasks.length == 0 ? "closed" : Arrays.toString(currentTasks)) + "\n";
        }
        log += "-------------------------------------------\n";
        System.out.print(log);
        writer.print(log);
        writer.flush();
    }

    private boolean areAllServersEmpty() {
        for (Server s : scheduler.getServers()) {
            if (s.getTasks().length > 0) return false;
        }
        return true;
    }

    public static void main(String[] args){
        SimulationManager gen = new SimulationManager();
        Thread t = new Thread(gen);
        t.start();
    }
}
