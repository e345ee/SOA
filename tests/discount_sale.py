import argparse
import json
import uuid
from urllib.error import HTTPError
from urllib.request import Request, urlopen


def request(method, url, expected, data=None):
    headers = {"Accept": "application/json"}
    if data is not None:
        headers["Content-Type"] = "application/json"
    req = Request(url, json.dumps(data).encode() if data is not None else None, headers, method=method)
    try:
        response = urlopen(req, timeout=30)
    except HTTPError as error:
        response = error
    with response:
        assert response.status == expected, (method, url, response.status, response.read())
        if expected == 204:
            return None
        return json.load(response)


def check(ticket, booking):
    event = uuid.uuid4().int >> 68
    person = request("POST", ticket + "/persons", 201, {"birthday": "2000-01-01",
                     "location": {"x": 0, "y": 1, "name": "sale-test"}})
    source = request("POST", ticket + "/tickets", 201, {"name": "sale-test",
                     "coordinates": {"x": 1.7}, "price": 50, "personId": person["id"], "eventId": event})
    sell = f"{booking}/reservation/sell/discount/{source['id']}/{person['id']}/"
    try:
        first = request("POST", sell + "20", 201)
        second = request("POST", sell + "20", 201)
        assert first["price"] == second["price"] == 40
        assert first["id"] != second["id"]
        assert first["person"]["id"] == person["id"]
        free = request("POST", sell + "100", 201)
        assert free["price"] == 1
        request("POST", sell + "101", 400)
        page = request("POST", ticket + "/tickets/search", 200, {"filter": {"eventId": event}, "page": 0, "size": 10})
        assert page["totalElements"] == 4
    finally:
        request("POST", f"{ticket}/tickets/cancel-event/{event}", 204)
        request("DELETE", f"{ticket}/persons/{person['id']}", 204)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Проверка продажи со скидкой на тестовом стенде")
    parser.add_argument("ticket", help="Базовый URL Ticket Service до /api/v1 включительно")
    parser.add_argument("booking", help="Базовый URL Booking Service до /api/v1 включительно")
    args = parser.parse_args()
    check(args.ticket.rstrip("/"), args.booking.rstrip("/"))
    print("Продажа со скидкой и создание нового билета при повторе проверены.")
