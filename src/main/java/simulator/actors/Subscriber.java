package simulator.actors;

import static simulator.actors.Subscriber.State.Available;
import static simulator.actors.Subscriber.State.Idle;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.actors.Subscriber.State;

public class Subscriber extends AbstractFSM<State, Data> {
    public enum State {
        Idle,
        Available
    }

    public enum Messages {
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
        startWith(Idle, null);

        when(Idle, matchEventEquals(Messages.ConnectToCell, (state, data) -> {
            sender().tell(Cell.Events.ConnectSubscriber, self());
            return stay();
        }));

        when(Idle, matchEventEquals(Messages.AckConnectToCell, (state, data) -> {
            setCell(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Available);
        }));

        when(Idle, matchEventEquals(Messages.NAckConnectToCell, (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(Messages.NAckConnectToCell, (state, data) -> {
            setCell(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Idle);
        }));

        when(Available, matchEventEquals(Messages.DisconnectFromCell, (state, data) -> {
            getCell().tell(Cell.Events.DisconnectSubscriber, self());
            return stay();
        }));

        when(Available, matchEventEquals(Messages.AckDisconnectFromCell, (state, data) -> {
            setCell(null);
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Idle);
        }));

        when(Available, matchEventEquals(Messages.SendSMS, (state, data) -> {
            sender().tell(Messages.ReceiveSMS, self());
            return stay();
        }));

        when(Available, matchEventEquals(Messages.ReceiveSMS, (state, data) -> {
            sender().tell(Messages.AckSendSMS, self());
            return stay();
        }));

        when(Available, matchEventEquals(Messages.AckSendSMS, (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(Messages.NAckSendSMS, (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(Messages.MakeVoiceCall, (state, data) -> {
            sender().tell(Messages.ReceiveVoiceCall, self());
            return stay();
        }));

        when(Available, matchEventEquals(Messages.ReceiveVoiceCall, (state, data) -> {
            sender().tell(Messages.AckMakeVoiceCall, self());
            return stay();
        }));

        when(Available, matchEventEquals(Messages.AckMakeVoiceCall, (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        when(Available, matchEventEquals(Messages.NAckMakeVoiceCall, (state, data) -> {
            Master.getMaster().tell(Master.Events.Ping, self());
            return stay();
        }));

        initialize();
    }
}
