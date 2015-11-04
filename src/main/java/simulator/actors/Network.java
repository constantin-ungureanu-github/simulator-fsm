package simulator.actors;

import static simulator.actors.Network.State.Available;

import java.util.HashSet;
import java.util.Set;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.actors.Network.Data;
import simulator.actors.Network.State;

public class Network extends AbstractFSM<State, Data> {
    public enum State {
        Available
    }

    public enum Events {
        ConnectCell,
        DisconnectCell
    }

    public static final class Data {
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

        when(Available, matchEventEquals(Events.ConnectCell, (state, data) -> {
            addCell(sender());
            sender().tell(Cell.Events.ConnectCellAck, self());
            return stay();
        }));

        when(Available, matchEventEquals(Events.DisconnectCell, (state, data) -> {
            removeCell(sender());
            sender().tell(Cell.Events.DisconnectFromNetwork, self());
            return stay();
        }));

        initialize();
    }
}
