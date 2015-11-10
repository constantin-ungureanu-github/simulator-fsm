package simulator.network;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.network.Cell.State;
import akka.actor.AbstractFSM;
import akka.actor.ActorRef;

public abstract class Cell extends AbstractFSM<State, Data> {
    protected static Logger log = LoggerFactory.getLogger(Cell.class);

    public enum State {
        Off,
        On
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
