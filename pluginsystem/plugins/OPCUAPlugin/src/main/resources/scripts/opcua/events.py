"""AJAN entry point for the OPC UA event client (OPCUASetupNode).

Runs main.py's event mode (`--url <url>`) with the arguments passed to main() directly instead of
through the process-global sys.argv: the event client runs until the process ends, and executeScriptFile
serializes calls that set sys.argv for their whole run. `url` is an input variable set by AJAN.
"""
import sys

import main

sys.exit(main.main(["--url", url]))  # noqa: F821 (url is set by AJAN)
