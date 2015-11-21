package simulator.network;

import static simulator.actors.events.CellEvents.ConnectDevice;
import static simulator.actors.events.CellEvents.DisconnectDevice;
import static simulator.actors.events.DeviceEvents.AckConnectToCell;
import static simulator.actors.events.DeviceEvents.AckDisconnectFromCell;
import static simulator.actors.events.DeviceEvents.AckMakeVoiceCall;
import static simulator.actors.events.DeviceEvents.AckSendSMS;
import static simulator.actors.events.DeviceEvents.ConnectToCell;
import static simulator.actors.events.DeviceEvents.DisconnectFromCell;
import static simulator.actors.events.DeviceEvents.MakeVoiceCall;
import static simulator.actors.events.DeviceEvents.NAckConnectToCell;
import static simulator.actors.events.DeviceEvents.NAckMakeVoiceCall;
import static simulator.actors.events.DeviceEvents.NAckSendSMS;
import static simulator.actors.events.DeviceEvents.PickedBySubscriber;
import static simulator.actors.events.DeviceEvents.PowerOff;
import static simulator.actors.events.DeviceEvents.PowerOn;
import static simulator.actors.events.DeviceEvents.ReceiveSMS;
import static simulator.actors.events.DeviceEvents.ReceiveVoiceCall;
import static simulator.actors.events.DeviceEvents.SendSMS;
import static simulator.actors.events.DiscreteEvent.RemoveWork;
import static simulator.network.UE.State.Off;
import static simulator.network.UE.State.On;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import simulator.actors.Master;
import simulator.actors.abstracts.Actor;
import simulator.actors.events.DeviceEvents;
import simulator.actors.interfaces.TemplateData;
import simulator.actors.interfaces.TemplateState;
import simulator.network.UE.Data;
import simulator.network.UE.State;

public class UE extends Actor<State, Data> {
    private static Logger log = LoggerFactory.getLogger(UE.class);

    public enum State implements simulator.actors.interfaces.TemplateState {
        Off, On, Airplane, InCall, InDataSession, InCallAndDataSession
    }

    public class Data implements simulator.actors.interfaces.TemplateData {
    }

    private ActorRef cell, subscriber;

    {
        startWith(On, null);
        scheduleEvent((long) ThreadLocalRandom.current().nextInt(1, 20), PowerOff);

        when(Off, matchEventEquals(PowerOn, (state, data) -> processPowerOn()));
        when(On, matchEventEquals(PowerOff, (state, data) -> processPowerOff()));

        when(On, matchEvent((event, data) -> (event == ConnectToCell), (state, data) -> {
            sender().tell(ConnectDevice, self());
            return stay();
        }).event((event, data) -> (event == AckConnectToCell), (state, data) -> {
            setCell(sender());
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
            return stay();
        }).event((event, data) -> (event == NAckConnectToCell), (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
            return stay();
        }).event((event, data) -> (event == DisconnectFromCell), (state, data) -> {
            getCell().tell(DisconnectDevice, self());
            return stay();
        }).event((event, data) -> (event == AckDisconnectFromCell), (state, data) -> {
            setCell(null);
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
            return stay();
        }).event((event, data) -> (event == DeviceEvents.SendSMS), (state, data) -> {
            log.info("", self().path().name(), cell.path().name());
            sender().tell(RemoveWork, self());
            return stay();
        }).event((event, data) -> (event == ReceiveSMS), (state, data) -> {
            sender().tell(AckSendSMS, self());
            return stay();
        }).event((event, data) -> (event == AckSendSMS), (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
            return stay();
        }).event((event, data) -> (event == NAckSendSMS), (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
            return stay();
        }).event((event, data) -> (event == MakeVoiceCall), (state, data) -> {
            log.info("", self().path().name(), cell.path().name());
            sender().tell(RemoveWork, self());
            return stay();
        }).event((event, data) -> (event == ReceiveVoiceCall), (state, data) -> {
            sender().tell(AckMakeVoiceCall, self());
            return stay();
        }).event((event, data) -> (event == AckMakeVoiceCall), (state, data) -> {
            return stay();
        }).event((event, data) -> (event == NAckMakeVoiceCall), (state, data) -> {
            return stay();
        }));

        when(Off, matchEvent((event, data) -> (event == SendSMS), (state, data) -> {
            sender().tell(RemoveWork, self());
            return stay();
        }).event((event, data) -> (event == MakeVoiceCall), (state, data) -> {
            sender().tell(RemoveWork, self());
            return stay();
        }));

        whenUnhandled(matchEventEquals(PickedBySubscriber, (state, data) -> {
            setSubscriber(sender());
            return stay();
        }).event(Master.Step.class, (step, data) -> processStep(step.getStep())).anyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> processPowerOn() {
        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(50, 60), PowerOff);
        removeWork();
        return goTo(On);
    }

    private akka.actor.FSM.State<TemplateState, TemplateData> processPowerOff() {
        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(0, 10), PowerOn);
        removeWork();
        return goTo(Off);
    }

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
}
