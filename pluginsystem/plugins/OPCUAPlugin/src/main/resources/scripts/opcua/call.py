"""AJAN entry point for one OPC UA method call (EnableAsset, SelectJoint, StartJoining nodes).

Runs main.py with `call_argv` (`--url <u> --call <c> ...`, an input variable set by AJAN) passed to
main() directly instead of through the process-global sys.argv, so calls of several agents run
concurrently (executeScriptFile serializes calls that set sys.argv). Exits with main()'s code.
"""
import sys

import main

sys.exit(main.main([str(a) for a in call_argv]))  # noqa: F821 (call_argv is set by AJAN)
