import requests
import json
import pyzbar.pyzbar as pyzbar
import cv2
import pprint
# 부경대학교 대연캠퍼스의 버스정류장 = 35.134199, 129.103163

print("QR코드를 스캔하세요.\n")

# 목적지 불러오기(qr코드 인식)
cap = cv2.VideoCapture(0)

qrcode_data = ""
while cap.isOpened():
    ret, img = cap.read()

    if not ret:
        continue

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    decoded = pyzbar.decode(gray)

    for d in decoded:
        x, y, w, h = d.rect
        qrcode_data = d.data.decode("utf-8")
        cv2.rectangle(img, (x, y), (x + w, y + h), (0, 0, 255), 2)
   
    cv2.imshow("video", img)
    
    cv2.waitKey(1)
    if qrcode_data != "":
        break

cap.release()
cv2.destroyAllWindows()

# 불러온 위도,경도를 endX, endY에 할당
endX = ""
endY = ""

if len(qrcode_data) is 21: 
    endX = qrcode_data[12:]
    endY = qrcode_data[0:10]

# 파싱
r = requests.post("https://maps.googleapis.com/maps/api/directions/json?origin=35.134199,129.103163&destination="+
                  endX+","+endY+"&mode=transit&transit_mode=bus|subway&language=ko&key=AIzaSyDaVL22v6JiLtVtL52rnYi4hy7z3XKXJXE")
res = json.loads(r.text)

if res['status'] != "NOT_FOUND":
    distance = res['routes'][0]['legs'][0]['distance']['text']
    duration = res['routes'][0]['legs'][0]['duration']['text']
    arrival = res['routes'][0]['legs'][0]['arrival_time']['text']
    departure = res['routes'][0]['legs'][0]['departure_time']['text']
    end_add = res['routes'][0]['legs'][0]['end_address'][5:]
    start_add = res['routes'][0]['legs'][0]['start_address'][5:]

    print("출발지 주소 : " + start_add + "\n도착지 주소 : " + end_add)
    print("\n출발 시간 : " + departure + ", 도착 시간 : " + arrival)
    print("거리 : " + distance + ", 소요 시간 : " + duration)
    print()

    try:
        i = 0
        while res['routes'][0]['legs'][0]['steps'][i]['html_instructions'] is not None:
            description = res['routes'][0]['legs'][0]['steps'][i]['html_instructions']
            dur = res['routes'][0]['legs'][0]['steps'][i]['duration']['text']
            dis = res['routes'][0]['legs'][0]['steps'][i]['distance']['text']
            detail = ""

            print(i + 1, end="")
            print(". " + description + ", " + dur + ", " + dis)
            if res['routes'][0]['legs'][0]['steps'][i]['travel_mode'] == "TRANSIT":
                detail = res['routes'][0]['legs'][0]['steps'][i]['transit_details']['line']['short_name']
                print("\t정보:" + detail)
            i += 1
    except IndexError:
        pass
else:
    print("Wrong request.")