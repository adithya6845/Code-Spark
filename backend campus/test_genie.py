import os
import asyncio
import httpx
import json
from dotenv import load_dotenv

load_dotenv()

host = os.getenv("DATABRICKS_HOST", "").rstrip("/")
token = os.getenv("DATABRICKS_TOKEN", "")
space = os.getenv("GENIE_SPACE_ID", "")

headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json",
}

async def query_genie(prompt: str):
    print(f"\n--- Testing Query: '{prompt}' ---")
    async with httpx.AsyncClient(timeout=60.0) as client:
        start_url = f"{host}/api/2.0/genie/spaces/{space}/start-conversation"
        r = await client.post(start_url, json={"content": prompt}, headers=headers)
        data = r.json()
        conv_id = data.get("conversation_id")
        msg_id = data.get("id") or data.get("message_id")
        
        if not conv_id or not msg_id:
            print("Failed to start:", data)
            return
            
        for i in range(15):
            await asyncio.sleep(2)
            poll_url = f"{host}/api/2.0/genie/spaces/{space}/conversations/{conv_id}/messages/{msg_id}"
            poll_res = await client.get(poll_url, headers=headers)
            res_json = poll_res.json()
            status = res_json.get("status")
            if status in ["COMPLETED", "EXECUTED", "SUCCESS"]:
                print("STATUS:", status)
                attachments = res_json.get("attachments", [])
                for att in attachments:
                    if "text" in att:
                        print("GENIE ANSWER:", att["text"].get("content"))
                    if "query" in att:
                        print("GENERATED SQL:", att["query"].get("query"))
                break
            elif status in ["FAILED", "ERROR"]:
                print("FAILED:", res_json)
                break

if __name__ == "__main__":
    asyncio.run(query_genie("What if I collaborate with Aarav + Sophia to build an AI powered campus traffic system?"))
