from typing import Any
from datetime import datetime as std_datetime
import orjson
from ijt_logger import ijt_log


def is_instance_of_user_class(obj: Any) -> bool:
    return str(type(obj)).startswith("<class") and hasattr(obj, "__weakref__")


def serialize_value(value: Any) -> Any:
    if is_instance_of_user_class(value):
        return serialize_class_instance_as_dict(value)
    elif value is None:
        return None
    elif isinstance(value, list):
        return [serialize_value(item) for item in value]
    elif isinstance(value, dict):
        return {k: serialize_value(v) for k, v in value.items()}
    elif isinstance(value, std_datetime):
        return value.isoformat()
    elif isinstance(value, (str, int, float, bool)):
        return value
    else:
        return str(value)


def serialize_class_instance_as_dict(obj: Any) -> dict:
    result = {"pythonclass": type(obj).__name__}
    for key, value in obj.__dict__.items():
        if key != "_freeze":
            result[key] = serialize_value(value)
    return result


def serialize_full_event(value: Any) -> Any:
    return serialize_value(value)


def serialize_tuple(list_of_tuples: list[tuple[str, Any]]) -> str:
    try:
        data = {key: serialize_value(val) for key, val in list_of_tuples}
        return orjson.dumps(data).decode("utf-8")
    except Exception as e:
        ijt_log.error(f"Failed to serialize tuple: {e}")
        return "{}"
