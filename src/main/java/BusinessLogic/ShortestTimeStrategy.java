package BusinessLogic;

import Model.Server;
import Model.Task;

import java.util.List;

public class ShortestTimeStrategy implements Strategy {
    @Override
    public int addTask(List<Server> servers, Task t){
        Server shortestTimeServer = servers.getFirst();
        for(Server server:servers){
            if(server.getWaitingPeriod().get() < shortestTimeServer.getWaitingPeriod().get()){
                shortestTimeServer = server;
            }
        }
        int waitingPeriod = shortestTimeServer.getWaitingPeriod().get();
        shortestTimeServer.addTask(t);
        return waitingPeriod;
    }
}
