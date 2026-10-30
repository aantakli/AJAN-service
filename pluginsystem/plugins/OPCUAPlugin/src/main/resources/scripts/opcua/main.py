import asyncio
import argparse
import re
import sys
import traceback
import os

from opcua_client import OPCUAClient
from client_config import SERVER_URL as DEFAULT_SERVER_URL
from ijt_logger import ijt_log

# KEEPING your original URL pattern as-is
URL_PATTERN = re.compile(r"opc\.tcp://[a-zA-Z0-9\.\-]+:\d+")


def validate_url(url: str) -> str:
    if url and URL_PATTERN.match(url):
        return url
    env_url = os.getenv("OPCUA_SERVER_URL")
    if env_url and URL_PATTERN.match(env_url):
        ijt_log.info(f"Using OPC UA server URL from environment: {env_url}")
        return env_url
    ijt_log.warning("Invalid or missing OPC UA URL. Falling back to default.")
    return DEFAULT_SERVER_URL


# main() returns EXIT_OK on success; on a failure it raises SystemExit("<reason>") (exit code 1),
# so AJAN's OPCUA nodes report the reason, not only an exit code.
EXIT_OK = 0


def _usage_error(message: str) -> str:
    ijt_log.error(message)
    return f"usage error: {message}"


# NEW: method-call runner for handling method calls
async def run_method_call(server_url: str, args):
    """
    Connects, calls a single method (if --call is supplied) and logs the result. Returns None if
    the method returned a result with status 0 (or no status), otherwise the failure reason.
    """
    client = OPCUAClient(server_url)
    await client.connect()

    try:
        methods = client.methods  # OPCUAMethodCaller bound to the same UA client

        if args.call == "select_joint":
            if not args.joint_id:
                return _usage_error("--joint-id is required for select_joint")
            origin = args.origin_id if args.origin_id else ""
            result = await methods.select_joint(
                object_nodeid="ns=1;s=TighteningSystem/JointManagement",
                method_nodeid="ns=1;s=TighteningSystem/JointManagement/SelectJoint",
                joint_id=args.joint_id,
                joint_origin_id=origin,
            )

        elif args.call == "enable_asset":
            if args.enable not in ("true", "false"):
                return _usage_error("--enable must be true|false for enable_asset")
            result = await methods.enable_asset(
                object_nodeid="ns=1;s=TighteningSystem/AssetManagement/MethodSet",
                method_nodeid="ns=1;s=TighteningSystem/AssetManagement/MethodSet/EnableAsset",
                enable=(args.enable == "true"),
            )

        elif args.call == "start_selected_joining":
            if args.deselect not in ("true", "false"):
                return _usage_error(
                    "--deselect must be true|false for start_selected_joining"
                )
            result = await methods.start_selected_joining(
                object_nodeid="ns=1;s=TighteningSystem/JoiningProcessManagement",
                method_nodeid="ns=1;s=TighteningSystem/JoiningProcessManagement/StartSelectedJoining",
                deselect_after_joining=(args.deselect == "true"),
            )

        else:
            return _usage_error(f"Unknown method: {args.call}")

        ijt_log.info(f"[METHOD RESULT] {result}")
        if result is None:
            ijt_log.error(f"Method {args.call} failed (no result).")
            return f"method {args.call} failed (no result)"
        status = result.get("status")
        if status not in (None, 0):
            reason = f"method {args.call} returned status {status}: {result.get('status_message')}"
            ijt_log.error(reason)
            return reason
        return None

    finally:
        await client.cleanup()
        ijt_log.info("Client shutdown complete.")
        ijt_log.info(
            "Note: Any late server responses after disconnect can be safely ignored."
        )


# Seconds between connection checks of the event client
CHECK_INTERVAL = 1.0


# This is normal event mode runner
async def run_client(server_url: str):
    """Runs until cancelled; raises (main() then exits with the reason) when the connection is
    lost, so AJAN's OPCUASetupNode can start a new client."""
    client = OPCUAClient(server_url)
    await client.clear_old_logs()
    await client.connect()
    await client.subscribe_to_events()
    try:
        while True:
            await asyncio.sleep(CHECK_INTERVAL)
            await client.check_connection()
    except asyncio.CancelledError:
        ijt_log.info("Run loop cancelled.")
    finally:
        await client.cleanup()
    ijt_log.info("Client shutdown complete.")
    ijt_log.info(
        "Note: Any late server responses after disconnect can be safely ignored."
    )


def main(argv=None) -> int:
    """Runs one method call (--call) or the event client; returns EXIT_OK.

    Raises SystemExit("<reason>") on a failure (connection or method error, usage error).
    argv defaults to sys.argv[1:]; AJAN passes it explicitly (see call.py and events.py). The
    event loop runs on the calling thread and is unset again afterwards: AJAN reuses its threads,
    and later Python code there must not find a closed loop.
    """
    parser = argparse.ArgumentParser(description="Run OPC UA Event Client")

    # Normal Scenario without methods
    parser.add_argument("--url", type=str, help="OPC UA server URL")

    # New option for METHOD mode
    parser.add_argument(
        "--call",
        type=str,
        help="Method to call (select_joint | enable_asset | start_selected_joining)",
    )
    parser.add_argument("--joint-id", type=str, help="SelectJoint: JointId")
    parser.add_argument(
        "--origin-id", type=str, help="SelectJoint: JointOriginId (optional)"
    )
    parser.add_argument(
        "--enable", type=str, choices=["true", "false"], help="EnableAsset: true|false"
    )
    parser.add_argument(
        "--deselect",
        type=str,
        choices=["true", "false"],
        help="StartSelectedJoining: true|false",
    )

    # OPTIONAL: allow disabling events in event mode (kept consistent)
    parser.add_argument(
        "--no-events", action="store_true", help="(ignored in method mode)"
    )

    args = parser.parse_args(argv)
    server_url = validate_url(args.url)
    ijt_log.info(f"Using OPC UA server URL: {server_url}")

    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)

    # Decide which runner to execute WITHOUT touching your original run_client logic
    if args.call:
        task = loop.create_task(run_method_call(server_url, args))
    else:
        task = loop.create_task(run_client(server_url))

    mode = f"OPC UA method {args.call}" if args.call else "OPC UA event client"
    failure = None
    try:
        outcome = loop.run_until_complete(task)
        failure = outcome if isinstance(outcome, str) else None
    except KeyboardInterrupt:
        ijt_log.info("KeyboardInterrupt received. Cancelling tasks...")
        ijt_log.info(
            "Shutdown initiated. Late OPC UA responses may appear in logs but are safe to ignore."
        )
        task.cancel()
        try:
            loop.run_until_complete(task)
        except asyncio.CancelledError:
            ijt_log.info("Client task cancelled.")
        except Exception:
            pass
    except Exception as e:
        # connection or method errors: report them through SystemExit below
        ijt_log.error(f"Unhandled exception: {e}")
        ijt_log.error(traceback.format_exc())
        failure = f"{type(e).__name__}: {e}"
    finally:
        loop.close()
        asyncio.set_event_loop(None)
        ijt_log.info("Event loop closed.")
    if failure:
        raise SystemExit(f"{mode} failed against {server_url}: {failure}")
    return EXIT_OK


if __name__ == "__main__":
    sys.exit(main())
