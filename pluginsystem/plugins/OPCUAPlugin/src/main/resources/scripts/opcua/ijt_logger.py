import logging
from datetime import datetime
from logging.handlers import TimedRotatingFileHandler
from pathlib import Path


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

# File handler with daily rotation
log_dir = Path("logs")
log_dir.mkdir(exist_ok=True)
file_handler = TimedRotatingFileHandler(
    log_dir / "client.log", when="midnight", backupCount=7, encoding="utf-8"
)
file_handler.setFormatter(formatter)

# Logger setup
ijt_log = logging.getLogger("ijt_logger")
ijt_log.setLevel(logging.INFO)
ijt_log.addHandler(console_handler)
ijt_log.addHandler(file_handler)
ijt_log.propagate = False

# Reduce verbosity of external libraries
logging.getLogger("asyncua").setLevel(logging.ERROR)
