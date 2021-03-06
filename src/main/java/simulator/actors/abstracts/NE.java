package simulator.actors.abstracts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import akka.actor.ActorRef;

public abstract class NE extends Actor {
    private Map<ActorRef, ActorRef> registeredNE = new HashMap<>();
    private Set<ActorRef> activeNE = new HashSet<>();

    protected ActorRef getRegisteredNe(ActorRef key) {
        return registeredNE.get(key);
    }

    protected void registerNE(ActorRef key, ActorRef value) {
        registeredNE.put(key, value);
    }

    protected void unRegisterNE(ActorRef key) {
        registeredNE.remove(key);
    }

    protected void unRegisterAllNE() {
        registeredNE = new HashMap<>();
    }

    protected void activateNE(ActorRef value) {
        activeNE.add(value);
    }

    protected void deActivateNE(ActorRef value) {
        activeNE.remove(value);
    }

    protected void deActivateAllNE(ActorRef value) {
        activeNE = new HashSet<>();
    }
}
