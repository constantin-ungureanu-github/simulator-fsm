package simulator.network;

import static simulator.network.Device.Events.AckConnectToCell;
import static simulator.network.Device.Events.AckDisconnectFromCell;
import static simulator.network.Device.Events.AckMakeVoiceCall;
import static simulator.network.Device.Events.AckSendSMS;
import static simulator.network.Device.Events.ConnectToCell;
import static simulator.network.Device.Events.DisconnectFromCell;
import static simulator.network.Device.Events.MakeVoiceCall;
import static simulator.network.Device.Events.NAckConnectToCell;
import static simulator.network.Device.Events.NAckMakeVoiceCall;
import static simulator.network.Device.Events.NAckSendSMS;
import static simulator.network.Device.Events.ReceiveSMS;
import static simulator.network.Device.Events.ReceiveVoiceCall;
import static simulator.network.Device.Events.SendSMS;
import static simulator.network.Device.State.Available;
import static simulator.network.Device.State.Off;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.Master;
import simulator.network.Device.State;
import simulator.network._2G.GSM.Cell;
import akka.actor.AbstractFSM;
import akka.actor.ActorRef;

public class Device extends AbstractFSM<State, Data> {
    private static Logger log = LoggerFactory.getLogger(Device.class);

    public enum State {
        Off,
        Airplane,
        Available
    }

    public enum Events {
        ConnectToCell,
        AckConnectToCell,
        NAckConnectToCell,
        DisconnectFromCell,
        AckDisconnectFromCell,
        SendSMS,
        ReceiveSMS,
        AckSendSMS,
        NAckSendSMS,
        MakeVoiceCall,
        ReceiveVoiceCall,
        AckMakeVoiceCall,
        NAckMakeVoiceCall
    }

    private ActorRef cell;

    public ActorRef getCell() {
        return cell;
    }

    public void setCell(ActorRef cell) {
        this.cell = cell;
    }

    {
        startWith(Off, null);

        when(Off, matchEventEquals(ConnectToCell, (state, data) -> {
            sender().tell(Cell.Events.ConnectDevice, self());
            return stay();
        }));

        when(Off, matchEventEquals(AckConnectToCell, (state, data) -> {
            setCell(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Available);
        }));

        when(Off, matchEventEquals(NAckConnectToCell, (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(NAckConnectToCell, (state, data) -> {
            setCell(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Off);
        }));

        when(Available, matchEventEquals(DisconnectFromCell, (state, data) -> {
            getCell().tell(Cell.Events.DisconnectDevice, self());
            return stay();
        }));

        when(Available, matchEventEquals(AckDisconnectFromCell, (state, data) -> {
            setCell(null);
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Off);
        }));

        when(Available, matchEventEquals(SendSMS, (state, data) -> {
            sender().tell(ReceiveSMS, self());
            return stay();
        }));

        when(Available, matchEventEquals(ReceiveSMS, (state, data) -> {
            sender().tell(AckSendSMS, self());
            return stay();
        }));

        when(Available, matchEventEquals(AckSendSMS, (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(NAckSendSMS, (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(MakeVoiceCall, (state, data) -> {
            sender().tell(ReceiveVoiceCall, self());
            return stay();
        }));

        when(Available, matchEventEquals(ReceiveVoiceCall, (state, data) -> {
            sender().tell(AckMakeVoiceCall, self());
            return stay();
        }));

        when(Available, matchEventEquals(AckMakeVoiceCall, (state, data) -> {
            log.info("{} made voice call using cell {}", self().path().name(), cell.path().name());
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(NAckMakeVoiceCall, (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        initialize();
    }
}
