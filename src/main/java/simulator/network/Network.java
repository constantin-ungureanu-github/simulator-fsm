package simulator.network;

import static simulator.network.Network.State.Available;

import java.util.HashSet;
import java.util.Set;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.actors.events.CellEvents.ConnectCellAck;
import simulator.actors.events.CellEvents.DisconnectFromNetwork;
import simulator.actors.events.NetworkEvents.ConnectCell;
import simulator.actors.events.NetworkEvents.DisconnectCell;
import simulator.actors.events.NetworkEvents.RegisterDevice;
import simulator.actors.events.NetworkEvents.Routing;
import simulator.actors.events.NetworkEvents.UnregisterDevice;
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

        when(Available, matchEvent(ConnectCell.class, (event, data) -> {
            addCell(sender());
            sender().tell(new ConnectCellAck(), self());
            return stay();
        }).event(DisconnectCell.class, (event, data) -> {
            removeCell(sender());
            sender().tell(new DisconnectFromNetwork(), self());
            return stay();
        }).event(RegisterDevice.class, (event, data) -> {
            sender().tell(new ConnectCellAck(), self());
            return stay();
        }).event(UnregisterDevice.class, (event, data) -> {
            sender().tell(new DisconnectFromNetwork(), self());
            return stay();
        }).event(Routing.class, (event, data) -> {
            return stay();
        }));

        initialize();
    }
}
