package simulator.network._4G.LTE.VoLTE;

import static simulator.network.Cell.Events.ConnectCellAck;
import static simulator.network.Cell.Events.ConnectDevice;
import static simulator.network.Cell.Events.ConnectToNetwork;
import static simulator.network.Cell.State.Available;
import static simulator.network.Cell.State.Idle;

import simulator.Master;
import simulator.network.Device;
import simulator.network.Network;

public class ENodeB extends simulator.network.Cell {
    {
        startWith(Idle, null);

        when(Idle, matchEventEquals(ConnectToNetwork, (state, data) -> stay().replying(Network.Events.ConnectCell)));

        when(Idle, matchEventEquals(ConnectCellAck, (state, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Available);
        }));

        when(Idle, matchEventEquals(ConnectDevice, (state, data) -> {
            addDevice(sender());
            sender().tell(Device.Events.AckConnectToCell, self());
            return stay();
        }));

        when(Available, matchEventEquals(ConnectDevice, (state, data) -> {
            addDevice(sender());
            sender().tell(Device.Events.AckConnectToCell, self());
            return stay();
        }));

        when(Available, matchEventEquals(Events.DisconnectDevice, (state, data) -> {
            removeDevice(sender());
            sender().tell(Device.Events.AckDisconnectFromCell, self());
            return stay();
        }));

        initialize();
    }
}
