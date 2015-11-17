package simulator.network;

import static simulator.network.UE.Events.AckConnectToCell;
import static simulator.network.UE.Events.AckDisconnectFromCell;
import static simulator.network.UE.Events.AckMakeVoiceCall;
import static simulator.network.UE.Events.AckSendSMS;
import static simulator.network.UE.Events.ConnectToCell;
import static simulator.network.UE.Events.DisconnectFromCell;
import static simulator.network.UE.Events.MakeVoiceCall;
import static simulator.network.UE.Events.NAckConnectToCell;
import static simulator.network.UE.Events.NAckMakeVoiceCall;
import static simulator.network.UE.Events.NAckSendSMS;
import static simulator.network.UE.Events.PickedBySubscriber;
import static simulator.network.UE.Events.ReceiveSMS;
import static simulator.network.UE.Events.ReceiveVoiceCall;
import static simulator.network.UE.Events.SendSMS;
import static simulator.network.UE.State.Off;
import static simulator.network.UE.State.On;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.actors.Master;
import simulator.actors.Subscriber;
import simulator.network.NE.Data;
import simulator.network.UE.State;
import simulator.network._2G.GSM.Cell;

public class UE extends AbstractFSM<State, Data> {
    private static Logger log = LoggerFactory.getLogger(UE.class);

    public enum State {
        Off, On, Airplane, InCall, InDataSession
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
        NAckMakeVoiceCall,
        RequestDataSession,
        AckRequestDataSession,
        NAckRequestDataSession
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

        when(Off, matchEvent((event, data) -> (event == ConnectToCell), (state, data) -> {
            sender().tell(Cell.Events.ConnectDevice, self());
            return stay();
        }).event((event, data) -> (event == AckConnectToCell), (state, data) -> {
            setCell(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(On);
        }).event((event, data) -> (event == NAckConnectToCell), (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(On, matchEvent((event, data) -> (event == NAckConnectToCell), (state, data) -> {
            setCell(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Off);
        }).event((event, data) -> (event == DisconnectFromCell), (state, data) -> {
            getCell().tell(Cell.Events.DisconnectDevice, self());
            return stay();
        }).event((event, data) -> (event == AckDisconnectFromCell), (state, data) -> {
            setCell(null);
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Off);
        }).event((event, data) -> (event == SendSMS), (state, data) -> {
            log.info("{} sent SMS using cell {}", self().path().name(), cell.path().name());
            sender().tell(Subscriber.DiscreteEvent.RemoveWork, self());
            return stay();
        }).event((event, data) -> (event == ReceiveSMS), (state, data) -> {
            sender().tell(AckSendSMS, self());
            return stay();
        }).event((event, data) -> (event == AckSendSMS), (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }).event((event, data) -> (event == NAckSendSMS), (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }).event((event, data) -> (event == MakeVoiceCall), (state, data) -> {
            log.info("", self().path().name(), cell.path().name());
            sender().tell(Subscriber.DiscreteEvent.RemoveWork, self());
            return stay();
        }).event((event, data) -> (event == ReceiveVoiceCall), (state, data) -> {
            sender().tell(AckMakeVoiceCall, self());
            return stay();
        }).event((event, data) -> (event == AckMakeVoiceCall), (state, data) -> {
            return stay();
        }).event((event, data) -> (event == NAckMakeVoiceCall), (state, data) -> {
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
