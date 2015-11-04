package simulator.actors;

import static simulator.actors.Cell.State.Available;
import static simulator.actors.Cell.State.Idle;

import java.util.HashSet;
import java.util.Set;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import simulator.actors.Cell.State;

public class Cell extends AbstractFSM<State, Data> {
    public enum State {
        Idle,
        Available,
        Down
    }

    public enum Events {
        ConnectToNetwork,
        DisconnectFromNetwork,
        ConnectCellAck,
        ConnectSubscriber,
        DisconnectSubscriber
    }

    private Set<ActorRef> subscribers = new HashSet<>();
    private ActorRef network;

    public void addSubscriber(ActorRef sender) {
        subscribers.add(sender);
    }

    public void removeSubscriber(ActorRef sender) {
        subscribers.remove(sender);
    }

    public ActorRef getNetwork() {
        return network;
    }

    public void setNetwork(ActorRef network) {
        this.network = network;
    }

    {
        startWith(Idle, null);

        when(Idle, matchEventEquals(Events.ConnectToNetwork, (state, data) -> {
            sender().tell(Network.Events.ConnectCell, self());
            return stay();
        }));

        when(Idle, matchEventEquals(Events.ConnectCellAck, (state, data) -> {
            setNetwork(sender());
            Master.getMaster().tell(Master.Events.Ping, self());
            return goTo(Available);
        }));

        when(Idle, matchEventEquals(Events.ConnectSubscriber, (state, data) -> {
            addSubscriber(sender());
            sender().tell(Subscriber.Messages.AckConnectToCell, self());
            return stay();
        }));

        when(Available, matchEventEquals(Events.ConnectSubscriber, (state, data) -> {
            addSubscriber(sender());
            sender().tell(Subscriber.Messages.AckConnectToCell, self());
            return stay();
        }));

        when(Available, matchEventEquals(Events.DisconnectSubscriber, (state, data) -> {
            removeSubscriber(sender());
            sender().tell(Subscriber.Messages.AckDisconnectFromCell, self());
            return stay();
        }));

        initialize();
    }
}
