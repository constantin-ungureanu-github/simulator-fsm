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

        when(Off,
                matchEvent(PowerOn.class, (state, data) -> processPowerOn())
                .event(SendSMS.class, (event, data) -> stayAndSendAck())
                .event(MakeVoiceCall.class, (state, data) -> stayAndSendAck()));
        when(On,
                matchEvent(PowerOff.class, (state, data) -> processPowerOff())
                .event(ConnectToCell.class, (state, data) -> processConnectToCell())
                .event(AckConnectToCell.class, (state, data) -> processAckConnectToCell())
                .event(NAckConnectToCell.class, (event, data) -> processNAckConnectToCell())
                .event(DisconnectFromCell.class, (event, data) -> processDisconnectFromCell())
                .event(AckDisconnectFromCell.class, (event, data) -> processAckDisconnectFromCell())
                .event(SendSMS.class, (event, data) -> processSendSMS())
                .event(ReceiveSMS.class, (event, data) -> processReceiveSMS())
                .event(AckSendSMS.class, (event, data) -> processAckSendSMS())
                .event(NAckSendSMS.class, (state, data) -> processNAckSendSMS())
                .event(MakeVoiceCall.class, (state, data) -> processMakeVoiceCall())
                .event(ReceiveVoiceCall.class, (state, data) -> processReceiveVoiceCall())
                .event(AckMakeVoiceCall.class, (state, data) -> processAckMakeVoiceCall())
                .event(NAckMakeVoiceCall.class, (state, data) -> processNAckMakeVoiceCall()));

        whenUnhandled(
                matchEvent(PickedBySubscriber.class, (event, data) -> processPickedBySubscriber())
                .event(Master.Step.class, (step, data) -> processStep(step.getStep()))
                .anyEvent((event, data) -> processUnhandledEvent(event)));

        initialize();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processUnhandledEvent(Object event) {
        log.error("Unhandled event: {}", event);
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processPickedBySubscriber() {
        setSubscriber(sender());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> stayAndSendAck() {
        sender().tell(new RemoveWork(), self());
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processNAckMakeVoiceCall() {
        return stay();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processAckMakeVoiceCall() {
        return processNAckMakeVoiceCall();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processReceiveVoiceCall() {
        sender().tell(new AckMakeVoiceCall(), self());
        return processAckMakeVoiceCall();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processMakeVoiceCall() {
        log.info("", self().path().name(), cell.path().name());
        sender().tell(new RemoveWork(), self());
        return processAckMakeVoiceCall();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processNAckSendSMS() {
        Master.getMaster().tell(Master.Events.Ping, ActorRef.noSender());
        return processAckMakeVoiceCall();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processAckSendSMS() {
        return processNAckSendSMS();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processReceiveSMS() {
        sender().tell(new AckSendSMS(), self());
        return processAckMakeVoiceCall();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processSendSMS() {
        log.info("", self().path().name(), cell.path().name());
        sender().tell(new RemoveWork(), self());
        return processAckMakeVoiceCall();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processAckDisconnectFromCell() {
        setCell(null);
        return processAckSendSMS();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processDisconnectFromCell() {
        getCell().tell(new DisconnectDevice(), self());
        return processAckMakeVoiceCall();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processNAckConnectToCell() {
        return processAckSendSMS();
    }

    private akka.actor.FSM.State<simulator.actors.interfaces.State, simulator.actors.interfaces.Data> processAckConnectToCell() {
        setCell(sender());
        return processNAckConnectToCell();
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
        return processAckMakeVoiceCall();
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
