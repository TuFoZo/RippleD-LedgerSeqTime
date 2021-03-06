# RippleD-LedgerSeqTime: 
LedgerSeqTime is a Java app to query Ripple public URL Daemon for Server_Info and record Sequence &amp; Time historical data.  Although more detailed information on XRP metrics can be found at https://xrpcharts.ripple.com/#/metrics, the purpose of this app is to get familiarity using Ripple Deamon via JAVA as well as understanding basic Ripple methods, requests and responses.

## Intro:
- App makes JSON RPC calls to Ripple public servers' (via server_info method) every x seconds for x minutes of duration.
- After every request to the Ripple server, response of time & validated_ledger.seq from daemon is written to a local file.
- After x minutes of polling, local file results are loaded into an array and the min, max, avg time of when ledger sequences are incremented over given time are calculated.

## Global Constants in the app:
Change the global values below in the code to adjust the output for your needs.
- Static file in local system (Windows) to write our output:  MY_FILE = "C:/myoutput/data.txt"
- Polling interval of Ripple Daemon in milliseconds - set to 1 second as default:  POLL_INTERVAL = 1000
- Duration of the polling in milliseconds - set to 3 minutes as default:  SLEEP_DURATION = 180000
- Public Rippled Server URL:  RIPPLE_URL = "http://s1.ripple.com:51234"
- JSON request for server_info method:  JSON_REQUEST = "{ \"method\" : \"server_info\" , \"params\" : [ {} ] }"
