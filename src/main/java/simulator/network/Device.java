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
import static simulator.network.Device.Events.PickedBySubscriber;
import static simulator.network.Device.Events.ReceiveSMS;
import static simulator.network.Device.Events.ReceiveVoiceCall;
import static simulator.network.Device.Events.SendSMS;
import static simulator.network.Device.State.Off;
import static simulator.network.Device.State.On;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.Master;
import simulator.Subscriber;
import simulator.network.Device.State;
import simulator.network._2G.GSM.Cell;
import akka.actor.AbstractFSM;
import akka.actor.ActorRef;

public class Device extends AbstractFSM<State, Data> {
    private static Logger log = LoggerFactory.getLogger(Device.class);

    public enum State {
        Off,
        On,
        Airplane
    }

    public enum Events {
        PickedBySubscriber,
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
    private ActorRef subscriber;

    public ActorRef getCell() {
        return cell;
    }

    public void setCell(ActorRef cell) {
        this.cell = cell;
    }

    public ActorRef getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(ActorRef subscriber) {
        this.subscriber = subscriber;
    }

    {
        startWith(Off, null);

        when(Off, matchEvent(Events.class, (event, data) -> (event == ConnectToCell), (state, data) -> {
            sender().tell(Cell.Events.ConnectDevice, self());
            return stay();
        }).event(Events.class, (event, data) -> (event == AckConnectToCell), (state, data) -> {
            setCell(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(On);
        }).event(Events.class, (event, data) -> (event == NAckConnectToCell), (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(On, matchEvent(Events.class, (event, data) -> (event == NAckConnectToCell), (state, data) -> {
            setCell(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Off);
        }).event(Events.class, (event, data) -> (event == DisconnectFromCell), (state, data) -> {
            getCell().tell(Cell.Events.DisconnectDevice, self());
            return stay();
        }).event(Events.class, (event, data) -> (event == AckDisconnectFromCell), (state, data) -> {
            setCell(null);
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Off);
        }).event(Events.class, (event, data) -> (event == SendSMS), (state, data) -> {
            log.info("{} sent SMS using cell {}", self().path().name(), cell.path().name());
            sender().tell(Subscriber.DiscreteEvent.RemoveWork, self());
            return stay();
        }).event(Events.class, (event, data) -> (event == ReceiveSMS), (state, data) -> {
            sender().tell(AckSendSMS, self());
            return stay();
        }).event(Events.class, (event, data) -> (event == AckSendSMS), (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }).event(Events.class, (event, data) -> (event == NAckSendSMS), (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }).event(Events.class, (event, data) -> (event == MakeVoiceCall), (state, data) -> {
            log.info("{} made voice call using cell {}", self().path().name(), cell.path().name());
            sender().tell(Subscriber.DiscreteEvent.RemoveWork, self());
            return stay();
        }).event(Events.class, (event, data) -> (event == ReceiveVoiceCall), (state, data) -> {
            sender().tell(AckMakeVoiceCall, self());
            return stay();
        }).event(Events.class, (event, data) -> (event == AckMakeVoiceCall), (state, data) -> {
            return stay();
        }).event(Events.class, (event, data) -> (event == NAckMakeVoiceCall), (state, data) -> {
            return stay();
        }));

        whenUnhandled(matchEventEquals(PickedBySubscriber, (state, data) -> {
            setSubscriber(sender());
            return stay();
        }).anyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }
}
