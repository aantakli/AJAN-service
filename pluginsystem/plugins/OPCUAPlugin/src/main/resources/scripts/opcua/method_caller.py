from asyncua import ua
from ijt_logger import ijt_log
import traceback
from utils import read_tool_identifier


class OPCUAMethodCaller:
    def __init__(self, client):
        self.client = client

    @staticmethod
    def _parse_outputs(ret):
        status = None
        message = None

        if isinstance(ret, (tuple, list)):
            if len(ret) >= 1:
                try:
                    status = int(ret[0])
                except Exception:
                    status = None
            if len(ret) >= 2:
                msg = ret[1]
                message = getattr(msg, "Text", str(msg))

        return status, message

    async def select_joint(
        self,
        object_nodeid: str,
        method_nodeid: str,
        joint_id: str,
        joint_origin_id: str = None,  # <-- Now optional
    ):
        try:
            # 1) Auto-load ProductInstanceUri (required)
            product_instance_uri = await read_tool_identifier(self.client)
            if not product_instance_uri:
                ijt_log.error("SelectJoint failed: ProductInstanceUri is NULL")
                return None

            product_instance_uri = str(product_instance_uri)

            # 2) Handle missing/empty JointOriginId
            if not joint_origin_id:
                joint_origin_id = ""  # <-- Send as empty OPC UA string

            obj = self.client.get_node(object_nodeid)
            mth = self.client.get_node(method_nodeid)

            # 3) Build arguments (3 input args required by OPC UA method)
            args = [
                ua.Variant(product_instance_uri, ua.VariantType.String),
                ua.Variant(joint_id, ua.VariantType.String),
                ua.Variant(joint_origin_id, ua.VariantType.String),  # <-- now may be ""
            ]

            ijt_log.info(
                f"Calling SelectJoint: PIUri={product_instance_uri}, "
                f"JointId={joint_id}, Origin={joint_origin_id!r}"
            )

            ret = await obj.call_method(mth, *args)

            # Parse outputs
            status, message = self._parse_outputs(ret)

            return {
                "status": status,
                "status_message": message,
                "raw": ret,
            }

        except Exception as e:
            ijt_log.error(f"SelectJoint failed: {e}")
            ijt_log.error(traceback.format_exc())
            return None

    async def enable_asset(self, object_nodeid, method_nodeid, enable: bool):
        try:
            pi = await read_tool_identifier(self.client)
            if not pi:
                ijt_log.error("ProductInstanceUri is NULL")
                return None
            pi = str(pi)

            obj = self.client.get_node(object_nodeid)
            mth = self.client.get_node(method_nodeid)

            args = [
                ua.Variant(pi, ua.VariantType.String),
                ua.Variant(bool(enable), ua.VariantType.Boolean),
            ]

            ijt_log.info(f"Calling EnableAsset: PIUri={pi}, enable={enable}")
            ret = await obj.call_method(mth, *args)

            status, msg = self._parse_outputs(ret)
            return {"status": status, "status_message": msg, "raw": ret}

        except Exception as e:
            ijt_log.error(f"EnableAsset failed: {e}")
            ijt_log.error(traceback.format_exc())
            return None

    async def start_selected_joining(
        self, object_nodeid, method_nodeid, deselect_after_joining: bool
    ):
        try:
            pi = await read_tool_identifier(self.client)
            if not pi:
                ijt_log.error("ProductInstanceUri is NULL")
                return None
            pi = str(pi)

            obj = self.client.get_node(object_nodeid)
            mth = self.client.get_node(method_nodeid)

            args = [
                ua.Variant(pi, ua.VariantType.String),
                ua.Variant(bool(deselect_after_joining), ua.VariantType.Boolean),
            ]

            ijt_log.info(
                f"Calling StartSelectedJoining: PIUri={pi}, deselect={deselect_after_joining}"
            )
            ret = await obj.call_method(mth, *args)

            status, msg = self._parse_outputs(ret)
            return {"status": status, "status_message": msg, "raw": ret}

        except Exception as e:
            ijt_log.error(f"StartSelectedJoining failed: {e}")
            ijt_log.error(traceback.format_exc())
            return None
