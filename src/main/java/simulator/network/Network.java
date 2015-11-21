package simulator.network;

import static simulator.actors.events.CellEvents.ConnectCellAck;
import static simulator.actors.events.CellEvents.DisconnectFromNetwork;
import static simulator.actors.events.NetworkEvents.ConnectCell;
import static simulator.actors.events.NetworkEvents.DisconnectCell;
import static simulator.actors.events.NetworkEvents.RegisterDevice;
import static simulator.actors.events.NetworkEvents.Routing;
import static simulator.actors.events.NetworkEvents.UnregisterDevice;
import static simulator.network.Network.State.Available;

import java.util.HashSet;
import java.util.Set;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.actors.interfaces.Data;
import simulator.network.Network.State;

public class Network extends AbstractFSM<State, Data> {
    public enum State implements simulator.actors.interfaces.State {
        Available
    }

    private Set<ActorRef> cells = new HashSet<>();

    public void addCell(ActorRef sender) {
        cells.add(sender);
    }

    public void removeCell(ActorRef sender) {
        cells.remove(sender);
    }

    {
        startWith(Available, null);

        when(Available, matchEventEquals(ConnectCell, (event, data) -> {
            addCell(sender());
            sender().tell(ConnectCellAck, self());
            return stay();
        }));

        when(Available, matchEventEquals(DisconnectCell, (event, data) -> {
            removeCell(sender());
            sender().tell(DisconnectFromNetwork, self());
            return stay();
        }));

        when(Available, matchEventEquals(RegisterDevice, (event, data) -> {
            sender().tell(ConnectCellAck, self());
            return stay();
        }));

        when(Available, matchEventEquals(UnregisterDevice, (event, data) -> {
            sender().tell(DisconnectFromNetwork, self());
            return stay();
        }));

        when(Available, matchEventEquals(Routing, (event, data) -> {
            return stay();
        }));

        initialize();
    }
}
