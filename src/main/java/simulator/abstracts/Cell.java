package simulator.abstracts;

import java.util.HashSet;
import java.util.Set;

import akka.actor.ActorRef;

public abstract class Cell extends NE {
    public enum State implements TemplateState {
        On,
        Off
    }

    public enum Events implements TemplateEvents {
        ConnectToNetwork,
        DisconnectFromNetwork,
        ConnectDevice,
        DisconnectDevice,
        ConnectCellAck
    }

    private Set<ActorRef> devices = new HashSet<>();
    private ActorRef network;

    protected void addDevice(ActorRef sender) {
        devices.add(sender);
    }

    protected void removeDevice(ActorRef sender) {
        devices.remove(sender);
    }

    protected ActorRef getNetwork() {
        return network;
    }

    protected void setNetwork(ActorRef network) {
        this.network = network;
    }
}
