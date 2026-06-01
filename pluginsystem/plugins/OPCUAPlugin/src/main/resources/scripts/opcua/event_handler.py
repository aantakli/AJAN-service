import asyncio
import traceback
import orjson
from threading import Thread
from dataclasses import dataclass
from typing import List, Any

from asyncua import ua
from ijt_logger import ijt_log
from utils import log_joining_system_event, nodeid_to_str, localizedtext_to_str
from serialize_data import serialize_full_event


@dataclass
class ShortJoiningEvent:
    EventType: ua.NodeId
    EventId: str
    Message: ua.LocalizedText
    SourceName: str
    SourceNode: str
    Severity: int
    Time: Any
    ReceiveTime: Any
    LocalTime: ua.TimeZoneDataType
    ConditionClassId: ua.NodeId
    ConditionClassName: ua.LocalizedText
    ConditionSubClassId: List[ua.NodeId]
    ConditionSubClassName: List[ua.LocalizedText]
    EventCode: Any
    EventText: str
    JoiningTechnology: str
    AssociatedEntities: List[Any]
    ReportedValues: List[Any]


class EventHandler:
    def __init__(self, websocket, server_url, client):
        self.websocket = websocket
        self.server_url = server_url
        self.client = client
        self.queue = asyncio.Queue()
        self.loop = asyncio.new_event_loop()

        def start_loop():
            asyncio.set_event_loop(self.loop)
            self.loop.run_forever()

        thread = Thread(target=start_loop, daemon=True)
        thread.start()

        asyncio.run_coroutine_threadsafe(self.handle_queue(), self.loop)

    async def event_notification(self, event):
        try:
            ijt_log.info("EventHandler: Event notification")

            short_event = ShortJoiningEvent(
                EventType=event.EventType,
                EventId=(
                    event.EventId.decode("utf-8", errors="replace")
                    if isinstance(event.EventId, bytes)
                    else str(event.EventId)
                ),
                Message=getattr(event, "Message", None),
                SourceName=str(getattr(event, "SourceName", "")),
                SourceNode=nodeid_to_str(getattr(event, "SourceNode", None)),
                Severity=int(getattr(event, "Severity", 0)),
                Time=getattr(event, "Time", None),
                ReceiveTime=getattr(event, "ReceiveTime", None),
                LocalTime=getattr(event, "LocalTime", None),
                ConditionClassId=getattr(event, "ConditionClassId", None),
                ConditionClassName=getattr(event, "ConditionClassName", None),
                ConditionSubClassId=getattr(event, "ConditionSubClassId", []),
                ConditionSubClassName=getattr(event, "ConditionSubClassName", []),
                EventCode=getattr(event, "JoiningSystemEventContent/EventCode", None),
                EventText=localizedtext_to_str(
                    getattr(event, "JoiningSystemEventContent/EventText", None)
                ),
                JoiningTechnology=localizedtext_to_str(
                    getattr(event, "JoiningSystemEventContent/JoiningTechnology", None)
                ),
                AssociatedEntities=getattr(
                    event, "JoiningSystemEventContent/AssociatedEntities", []
                ),
                ReportedValues=getattr(
                    event, "JoiningSystemEventContent/ReportedValues", []
                ),
            )

            await log_joining_system_event(short_event)
            await self.queue.put(short_event)

        except Exception as e:
            ijt_log.error(f"Error handling event notification: {e}")
            ijt_log.error(traceback.format_exc())

    async def handle_queue(self):
        while True:
            item = await self.queue.get()
            if item is None:
                break
            try:
                serialized_event = serialize_full_event(item)
                json_payload = orjson.dumps(serialized_event).decode("utf-8")

                if self.websocket:
                    await self.websocket.send(json_payload)
                else:
                    ijt_log.debug(
                        "WebSocket is None, skipping send. Event processed locally."
                    )

            except Exception as e:
                ijt_log.error(f"Error sending message: {e}")
                ijt_log.error(traceback.format_exc())
                if self.websocket:
                    try:
                        await self.websocket.close()
                    except Exception:
                        pass
                break
            finally:
                self.queue.task_done()

    async def shutdown(self):
        await self.queue.put(None)

    async def close(self):
        await self.shutdown()
        ijt_log.info("EventHandler closed.")
