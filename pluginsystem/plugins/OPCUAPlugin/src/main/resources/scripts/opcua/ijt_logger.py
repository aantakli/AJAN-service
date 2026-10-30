import logging
import os
import sys
from datetime import datetime
from logging.handlers import TimedRotatingFileHandler
from pathlib import Path

# Base directory of logs/ and result_logs/: $AJAN_OPCUA_LOG_DIR, by default <user cache>/ajan-opcua
# (not the working directory of the AJAN service, which may not be writable).
LOG_DIR_ENV = "AJAN_OPCUA_LOG_DIR"


def _user_cache_dir() -> Path:
    if sys.platform == "win32":
        local = os.getenv("LOCALAPPDATA")
        if local:
            return Path(local)
    xdg = os.getenv("XDG_CACHE_HOME")
    if xdg:
        return Path(xdg)
    return Path.home() / ".cache"


def base_dir() -> Path:
    configured = os.getenv(LOG_DIR_ENV)
    return Path(configured) if configured else _user_cache_dir() / "ajan-opcua"


def log_subdir(name: str) -> Path:
    """<base_dir>/<name>, created with its parents."""
    path = base_dir() / name
    path.mkdir(parents=True, exist_ok=True)
    return path


class MillisecondFormatter(logging.Formatter):
    def formatTime(self, record, datefmt=None):
        ct = datetime.fromtimestamp(record.created)
        if datefmt:
            s = ct.strftime(datefmt)
            return s[:-3]  # Trim microseconds to milliseconds
        else:
            return ct.strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]


LOG_FORMAT = "[%(asctime)s] [%(levelname)s] %(filename)s:%(funcName)s - %(message)s"
DATE_FORMAT = "%Y-%m-%d %H:%M:%S.%f"

formatter = MillisecondFormatter(LOG_FORMAT, datefmt=DATE_FORMAT)

# Console handler
console_handler = logging.StreamHandler()
console_handler.setFormatter(formatter)

# Logger setup
ijt_log = logging.getLogger("ijt_logger")
ijt_log.setLevel(logging.INFO)
ijt_log.propagate = False
if not ijt_log.handlers:  # imported again (e.g. by another script directory): no duplicates
    ijt_log.addHandler(console_handler)
    # File handler with daily rotation; an unwritable log directory only costs the log file
    try:
        file_handler = TimedRotatingFileHandler(
            log_subdir("logs") / "client.log", when="midnight", backupCount=7, encoding="utf-8"
        )
        file_handler.setFormatter(formatter)
        ijt_log.addHandler(file_handler)
    except OSError as e:
        ijt_log.warning(f"No log file ({e}); set {LOG_DIR_ENV} to a writable directory")

# Reduce verbosity of external libraries
logging.getLogger("asyncua").setLevel(logging.ERROR)
