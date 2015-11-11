package simulator.network._2G.GSM;

import static simulator.network.Cell.Events.ConnectCellAck;
import static simulator.network.Cell.Events.ConnectDevice;
import static simulator.network.Cell.Events.ConnectToNetwork;
import static simulator.network.Cell.Events.DisconnectDevice;
import static simulator.network.Cell.State.Off;
import static simulator.network.Cell.State.On;
import simulator.Master;
import simulator.network.UE;
import simulator.network.Network;

public class Cell extends simulator.network.Cell {
    {
        startWith(Off, null);

        when(Off, matchEvent(Events.class, (event, data) -> (event == ConnectToNetwork), (event, data) -> {
            sender().tell(Network.Events.ConnectCell, self());
            return stay();
        }).event(Events.class, (event, data) -> (event == ConnectCellAck), (event, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(On);
        }).event(Events.class, (event, data) -> (event == ConnectDevice), (event, data) -> {
            addDevice(sender());
            sender().tell(UE.Events.AckConnectToCell, self());
            return stay();
        }).anyEvent((event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }).event(Events.class, (event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        when(On, matchEvent(Events.class, (event, data) -> (event == ConnectDevice), (state, data) -> {
            addDevice(sender());
            sender().tell(UE.Events.AckConnectToCell, self());
            return stay();
        }).event(Events.class, (event, data) -> (event == DisconnectDevice), (event, data) -> {
            removeDevice(sender());
            sender().tell(UE.Events.AckDisconnectFromCell, self());
            return stay();
        }).anyEvent((event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }).event(Events.class, (event, state) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        whenUnhandled(matchAnyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }
}
