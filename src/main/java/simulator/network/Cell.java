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

    private Set<ActorRef> devices = new HashSet<>();
    private ActorRef network;

    public void addDevice(ActorRef sender) {
        devices.add(sender);
    }

    public void removeDevice(ActorRef sender) {
        devices.remove(sender);
    }

    public ActorRef getNetwork() {
        return network;
    }

    public void setNetwork(ActorRef network) {
        this.network = network;
    }
}
