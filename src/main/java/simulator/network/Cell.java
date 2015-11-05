package simulator.network;

import java.util.HashSet;
import java.util.Set;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.network.Cell.State;

public abstract class Cell extends AbstractFSM<State, Data> {
    public enum State {
        Idle,
        Available,
        Down
    }

    public enum Events {
        ConnectToNetwork,
        DisconnectFromNetwork,
        ConnectCellAck,
        ConnectDevice,
        DisconnectDevice
    }

    private Set<ActorRef> subscribers = new HashSet<>();
    private ActorRef network;

    public void addSubscriber(ActorRef sender) {
        subscribers.add(sender);
    }

    public void removeSubscriber(ActorRef sender) {
        subscribers.remove(sender);
    }

    public ActorRef getNetwork() {
        return network;
    }

    public void setNetwork(ActorRef network) {
        this.network = network;
    }
}
