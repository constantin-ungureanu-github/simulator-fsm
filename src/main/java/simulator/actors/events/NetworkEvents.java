package simulator.actors.events;

import simulator.actors.interfaces.EventInterface;

public enum NetworkEvents implements EventInterface {
    SendSMS, MakeVoiceCall, RequestDataSession,
    ConnectCell, DisconnectCell, RegisterDevice, UnregisterDevice, Routing
}
