"""Example of announcing a service (in this case, a fake HTTP server)"""

import asyncio
import logging
import os
import socket
import sys

from aiohttp import web
from aiozeroconf import ServiceInfo, Zeroconf
from netifaces import interfaces, ifaddresses, AF_INET

SERVICE_TYPE = "_openbot-server._tcp.local."

loop = asyncio.get_event_loop()
zc = Zeroconf(loop)


async def register(app: web.Application):
    await run_test(zc)
    app.on_shutdown.append(on_shutdown)


async def run_test(zc):
    desc = {}
    local_ip = ip4_address()
    name = (
        os.getenv("OPENBOT_NAME", socket.gethostname())
        .replace(".local", "")
        .replace(".", "-")
    )

    info = ServiceInfo(
        SERVICE_TYPE,
        f"{name}.{SERVICE_TYPE}",
        address=socket.inet_aton(local_ip),
        port=8000,
        weight=0,
        priority=0,
        properties=desc,
    )
    print("Registration of the service with name:", name)
    await zc.register_service(info)


def ip4_address():
    for interface in interfaces():
        addresses = ifaddresses(interface)
        if AF_INET not in addresses:
            continue
        for link in addresses[AF_INET]:
            if "addr" not in link:
                continue
            ip4 = link["addr"]
            if ip4.startswith("127.") or ip4.startswith("10."):
                print(f"Skip address {ip4} @ interface {interface}")
                continue
            print(f"Found address {ip4} @ interface {interface}")
            return ip4


async def do_close(zc):
    global info
    await zc.unregister_service(info)
    await zc.close()


async def on_shutdown(app):
    print("Unregistering...")
    await do_close(zc)


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    if len(sys.argv) > 1:
        assert sys.argv[1:] == ["--debug"]
        logging.getLogger("aiozeroconf").setLevel(logging.DEBUG)

    try:
        xx = loop.create_task(run_test(zc))
        loop.run_forever()
    except KeyboardInterrupt:
        print("Unregistering...")
        loop.run_until_complete(do_close(zc))
    finally:
        loop.close()
