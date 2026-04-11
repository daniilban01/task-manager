package BusinessLogic;

import Model.Server;
import Model.Task;

import java.util.List;

public class ShortestQueueStrategy implements Strategy {
    @Override
    public int addTask(List<Server> servers, Task t){
        Server shortestServer = servers.getFirst();
        for(Server server:servers){
            if(server.getTasks().length < shortestServer.getTasks().length){
                shortestServer = server;
            }
        }
        int waitingPeriod = shortestServer.getWaitingPeriod().get();
        shortestServer.addTask(t);
        return waitingPeriod;
    }
}
