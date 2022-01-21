# This unit test checks the python_server.py
# Run `pytest` in the root directory of the jRDF2vec project (where the pom.xml resides).

import threading
import python_server as server
import time
import requests


class ServerThread(threading.Thread):

    def __init__(self,  *args, **kwargs):
        super(ServerThread, self).__init__(*args, **kwargs)
        self._stop_event = threading.Event()
    
    def run(self):
        server.main()

    def stop(self):
        self._stop_event.set()

    def stopped(self):
        return self._stop_event.is_set()
    
server_thread = ServerThread()

def setup_module(module):
    wait_time_seconds = 10
    server_thread.start()
    print(f"Waiting {wait_time_seconds} seconds for the server to start.")
    time.sleep(wait_time_seconds)

def test_my_test():
    test_model_vectors = "/src/main/resources/test/test_python_server.py"
    print("IN")
    assert(True)
    
def teardown_module(module):
    """
    print("Shutting down the server.")
    serverThread.stop()
    """
    print("Shutting down...")
    requests.get("http://localhost:1808/shutdown")

