package simulator.actors.events;

import simulator.actors.interfaces.Events;

public enum NetworkEvents implements Events {
    SendSMS, MakeVoiceCall, RequestDataSession,
    ConnectCell, DisconnectCell, RegisterDevice, UnregisterDevice, Routing
}
