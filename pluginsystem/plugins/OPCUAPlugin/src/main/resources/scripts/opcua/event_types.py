from ijt_logger import ijt_log

RESULT_NS_URI = "http://opcfoundation.org/UA/Machinery/Result/"
JOINING_NS_URI = "http://opcfoundation.org/UA/IJT/Base/"


async def get_event_types(client, root):
    try:
        ns_result = await client.get_namespace_index(RESULT_NS_URI)
        ns_joining = await client.get_namespace_index(JOINING_NS_URI)

        result_event_node = await root.get_child(
            [
                "0:Types",
                "0:EventTypes",
                "0:BaseEventType",
                f"{ns_result}:ResultReadyEventType",
            ]
        )

        joining_result_event_node = await root.get_child(
            [
                "0:Types",
                "0:EventTypes",
                "0:BaseEventType",
                f"{ns_result}:ResultReadyEventType",
                f"{ns_joining}:JoiningSystemResultReadyEventType",
            ]
        )

        joining_system_event_node = await root.get_child(
            [
                "0:Types",
                "0:EventTypes",
                "0:BaseEventType",
                f"{ns_joining}:JoiningSystemEventType",
            ]
        )

        ijt_log.info("Event type nodes resolved successfully.")
        return result_event_node, joining_result_event_node, joining_system_event_node

    except Exception as e:
        ijt_log.error(f"Failed to resolve event types: {e}")
        raise
