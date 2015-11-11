package simulator.network;

import static simulator.network.Network.Events.ConnectCell;
import static simulator.network.Network.Events.DisconnectCell;
import static simulator.network.Network.Events.RegisterDevice;
import static simulator.network.Network.Events.Routing;
import static simulator.network.Network.Events.UnregisterDevice;
import static simulator.network.Network.State.Available;

import java.util.HashSet;
import java.util.Set;

import simulator.network.Network.State;
import simulator.network._2G.GSM.Cell;
import akka.actor.AbstractFSM;
import akka.actor.ActorRef;

public class Network extends AbstractFSM<State, Data> {
    public enum State {
        Available
    }

    public enum Events {
        ConnectCell,
        DisconnectCell,
        RegisterDevice,
        UnregisterDevice,
        Routing
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

        when(Available, matchEventEquals(ConnectCell, (state, data) -> {
            addCell(sender());
            sender().tell(Cell.Events.ConnectCellAck, self());
            return stay();
        }));

        when(Available, matchEventEquals(DisconnectCell, (state, data) -> {
            removeCell(sender());
            sender().tell(Cell.Events.DisconnectFromNetwork, self());
            return stay();
        }));

        when(Available, matchEventEquals(RegisterDevice, (state, data) -> {
            sender().tell(Cell.Events.ConnectCellAck, self());
            return stay();
        }));

        when(Available, matchEventEquals(UnregisterDevice, (state, data) -> {
            sender().tell(Cell.Events.DisconnectFromNetwork, self());
            return stay();
        }));

        when(Available, matchEventEquals(Routing, (state, data) -> {
            return stay();
        }));

        initialize();
    }
}
