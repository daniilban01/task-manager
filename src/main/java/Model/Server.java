package Model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private BlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod;

    public void setTasks(BlockingQueue<Task> tasks) {
        this.tasks = tasks;
    }

    public Task[] getTasks(){
        return tasks.toArray(new Task[tasks.size()]);
    }

    public void setWaitingPeriod(AtomicInteger waitingPeriod) {
        this.waitingPeriod = waitingPeriod;
    }

    public AtomicInteger getWaitingPeriod() {
        return waitingPeriod;
    }

    public Server(){
        tasks = new LinkedBlockingQueue<>();
        waitingPeriod = new AtomicInteger(0);
    }

    public void addTask(Task newTask){
        tasks.add(newTask);
        waitingPeriod.addAndGet(newTask.getServiceTime());
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()){
            try {
                Task temp = tasks.peek();
                if(temp != null){
                    Thread.sleep(1000L);
                    temp.setServiceTime(temp.getServiceTime() - 1);
                    waitingPeriod.decrementAndGet();
                    if(temp.getServiceTime() <= 0) {
                        tasks.poll();
                    }
                }else {
                    Thread.sleep(100);
                }
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
