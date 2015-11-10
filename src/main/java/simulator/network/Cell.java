package simulator.network;

import static simulator.network.Cell.State.Idle;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.network.Cell.State;

public abstract class Cell extends AbstractFSM<State, Data> {
    private static Logger log = LoggerFactory.getLogger(Cell.class);
    {
        startWith(Idle, null);

        whenUnhandled(matchAnyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }

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
