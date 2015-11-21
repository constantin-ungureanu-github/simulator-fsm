package simulator.actors.events;

import simulator.actors.interfaces.TemplateEvents;

public enum DeviceEvents implements TemplateEvents {
    PickedBySubscriber,
    AddDevice, RemoveDevice,
    PowerOn, PowerOff,
    ConnectToCell, AckConnectToCell, NAckConnectToCell, DisconnectFromCell, AckDisconnectFromCell,
    SendSMS, ReceiveSMS, AckSendSMS, NAckSendSMS,
    MakeVoiceCall, ReceiveVoiceCall, AckMakeVoiceCall, NAckMakeVoiceCall,
    RequestDataSession, AckRequestDataSession, NAckRequestDataSession
}
