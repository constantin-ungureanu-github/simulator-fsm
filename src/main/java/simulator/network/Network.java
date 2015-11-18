package simulator.network;

import static simulator.network.Network.NetworkEvents.ConnectCell;
import static simulator.network.Network.NetworkEvents.DisconnectCell;
import static simulator.network.Network.NetworkEvents.RegisterDevice;
import static simulator.network.Network.NetworkEvents.Routing;
import static simulator.network.Network.NetworkEvents.UnregisterDevice;
import static simulator.network.Network.State.Available;

import java.util.HashSet;
import java.util.Set;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.abstracts.TemplateData;
import simulator.abstracts.TemplateEvents;
import simulator.network.Network.State;
import simulator.network._2G.GSM.Cell;

public class Network extends AbstractFSM<State, TemplateData> {
    public enum State {
        Available
    }

    public enum NetworkEvents implements TemplateEvents {
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
