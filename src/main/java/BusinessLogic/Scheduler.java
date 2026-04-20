package BusinessLogic;

import Model.Server;
import Model.Task;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private List<Server> servers;
    private int maxNoServers;
    public int maxTasksPerServer;
    private Strategy strategy;

    public void setServers(List<Server> servers){
        this.servers = servers;
    }

    public List<Server> getServers(){
        return servers;
    }

    public void setMaxNoServers(int maxNoServers){
        this.maxNoServers = maxNoServers;
    }

    public int getMaxNoServers(){
        return maxNoServers;
    }

    public void setMaxTasksPerServer(int maxTasksPerServer){
        this.maxTasksPerServer = maxTasksPerServer;
    }

    public int getMaxTasksPerServer(){
        return maxTasksPerServer;
    }

    public void setStrategy(Strategy strategy){
        this.strategy = strategy;
    }

    public Strategy getStrategy(){
        return strategy;
    }

    public Scheduler(int maxNoServers, int maxTasksPerServer){
        this.maxNoServers = maxNoServers;
        this.maxTasksPerServer = maxTasksPerServer;
        this.servers = new ArrayList<Server>();
        for(int i=0 ; i<maxNoServers ; i++) {
            servers.add(new Server());
        }
    }

    public void changeStrategy(SelectionPolicy policy){
        if(policy == SelectionPolicy.SHORTEST_QUEUE){
            strategy = new ShortestQueueStrategy();
        }
        if(policy == SelectionPolicy.SHORTEST_TIME){
            strategy = new ShortestTimeStrategy();
        }
    }

    public int dispatchTask(Task t){
        return strategy.addTask(servers, t);
    }

}
