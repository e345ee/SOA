import argparse
import http.client
import ssl
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


class Proxy(BaseHTTPRequestHandler):
    mode = "normal"
    lock = threading.Lock()

    def log_message(self, *args):
        pass

    def do_POST(self):
        if self.path.startswith("/mode/"):
            with self.lock:
                type(self).mode = self.path.removeprefix("/mode/")
            self.send_response(204)
            self.end_headers()
            return
        with self.lock:
            mode = self.mode
        if mode == "unavailable":
            self.close_connection = True
            return
        client = http.client.HTTPConnection("localhost", self.server.upstream, timeout=10)
        try:
            client.request("POST", self.path, headers={name: value for name, value in self.headers.items()
                           if name.lower() in ("idempotency-key", "x-request-id", "accept")})
            response = client.getresponse()
            body = response.read()
            if mode == "drop":
                self.close_connection = True
                return
            if mode == "timeout":
                time.sleep(3)
            self.send_response(response.status)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            try:
                self.wfile.write(body)
            except (BrokenPipeError, ConnectionResetError, ssl.SSLError):
                pass
        finally:
            client.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Локальный HTTPS-прокси с управляемыми сетевыми сбоями")
    parser.add_argument("--cert", required=True)
    parser.add_argument("--key", required=True)
    parser.add_argument("--port", type=int, default=9444)
    parser.add_argument("--upstream", type=int, default=8080)
    args = parser.parse_args()
    server = ThreadingHTTPServer(("127.0.0.1", args.port), Proxy)
    server.upstream = args.upstream
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain(args.cert, args.key)
    server.socket = context.wrap_socket(server.socket, server_side=True)
    server.serve_forever()
