package simulator.network;

import static simulator.network.UE.State.Off;
import static simulator.network.UE.State.On;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import simulator.actors.Master;
import simulator.actors.abstracts.Actor;
import simulator.actors.events.CellEvents.ConnectDevice;
import simulator.actors.events.CellEvents.DisconnectDevice;
import simulator.actors.events.DeviceEvents.AckConnectToCell;
import simulator.actors.events.DeviceEvents.AckDisconnectFromCell;
import simulator.actors.events.DeviceEvents.AckMakeVoiceCall;
import simulator.actors.events.DeviceEvents.AckSendSMS;
import simulator.actors.events.DeviceEvents.ConnectToCell;
import simulator.actors.events.DeviceEvents.DisconnectFromCell;
import simulator.actors.events.DeviceEvents.MakeVoiceCall;
import simulator.actors.events.DeviceEvents.NAckConnectToCell;
import simulator.actors.events.DeviceEvents.NAckMakeVoiceCall;
import simulator.actors.events.DeviceEvents.NAckSendSMS;
import simulator.actors.events.DeviceEvents.PickedBySubscriber;
import simulator.actors.events.DeviceEvents.PowerOff;
import simulator.actors.events.DeviceEvents.PowerOn;
import simulator.actors.events.DeviceEvents.ReceiveSMS;
import simulator.actors.events.DeviceEvents.ReceiveVoiceCall;
import simulator.actors.events.DeviceEvents.SendSMS;
import simulator.actors.events.DiscreteEvent;
import simulator.actors.events.DiscreteEvent.RemoveWork;

public class UE extends Actor {
    private static Logger log = LoggerFactory.getLogger(UE.class);

    public enum State implements simulator.actors.interfaces.State {
        Off, On, Airplane, InCall, InDataSession, InCallAndDataSession
    }

    private ActorRef cell, subscriber;

    {
        startWith(On, null);
        scheduleEvent((long) ThreadLocalRandom.current().nextInt(1, 20), new PowerOff());

        when(Off, matchEvent(PowerOn.class, (state, data) -> processPowerOn()));
        when(On, matchEvent(PowerOff.class, (state, data) -> processPowerOff()));

        when(On, matchEvent(ConnectToCell.class, (state, data) -> processConnectToCell())
        .event(AckConnectToCell.class, (state, data) -> {
            setCell(sender());
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
            return stay();
        }).event(NAckConnectToCell.class, (event, data) -> {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
            return stay();
        }).event(DisconnectFromCell.class, (event, data) -> {
            getCell().tell(new DisconnectDevice(), self());
            return stay();
        }).event(AckDisconnectFromCell.class, (event, data) -> {
            setCell(null);
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
            return stay();
        }).event(SendSMS.class, (event, data) -> {
            log.info("", self().path().name(), cell.path().name());
            sender().tell(new RemoveWork(), self());
            return stay();
        }).event(ReceiveSMS.class, (event, data) -> {
            sender().tell(new AckSendSMS(), self());
            return stay();
        }).event(AckSendSMS.class, (event, data) -> {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
            return stay();
        }).event(NAckSendSMS.class, (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
            return stay();
        }).event(MakeVoiceCall.class, (state, data) -> {
            log.info("", self().path().name(), cell.path().name());
            sender().tell(new RemoveWork(), self());
            return stay();
        }).event(ReceiveVoiceCall.class, (state, data) -> {
            sender().tell(new AckMakeVoiceCall(), self());
            return stay();
        }).event(AckMakeVoiceCall.class, (state, data) -> {
            return stay();
        }).event(NAckMakeVoiceCall.class, (state, data) -> {
            return stay();
        }));

        when(Off, matchEvent(SendSMS.class, (event, data) -> {
            sender().tell(new DiscreteEvent.RemoveWork(null, null, null), self());
            return stay();
        }).event(MakeVoiceCall.class, (state, data) -> {
            sender().tell(new DiscreteEvent.RemoveWork(null, null, null), self());
            return stay();
        }));

        whenUnhandled(matchEvent(PickedBySubscriber.class, (state, data) -> {
            setSubscriber(sender());
            return stay();
        }).event(Master.Step.class, (step, data) -> processStep(step.getStep())).anyEvent((event, data) -> {
            log.error("Unhandled event: {}", event);
            return stay();
        }));

        initialize();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processPowerOn() {
        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(50, 60), new PowerOff());
        removeWork();
        return goTo(On);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processPowerOff() {
        scheduleEvent(getStep() + ThreadLocalRandom.current().nextInt(0, 10), new PowerOn());
        removeWork();
        return goTo(Off);
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processConnectToCell() {
        sender().tell(new ConnectDevice(), self());
        return stay();
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
