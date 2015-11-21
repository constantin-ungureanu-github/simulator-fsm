package simulator.actors.events;

import simulator.actors.interfaces.TemplateEvents;

public enum NetworkEvents implements TemplateEvents {
    SendSMS, MakeVoiceCall, RequestDataSession,
    ConnectCell, DisconnectCell, RegisterDevice, UnregisterDevice, Routing
}
