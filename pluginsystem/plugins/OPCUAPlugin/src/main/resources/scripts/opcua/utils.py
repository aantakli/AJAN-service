import pytz
import traceback
import aiofiles
import re
from datetime import datetime
from typing import Optional, Dict, List, Any
from asyncua import Client, ua
from asyncua.ua import String
from pathlib import Path

# ---- START: robust JSON import (fallback if orjson is missing) ----
try:
    import orjson  # fast path

    def _to_json_bytes(obj) -> bytes:
        return orjson.dumps(obj)

    def _to_json_str(obj) -> str:
        return orjson.dumps(obj).decode("utf-8")

except Exception:
    import json  # fallback

    def _to_json_bytes(obj) -> bytes:
        return json.dumps(obj, ensure_ascii=False, separators=(",", ":")).encode(
            "utf-8"
        )

    def _to_json_str(obj) -> str:
        return json.dumps(obj, ensure_ascii=False, separators=(",", ":"))


# ---- END: robust JSON import ----

from ijt_logger import ijt_log
from serialize_data import serialize_full_event
from client_config import ENABLE_RESULT_FILE_LOGGING


def log_field(label: str, value: str, label_width: int = 35):
    ijt_log.info(f"{label:<{label_width}} : {value}")


def log_separator(label_width: int = 35):
    ijt_log.info("-" * (label_width + 40))


def format_local_time(dt: datetime, timezone: str = "Europe/Stockholm") -> str:
    if isinstance(dt, str):
        try:
            dt = datetime.fromisoformat(dt)
        except Exception:
            return dt
    local_tz = pytz.timezone(timezone)
    return dt.astimezone(local_tz).strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]


async def read_server_time(client: Client) -> Optional[datetime]:
    try:
        node = client.get_node(ua.NodeId(2258, 0))  # ServerStatus.CurrentTime
        return await node.read_value()
    except Exception as e:
        ijt_log.warning(f"{'Server Time Read Failed':<40} : {e}")
        return None


async def read_tool_identifier(client: Client) -> Optional[String]:
    try:
        node = client.get_node(
            ua.NodeId(
                "TighteningSystem/AssetManagement/Assets/Tools/TighteningTool/Identification/ProductInstanceUri",
                1,
            )
        )  # ns=1;s=TighteningSystem/AssetManagement/Assets/Tools/TighteningTool/Identification/ProductInstanceUri
        return await node.read_value()
    except Exception as e:
        ijt_log.warning(f"{'Tool Identifier Read Failed':<40} : {e}")
        return None


async def log_result_event_details(
    event, client: Client, server_url: str, client_received_time: datetime
) -> str:
    try:
        server_time = await read_server_time(client)
        event_time = event.Time
        event_id = event.EventId.decode("utf-8", errors="replace")

        meta = getattr(event.Result, "ResultMetaData", None)
        times = getattr(meta, "ProcessingTimes", None) if meta else None
        start_time = getattr(times, "StartTime", None)
        end_time = getattr(times, "EndTime", None)
        creation_time = getattr(meta, "CreationTime", None) if meta else None

        if end_time and end_time.tzinfo is None:
            end_time = pytz.utc.localize(end_time)

        latency_ms = (
            (client_received_time - end_time).total_seconds() * 1000
            if end_time
            else None
        )

        label_width = 35

        log_separator(label_width)
        log_field(
            "RESULT EVENT RECEIVED",
            getattr(event.Message, "Text", "Unavailable"),
            label_width,
        )
        log_field(
            "1. StartTime of Tightening",
            format_local_time(start_time) if start_time else "Unavailable",
            label_width,
        )
        log_field(
            "2. EndTime of Tightening",
            format_local_time(end_time) if end_time else "Unavailable",
            label_width,
        )
        log_field(
            "3. Result Creation Time",
            format_local_time(creation_time) if creation_time else "Unavailable",
            label_width,
        )
        log_field(
            "4. Result Event Generated Time",
            format_local_time(event_time) if event_time else "Unavailable",
            label_width,
        )
        log_field(
            "5. Client Time", format_local_time(client_received_time), label_width
        )
        log_field(
            "6. Server Time",
            format_local_time(server_time) if server_time else "Unavailable",
            label_width,
        )

        if latency_ms is not None:
            log_field(
                "*** Turn around Time (EndTime → Client)",
                f"{abs(latency_ms):.3f} ms",
                label_width,
            )
        else:
            log_field(
                "*** Turn around Time (EndTime → Client)", "Unavailable", label_width
            )

        log_separator(label_width)
        return event_id
    except Exception as e:
        ijt_log.error(f"Error logging result event details: {e}")
        ijt_log.error(traceback.format_exc())
        return "unknown"


