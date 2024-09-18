# This unit test checks the python_server.py
# Run `pytest` in the root directory of the jRDF2vec project (where the pom.xml resides).

import threading
import python_server as server
import time
import requests
from pathlib import Path


uri_prefix = "http://localhost:1808/"
base_dir = Path(__file__).resolve().parent.parent.parent.parent / "test" / "resources"

class ServerThread(threading.Thread):
    def __init__(self, *args, **kwargs):
        super(ServerThread, self).__init__(*args, **kwargs)
        self._stop_event = threading.Event()
        self.daemon = True

    def run(self):
        server.main()

    def stop(self):
        self._stop_event.set()
        requests.get(uri_prefix + "shutdown")

    def stopped(self):
        return self._stop_event.is_set()


server_thread = ServerThread()


def setup_module(module):
    """Let's start the server."""
    wait_time_seconds = 10
    server_thread.start()
    print(f"Waiting {wait_time_seconds} seconds for the server to start.")
    time.sleep(wait_time_seconds)


def test_get_vector():
    vector_test_path = (base_dir / "test_model_vectors.kv")
    assert vector_test_path.is_file()
    result = requests.get(
        uri_prefix + "get-vector",
        headers={"concept": "Europe", "vector-path": str(vector_test_path)},
    )
    assert len(result.content.decode("utf-8").split(" ")) == 100


def test_is_in_vocabulary():
    model_test_path = (base_dir / "test_model")
    vector_test_path = (base_dir / "test_model_vectors.kv")
    assert model_test_path.is_file()
    assert vector_test_path.is_file()
    result = requests.get(
        uri_prefix + "is-in-vocabulary",
        headers={"concept": "Europe", "model-path": str(model_test_path)},
    )
    assert result.content.decode("utf-8") == "True"
    result = requests.get(
        uri_prefix + "is-in-vocabulary",
        headers={"concept": "Europe", "vector-path": str(vector_test_path)},
    )
    assert result.content.decode("utf-8") == "True"


def test_get_similarity():
    model_test_path = (base_dir / "test_model")
    assert model_test_path.is_file()
    result = requests.get(
        uri_prefix + "get-similarity",
        headers={
            "concept-1": "Europe", 
        "concept-2": "united",
        "model-path": str(model_test_path)},
    )
    result_str = result.content.decode("utf-8")
    assert float(result_str) > 0


def teardown_module(module):
    print("Shutting down...")
    server_thread.stop()
