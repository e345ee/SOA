import argparse
import json
import uuid
from pathlib import Path
from urllib.request import Request, urlopen


def events(path):
    result = []
    for line in Path(path).read_text().splitlines():
        line = line.strip()
        if line.startswith("{") and '"event":' in line:
            try:
                result.append(json.JSONDecoder().raw_decode(line)[0])
            except ValueError:
                pass
    return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Проверить счётчики запроса и корреляцию по журналам Payara")
    parser.add_argument("ticket")
    parser.add_argument("ticket_log")
    parser.add_argument("booking_log")
    args = parser.parse_args()
    request_id = str(uuid.uuid4())
    with urlopen(Request(args.ticket + "/tickets", headers={"X-Request-ID": request_id})) as response:
        assert response.headers["X-Request-ID"] == request_id
        response.read()
    ticket_events = events(args.ticket_log)
    current = [event for event in ticket_events if event["requestId"] == request_id]
    sql = [event for event in current if event["event"] == "sql"]
    http = next(event for event in current if event["event"] == "http")
    assert http["sqlCount"] == len(sql) >= 2
    assert abs(http["sqlMs"] - sum(event["durationUs"] for event in sql) / 1000) < 0.00001
    assert all(len(event["queryId"]) == 64 for event in sql)
    bookings = events(args.booking_log)
    forwarded = [event for event in bookings if event["event"] == "downstream" and event["status"] == 201]
    assert forwarded
    ticket_ids = {event["requestId"] for event in ticket_events if event["event"] == "http"}
    assert all(event["requestId"] in ticket_ids for event in forwarded)
    errors = [event for event in bookings if event["event"] == "http" and event["status"] == 502]
    assert errors
    print(f"SQL-счётчик {len(sql)}, длительности и корреляция {len(forwarded)} межсервисных вызовов проверены.")
