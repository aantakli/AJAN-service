import asyncio
import argparse
import re
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


# NEW: method-call runner for handling method calls
async def run_method_call(server_url: str, args):
    """
    Connects, calls a single method (if --call is supplied), logs result, and exits.
    """
    client = OPCUAClient(server_url)
    await client.connect()

    try:
        methods = client.methods  # OPCUAMethodCaller bound to the same UA client

        if args.call == "select_joint":
            if not args.joint_id:
                ijt_log.error("--joint-id is required for select_joint")
                return
            origin = args.origin_id if args.origin_id else ""
            result = await methods.select_joint(
                object_nodeid="ns=1;s=TighteningSystem/JointManagement",
                method_nodeid="ns=1;s=TighteningSystem/JointManagement/SelectJoint",
                joint_id=args.joint_id,
                joint_origin_id=origin,
            )

        elif args.call == "enable_asset":
            if args.enable not in ("true", "false"):
                ijt_log.error("--enable must be true|false for enable_asset")
                return
            result = await methods.enable_asset(
                object_nodeid="ns=1;s=TighteningSystem/AssetManagement/MethodSet",
                method_nodeid="ns=1;s=TighteningSystem/AssetManagement/MethodSet/EnableAsset",
                enable=(args.enable == "true"),
            )

        elif args.call == "start_selected_joining":
            if args.deselect not in ("true", "false"):
                ijt_log.error(
                    "--deselect must be true|false for start_selected_joining"
                )
                return
            result = await methods.start_selected_joining(
                object_nodeid="ns=1;s=TighteningSystem/JoiningProcessManagement",
                method_nodeid="ns=1;s=TighteningSystem/JoiningProcessManagement/StartSelectedJoining",
                deselect_after_joining=(args.deselect == "true"),
            )

        else:
            ijt_log.error(f"Unknown method: {args.call}")
            return

        ijt_log.info(f"[METHOD RESULT] {result}")

    finally:
        await client.cleanup()
        ijt_log.info("Client shutdown complete.")
        ijt_log.info(
            "Note: Any late server responses after disconnect can be safely ignored."
        )


# This is normal event mode runner
async def run_client(server_url: str):
    client = OPCUAClient(server_url)
    await client.connect()
    await client.subscribe_to_events()
    try:
        while True:
            await asyncio.sleep(1)
    except asyncio.CancelledError:
        ijt_log.info("Run loop cancelled.")
    finally:
        await client.cleanup()
    ijt_log.info("Client shutdown complete.")
    ijt_log.info(
        "Note: Any late server responses after disconnect can be safely ignored."
    )


def main():
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

    args = parser.parse_args()
    server_url = validate_url(args.url)
    ijt_log.info(f"Using OPC UA server URL: {server_url}")

    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)

    # Decide which runner to execute WITHOUT touching your original run_client logic
    if args.call:
        task = loop.create_task(run_method_call(server_url, args))
    else:
        task = loop.create_task(run_client(server_url))

    try:
        loop.run_until_complete(task)
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
    except Exception as e:
        ijt_log.error(f"Unhandled exception: {e}")
        ijt_log.error(traceback.format_exc())
    finally:
        loop.close()
        ijt_log.info("Event loop closed.")


if __name__ == "__main__":
    main()
