import asyncio
import traceback
import pytz
from datetime import datetime
from typing import Dict
from dataclasses import dataclass

from ijt_logger import ijt_log
from utils import log_result_to_file, log_result_event_details


@dataclass
class ShortResultEvent:
    EventType: str
    Result: dict
    Message: str
    EventId: str

    def to_dict(self) -> Dict:
        return {
            "EventType": self.EventType,
            "Result": self.Result,
            "Message": self.Message,
            "EventId": self.EventId,
        }


class ResultEventHandler:
    def __init__(self, server_url, client):
        self.server_url = server_url
        self.client = client
        try:
            self.loop = asyncio.get_running_loop()
        except RuntimeError:
            self.loop = asyncio.new_event_loop()
            asyncio.set_event_loop(self.loop)
        ijt_log.info("ResultEventHandler initialized.")

    async def process_event(self, event: ShortResultEvent):
        try:
            ijt_log.info(f"Processing Result Event: {event.Message}")
            await log_result_to_file(event)
        except Exception as e:
            ijt_log.error("Exception: " + str(e))
            ijt_log.error(traceback.format_exc())

    async def event_notification(self, event):
        try:
            client_received_time = datetime.now(pytz.utc)
            event_id = await log_result_event_details(
                event, self.client, self.server_url, client_received_time
            )
            filtered_event = ShortResultEvent(
                EventType=str(event.EventType),
                Result=event.Result,
                Message=getattr(event.Message, "Text", "N/A"),
                EventId=event_id,
            )
            asyncio.run_coroutine_threadsafe(
                self.process_event(filtered_event), self.loop
            )
        except Exception as e:
            ijt_log.error(f"Error handling event notification: {e}")
            ijt_log.error(traceback.format_exc())
