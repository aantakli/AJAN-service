import re

# Regex pattern for validating OPC UA URLs
URL_PATTERN = re.compile(r"opc\.tcp://[a-zA-Z0-9\.\-]+:\d+")

# Default OPC UA server URL (used if --url is not provided)
SERVER_URL = "opc.tcp://localhost:40451"

# Enable result file logging (project-level constant)
ENABLE_RESULT_FILE_LOGGING = True
