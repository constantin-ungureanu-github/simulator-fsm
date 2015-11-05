package simulator.network._3G.UMTS.WCMDA;

import static simulator.network.Cell.Events.ConnectCellAck;
import static simulator.network.Cell.Events.ConnectDevice;
import static simulator.network.Cell.Events.ConnectToNetwork;
import static simulator.network.Cell.Events.DisconnectDevice;
import static simulator.network.Cell.State.Available;
import static simulator.network.Cell.State.Idle;

import simulator.Master;
import simulator.network.Device;
import simulator.network.Network;

public class Cell extends simulator.network.Cell {
    {
        startWith(Idle, null);

        when(Idle, matchEventEquals(ConnectToNetwork, (state, data) -> stay().replying(Network.Events.ConnectCell)));

        when(Idle, matchEventEquals(ConnectCellAck, (state, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Available);
        }));

        when(Idle, matchEventEquals(ConnectDevice, (state, data) -> {
            addSubscriber(sender());
            sender().tell(Device.Events.AckConnectToCell, self());
            return stay();
        }));

        when(Available, matchEventEquals(ConnectDevice, (state, data) -> {
            addSubscriber(sender());
            sender().tell(Device.Events.AckConnectToCell, self());
            return stay();
        }));

        when(Available, matchEventEquals(DisconnectDevice, (state, data) -> {
            removeSubscriber(sender());
            sender().tell(Device.Events.AckDisconnectFromCell, self());
            return stay();
        }));

        initialize();
    }
}