async def log_joining_system_event(event):
    label_width = 35
    log_separator(label_width)
    log_field(
        "JOINING SYSTEM EVENT",
        getattr(event.Message, "Text", "Unavailable"),
        label_width,
    )

    log_field("EventType", nodeid_to_str(event.EventType), label_width)
    log_field("EventId", event.EventId, label_width)
    log_field("Message", event.Message, label_width)
    log_field("SourceName", event.SourceName, label_width)
    log_field("SourceNode", event.SourceNode, label_width)
    log_field("Severity", event.Severity, label_width)
    log_field(
        "Time",
        format_local_time(event.Time) if event.Time else "Unavailable",
        label_width,
    )
    log_field(
        "ReceiveTime",
        format_local_time(event.ReceiveTime) if event.ReceiveTime else "Unavailable",
        label_width,
    )

    if event.LocalTime:
        log_field(
            "LocalTime.Offset",
            getattr(event.LocalTime, "Offset", "Unavailable"),
            label_width,
        )
        log_field(
            "LocalTime.DaylightSavingInOffset",
            getattr(event.LocalTime, "DaylightSavingInOffset", "Unavailable"),
            label_width,
        )
    else:
        log_field("LocalTime", "Unavailable", label_width)

    log_field("ConditionClassId", nodeid_to_str(event.ConditionClassId), label_width)
    log_field(
        "ConditionClassName",
        localizedtext_to_str(event.ConditionClassName),
        label_width,
    )

    subclass_ids = [nodeid_to_str(nid) for nid in event.ConditionSubClassId]
    log_field("ConditionSubClassId", "", label_width)
    for item in subclass_ids:
        log_field("", item, label_width)

    subclass_names = [localizedtext_to_str(lt) for lt in event.ConditionSubClassName]
    log_field("ConditionSubClassName", "", label_width)
    for item in subclass_names:
        log_field("", item, label_width)

    log_field("EventCode", event.EventCode, label_width)
    log_field("EventText", event.EventText, label_width)
    log_field("JoiningTechnology", event.JoiningTechnology, label_width)

    if isinstance(event.AssociatedEntities, list) and event.AssociatedEntities:
        log_field("AssociatedEntities", "", label_width)
        for entity in event.AssociatedEntities:
            try:
                log_entity(entity)
            except Exception as e:
                log_field("Error logging entity", str(e), label_width)
    else:
        log_field("AssociatedEntities", str(event.AssociatedEntities), label_width)

    if isinstance(event.ReportedValues, list) and event.ReportedValues:
        log_field("ReportedValues", "", label_width)
        for rv in event.ReportedValues:
            try:
                log_reported_value(rv)
            except Exception as e:
                log_field("Error logging reported value", str(e), label_width)
    else:
        log_field("ReportedValues", str(event.ReportedValues), label_width)

    log_separator(label_width)


def log_entity(entity: Any):
    label_width = 35
    for field in ["Name", "Description", "EntityId", "EntityType", "IsExternal"]:
        log_field(field, getattr(entity, field, ""), label_width)


def log_reported_value(rv: Any):
    label_width = 35
    eu = getattr(rv, "EngineeringUnits", None)
    eu_display = getattr(eu, "DisplayName", "")
    eu_desc = getattr(eu, "Description", "")

    log_field("Name", getattr(rv, "Name", ""), label_width)
    log_field(
        "Current", getattr(getattr(rv, "CurrentValue", None), "Value", ""), label_width
    )
    log_field(
        "Previous",
        getattr(getattr(rv, "PreviousValue", None), "Value", ""),
        label_width,
    )
    log_field("PhysicalQuantity", getattr(rv, "PhysicalQuantity", ""), label_width)
    log_field("LowLimit", getattr(rv, "LowLimit", ""), label_width)
    log_field("HighLimit", getattr(rv, "HighLimit", ""), label_width)
    log_field("Units", eu_display, label_width)
    log_field("Description", eu_desc, label_width)


def nodeid_to_str(nodeid: ua.NodeId) -> str:
    try:
        if isinstance(nodeid, ua.NodeId):
            ns = nodeid.NamespaceIndex
            identifier = nodeid.Identifier
            if nodeid.NodeIdType == ua.NodeIdType.Numeric:
                return f"ns={ns};i={identifier}"
            elif nodeid.NodeIdType == ua.NodeIdType.String:
                return f"ns={ns};s={identifier}"
            elif nodeid.NodeIdType == ua.NodeIdType.Guid:
                return f"ns={ns};g={identifier}"
            elif nodeid.NodeIdType == ua.NodeIdType.Opaque:
                return f"ns={ns};b={identifier}"
    except Exception:
        pass
    return str(nodeid)


def localizedtext_to_str(lt: ua.LocalizedText) -> str:
    try:
        if isinstance(lt, ua.LocalizedText):
            return lt.Text
    except Exception:
        pass
    return str(lt)


def format_list_for_logging(
    label: str, items: List[str], label_width: int = 35
) -> List[str]:
    lines = [f"{label:<{label_width}} :"]
    for item in items:
        lines.append(f"{'':<{label_width}} {item}")
    return lines


async def log_result_to_file(event):
    if ENABLE_RESULT_FILE_LOGGING:
        try:
            json_data = serialize_full_event(event.Result)
            json_str = orjson.dumps(json_data).decode("utf-8")

            log_dir = Path("result_logs")
            log_dir.mkdir(exist_ok=True)

            safe_message = re.sub(
                r"[^\w\-_\. ]", "_", str(event.Message).replace(":", "_")
            )
            timestamp = datetime.utcnow().strftime("%Y%m%d_%H%M%S_%f")
            event_id = getattr(event, "EventId", "unknown")

            filename = f"{safe_message}_{event_id}_{timestamp}.json"
            temp_file = log_dir / f"{filename}.tmp"
            final_file = log_dir / filename

            async with aiofiles.open(temp_file, mode="w", encoding="utf-8") as f:
                await f.write(json_str)
            temp_file.rename(final_file)

            ijt_log.info(f"Event Result logged to {final_file}")
        except Exception as e:
            ijt_log.error(f"Failed to log result to file: {e}")
            ijt_log.error(traceback.format_exc())
