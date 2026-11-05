import argparse
import json
import uuid
import ssl
from concurrent.futures import ThreadPoolExecutor
from urllib.error import HTTPError
from urllib.request import Request, urlopen


def request(method, url, expected, data=None, key=None, discard=False):
    headers = {"Accept": "application/json", "X-Request-ID": str(uuid.uuid4())}
    if data is not None:
        headers["Content-Type"] = "application/json"
    if key:
        headers["Idempotency-Key"] = key
    req = Request(url, json.dumps(data).encode() if data is not None else None, headers, method=method)
    try:
        response = urlopen(req, timeout=30)
    except HTTPError as error:
        response = error
    with response:
        assert response.status == expected, (method, url, response.status, response.read())
        assert response.headers["X-Request-ID"] == headers["X-Request-ID"]
        if discard or expected == 204:
            return
        return json.load(response)


def check(ticket, booking, fault_control=None, fault_ca=None):
    event = uuid.uuid4().int >> 68
    person = request("POST", ticket + "/persons", 201, {"birthday": "2000-01-01",
                     "location": {"x": 0, "y": 1, "name": "sale-test"}})
    source = request("POST", ticket + "/tickets", 201, {"name": "sale-test",
                     "coordinates": {"x": 1.7}, "price": 50, "personId": person["id"], "eventId": event})
    sell = f"{booking}/reservation/sell/discount/{source['id']}/{person['id']}/"
    key = str(uuid.uuid4())
    try:
        request("POST", sell + "20", 201, key=key, discard=True)
        replay = request("POST", sell + "20", 201, key=key)
        assert replay["price"] == 40 and replay["person"]["id"] == person["id"]
        request("POST", sell + "30", 409, key=key)
        concurrent_key = str(uuid.uuid4())
        with ThreadPoolExecutor(max_workers=8) as pool:
            results = list(pool.map(lambda _: request("POST", sell + "100", 201, key=concurrent_key), range(8)))
        assert all(item == results[0] for item in results) and results[0]["price"] == 1
        assert results[0]["id"] != replay["id"]
        page = request("POST", ticket + "/tickets/search", 200, {"filter": {"eventId": event}, "page": 0, "size": 10})
        assert page["totalElements"] == 3
        if fault_control:
            context = ssl.create_default_context(cafile=fault_ca)
            for mode in ("drop", "timeout", "unavailable"):
                fault_key = str(uuid.uuid4())
                with urlopen(Request(fault_control + "/mode/" + mode, method="POST"), context=context) as response:
                    assert response.status == 204
                request("POST", sell + "20", 502, key=fault_key)
                with urlopen(Request(fault_control + "/mode/normal", method="POST"), context=context) as response:
                    assert response.status == 204
                restored = request("POST", sell + "20", 201, key=fault_key)
                assert request("POST", sell + "20", 201, key=fault_key) == restored
            page = request("POST", ticket + "/tickets/search", 200, {"filter": {"eventId": event}, "page": 0, "size": 10})
            assert page["totalElements"] == 6
        request("DELETE", f"{ticket}/tickets/{source['id']}", 204)
        assert request("POST", sell + "20", 201, key=key) == replay
        request("POST", sell + "20", 404, key=str(uuid.uuid4()))
        request("POST", sell + "20", 400, key="invalid")
    finally:
        request("POST", f"{ticket}/tickets/cancel-event/{event}", 204)
        request("DELETE", f"{ticket}/persons/{person['id']}", 204)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Проверка на тестовом стенде с PostgreSQL и миграцией V003")
    parser.add_argument("ticket", help="Базовый URL Ticket Service до /api/v1 включительно")
    parser.add_argument("booking", help="Базовый URL Booking Service до /api/v1 включительно")
    parser.add_argument("--fault-control")
    parser.add_argument("--fault-ca")
    args = parser.parse_args()
    check(args.ticket.rstrip("/"), args.booking.rstrip("/"), args.fault_control, args.fault_ca)
    print("Потеря ответа, повтор, конфликт и параллельные продажи проверены.")
