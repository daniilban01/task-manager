package BusinessLogic;

import Model.Server;
import Model.Task;

import java.util.List;

public class ShortestTimeStrategy implements Strategy {
    @Override
    public void addTask(List<Server> servers, Task t){
        Server shortestTimeServer = servers.getFirst();
        for(Server server:servers){
            if(server.getWaitingPeriod().get()<shortestTimeServer.getWaitingPeriod().get()){
                shortestTimeServer = server;
            }
        }
        shortestTimeServer.addTask(t);
    }
}
