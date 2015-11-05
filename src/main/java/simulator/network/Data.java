package simulator.network;

import akka.actor.ActorRef;

public class Data {
    private ActorRef source;
    private ActorRef destination;

    public Data(ActorRef source, ActorRef destination) {
        setSource(source);
        setDestination(destination);
    }

    public ActorRef getSource() {
        return source;
    }

    public void setSource(ActorRef source) {
        this.source = source;
    }

    public ActorRef getDestination() {
        return destination;
    }

    public void setDestination(ActorRef destination) {
        this.destination = destination;
    }
}
