# IJT Console Client

## Contact
- **Author:** Mohit Agarwal: mohit.agarwal@atlascopco.com

## Overview
- This application uses the open source OPC UA Python opcua-asyncio stack. 
- This is a minimal client application based on the OPC UA Industrial Joining Technologies (IJT) Companion Specifications.

## Prerequisites
-  **Internet Connection**
-  **Download** the project directory: **`IJT_Console_Client`** and launch a terminial from the project directory.
-  **Install** **Python** from the **official** **website** and **add** the installation directory to the system **PATH**.

## Run the client application
- **Note:** On Linux, the command should be **python3** instead of **python**.
### Option 1
- **Run** the following command **`python setup_client.py --url="opc.tcp://<ip>:<port>"`**.
### Option 2
- **Update** the **SERVER_URL** in **`client_config.py`** to the OPC UA Server EndpointUrl.
- **Run** the following command **`python setup_client.py`**.

