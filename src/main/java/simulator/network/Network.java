package simulator.network;

import static simulator.network.Network.State.On;

import java.util.HashSet;
import java.util.Set;

import simulator.actors.abstracts.NE;
import simulator.actors.events.CellEvents.ConnectCellAck;
import simulator.actors.events.CellEvents.DisconnectFromNetwork;
import simulator.actors.events.NetworkEvents.ConnectCell;
import simulator.actors.events.NetworkEvents.DisconnectCell;
import simulator.actors.events.NetworkEvents.RegisterDevice;
import simulator.actors.events.NetworkEvents.Routing;
import simulator.actors.events.NetworkEvents.UnregisterDevice;
import akka.actor.ActorRef;

public class Network extends NE {
    public enum State implements simulator.actors.interfaces.State {
        On
    }

    private Set<ActorRef> cells = new HashSet<>();

    {
        startWith(On, null);

        when(On,
                matchEvent(ConnectCell.class, (event, data) -> processConnectCell(event))
                .event(DisconnectCell.class, (event, data) -> processDisconnectCell(event))
                .event(RegisterDevice.class, (event, data) -> processRegisterDevice(event))
                .event(UnregisterDevice.class, (event, data) -> processUnregisterDevice(event))
                .event(Routing.class, (event, data) -> processRouting()));

        initialize();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processConnectCell(ConnectCell event) {
        addCell(event.getSource());
        event.getSource().tell(new ConnectCellAck(self(), event.getSource(), null), ActorRef.noSender());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processDisconnectCell(DisconnectCell event) {
        removeCell(event.getSource());
        event.getSource().tell(new DisconnectFromNetwork(self(), event.getSource(), null), ActorRef.noSender());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processRegisterDevice(RegisterDevice event) {
        event.getSource().tell(new ConnectCellAck(self(), event.getSource(), null), ActorRef.noSender());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processUnregisterDevice(UnregisterDevice event) {
        event.getSource().tell(new DisconnectFromNetwork(self(), event.getSource(), null), ActorRef.noSender());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processRouting() {
        return stay();
    }

    public void addCell(ActorRef sender) {
        cells.add(sender);
    }

    public void removeCell(ActorRef sender) {
        cells.remove(sender);
    }
}
