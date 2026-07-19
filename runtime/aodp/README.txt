Place the locally patched AODP client here as:

    runtime/aodp/albiondata-client-local.exe

IMPORTANT:
The upstream AODP client starts a GitHub updater. For strict local-only operation,
build your own local binary after removing the startUpdater() call from
albiondata-client.go. Do not rename an unmodified upstream binary to "local".

AlbionServant starts the client with:

    -i nats://albionservant:local-only-change-me@127.0.0.1:4222
    -p noop
    -minimize

Optional capture interface can be configured with:

    albion.local.processes.aodp-listen-devices=...
