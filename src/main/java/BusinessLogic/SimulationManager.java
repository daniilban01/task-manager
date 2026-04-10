package BusinessLogic;

import Model.Server;
import Model.Task;

import java.awt.datatransfer.StringSelection;
import java.util.*;

public class SimulationManager implements Runnable {
    private Scheduler scheduler;
    //private SimulationFrame frame;
    private List<Task> tasks;
    private SelectionPolicy selectionPolicy = SelectionPolicy.SHORTEST_TIME;
    public int timeLimit = 30;
    public int maxProcessingTime = 10;
    public int minProcessingTime = 2;
    public int numberOfServers = 3;
    public int numberOfClients = 12;
    public int maxArrivalTime = 20;
    public int minArrivalTime = 2;

    public SimulationManager(){
        this.scheduler = new Scheduler(numberOfServers, numberOfClients);
        this.scheduler.changeStrategy(selectionPolicy);
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
        int currentTime = 0;
        while(currentTime <= timeLimit){
            Iterator<Task> iterator = tasks.iterator();
            while(iterator.hasNext()){
                Task task = iterator.next();
                if(task.getArrivalTime() == currentTime){
                    scheduler.dispatchTask(task);
                    iterator.remove();
                }
            }

            try {
                Thread.sleep(1000L);
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            }
            System.out.println(getLogOfEvents(currentTime));
            currentTime++;
        }
    }

    private String getLogOfEvents(int currentTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("Time ").append(currentTime).append("\n");

        sb.append("Waiting clients: ");
        if (tasks == null || tasks.isEmpty()) {
            sb.append("none");
        } else {
            for (int i = 0; i < tasks.size(); i++) {
                Task t = tasks.get(i);
                if (t != null) {
                    sb.append("(").append(t.getID()).append(", ")
                            .append(t.getArrivalTime()).append(", ")
                            .append(t.getServiceTime()).append(")");
                    if (i < tasks.size() - 1) {
                        sb.append(", ");
                    }
                }
            }
        }
        sb.append("\n");

        List<Server> servers = scheduler.getServers();
        for (int i = 0; i < servers.size(); i++) {
            sb.append("Queue ").append(i + 1).append(": ");

            Task[] currentTasks = servers.get(i).getTasks();

            if (currentTasks == null || currentTasks.length == 0) {
                sb.append("closed");
            } else {
                boolean first = true;
                for (Task t : currentTasks) {
                    if (t != null) {
                        if (!first) {
                            sb.append("; ");
                        }
                        sb.append("(").append(t.getID()).append(", ")
                                .append(t.getArrivalTime()).append(", ")
                                .append(t.getServiceTime()).append(")");
                        first = false;
                    }
                }
                if (first) {
                    sb.append("closed");
                }
            }
            sb.append("\n");
        }
        sb.append("-------------------------------------------\n");
        return sb.toString();
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
