package BusinessLogic;

import GUI.SimulationFrame;
import Model.Server;
import Model.Task;
import javax.swing.*;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulationManager implements Runnable {
    private ExecutorService pool;
    private Scheduler scheduler;
    private List<Task> tasks;
    private SelectionPolicy selectionPolicy;

    public int timeLimit, maxProcessingTime, minProcessingTime, numberOfServers, numberOfClients, maxArrivalTime, minArrivalTime;

    private double totalWaitingTime = 0;
    private double totalServiceTime = 0;
    private int peakHour = 0;
    private int maxClients = -1;

    private PrintWriter writer;
    private SimulationFrame frame;

    public SimulationManager() {
        this.frame = new SimulationFrame();
        this.frame.setVisible(true);
        this.frame.getStartButton().addActionListener(e -> {
            try {
                this.numberOfClients = Integer.parseInt(frame.getClientsField().getText());
                this.numberOfServers = Integer.parseInt(frame.getQueuesField().getText());
                this.minArrivalTime = Integer.parseInt(frame.getMinArrivalField().getText());
                this.maxArrivalTime = Integer.parseInt(frame.getMaxArrivalField().getText());
                this.minProcessingTime = Integer.parseInt(frame.getMinServiceField().getText());
                this.maxProcessingTime = Integer.parseInt(frame.getMaxServiceField().getText());
                this.timeLimit = Integer.parseInt(frame.getTimeLimitField().getText());
                this.selectionPolicy = (SelectionPolicy) frame.getStrategyComboBox().getSelectedItem();
                this.totalWaitingTime = 0;
                this.totalServiceTime = 0;
                this.maxClients = -1;
                this.peakHour = 0;
                this.scheduler = new Scheduler(numberOfServers, numberOfClients);
                this.scheduler.changeStrategy(selectionPolicy);
                this.pool = Executors.newFixedThreadPool(numberOfServers);

                try { this.writer = new PrintWriter("log.txt"); } catch (Exception ex) {}

                generateNRandomTasks();

                new Thread(this).start();
                frame.getStartButton().setEnabled(false);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid integers!");
            }
        });
    }

    public void generateNRandomTasks() {
        Random random = new Random();
        this.tasks = new ArrayList<>();
        for (int i = 0; i < numberOfClients; i++) {
            int arrivalTime = random.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime;
            int processingTime = random.nextInt(maxProcessingTime - minProcessingTime + 1) + minProcessingTime;
            tasks.add(new Task(i, arrivalTime, processingTime));
            totalServiceTime += processingTime;
        }
        tasks.sort(new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return Integer.compare(t1.getArrivalTime(), t2.getArrivalTime());
            }
        });

    }

    @Override
    public void run() {
        for (Server server : scheduler.getServers()) pool.execute(server);
        int currentTime = 0;
        while (currentTime <= timeLimit) {
            Iterator<Task> iterator = tasks.iterator();
            while (iterator.hasNext()) {
                Task task = iterator.next();
                if (task.getArrivalTime() == currentTime) {
                    totalWaitingTime += scheduler.dispatchTask(task);
                    iterator.remove();
                }
            }
            int currentClients = 0;
            for (Server server : scheduler.getServers())
                currentClients += server.getTasks().length;

            if (currentClients > maxClients) {
                maxClients = currentClients;
                peakHour = currentTime;
            }
            writeLog(currentTime);
            updateGUI(currentTime);

            if (tasks.isEmpty() && areAllServersEmpty()) break;
            for (Server server : scheduler.getServers()) server.tick();

            try { Thread.sleep(1000L); } catch (InterruptedException e) { break; }
            currentTime++;
        }
        finishSimulation();
    }

    private void updateGUI(int time) {
        List<Task> waitingCopy = new ArrayList<>();
        for (Task t : tasks) waitingCopy.add(new Task(t.getID(), t.getArrivalTime(), t.getServiceTime()));

        List<Server> serversSnapshot = new ArrayList<>();
        for (Server s : scheduler.getServers()) {
            Server temp = new Server();
            for (Task t : s.getTasks()) temp.addTask(new Task(t.getID(), t.getArrivalTime(), t.getServiceTime()));
            serversSnapshot.add(temp);
        }
        SwingUtilities.invokeLater(() -> frame.update(time, waitingCopy, serversSnapshot));
    }

    private void finishSimulation() {
        String stats = String.format("Average waiting time: %.2f\nAverage service time: %.2f\nPeak hour: %d with %d clients",
                (totalWaitingTime / numberOfClients), (totalServiceTime / numberOfClients), peakHour, maxClients);

        System.out.println(stats);
        writer.println(stats);
        writer.close();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setFinalStats(stats);
                frame.getStartButton().setEnabled(true);
            }
        });
        pool.shutdownNow();
    }

    private void writeLog(int time) {
        StringBuilder log = new StringBuilder("Time " + time + "\nWaiting: " + (tasks.isEmpty() ? "none" : tasks) + "\n");
        for (int i = 0; i < scheduler.getServers().size(); i++) {
            Task[] currentTasks = scheduler.getServers().get(i).getTasks();
            log.append("Queue ").append(i + 1).append(": ").append(currentTasks.length == 0 ? "closed" : Arrays.toString(currentTasks)).append("\n");
        }
        log.append("-------------------------------------------\n");
        System.out.print(log);
        writer.print(log);
        writer.flush();
    }

    private boolean areAllServersEmpty() {
        for (Server s : scheduler.getServers()) if (s.getTasks().length > 0) return false;
        return true;
    }

    public static void main(String[] args) {
        new SimulationManager();
    }
}