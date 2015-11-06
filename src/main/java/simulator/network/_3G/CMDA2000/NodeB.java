package simulator.network._3G.CMDA2000;

import static simulator.network.Cell.Events.ConnectCellAck;
import static simulator.network.Cell.Events.ConnectDevice;
import static simulator.network.Cell.Events.ConnectToNetwork;
import static simulator.network.Cell.Events.DisconnectDevice;
import static simulator.network.Cell.State.Available;
import static simulator.network.Cell.State.Idle;

import java.util.HashSet;
import java.util.Set;

import simulator.Master;
import simulator.network.Device;
import simulator.network.Network;
import akka.actor.ActorRef;

public class NodeB extends simulator.network.Cell {
    private Set<ActorRef> subscribers = new HashSet<>();
    private ActorRef network;

    public void addDevice(ActorRef sender) {
        subscribers.add(sender);
    }

    public void removeDevice(ActorRef sender) {
        subscribers.remove(sender);
    }

    public ActorRef getNetwork() {
        return network;
    }

    public void setNetwork(ActorRef network) {
        this.network = network;
    }

    {
        startWith(Idle, null);

        when(Idle, matchEventEquals(ConnectToNetwork, (state, data) -> stay().replying(Network.Events.ConnectCell)));

        when(Idle, matchEventEquals(ConnectCellAck, (state, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Available);
        }));

        when(Idle, matchEventEquals(Events.ConnectDevice, (state, data) -> {
            addDevice(sender());
            sender().tell(Device.Events.AckConnectToCell, self());
            return stay();
        }));

        when(Available, matchEventEquals(ConnectDevice, (state, data) -> {
            addDevice(sender());
            sender().tell(Device.Events.AckConnectToCell, self());
            return stay();
        }));

        when(Available, matchEventEquals(DisconnectDevice, (state, data) -> {
            removeDevice(sender());
            sender().tell(Device.Events.AckDisconnectFromCell, self());
            return stay();
        }));

        initialize();
    }
}