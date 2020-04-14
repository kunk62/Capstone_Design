import requests
import json
import cv2
import pyzbar.pyzbar as pyzbar

# 부경대학교 대연캠퍼스의 버스정류장 = 35.134199, 129.103163

print("QR코드를 스캔하세요.")

# 목적지 불러오기(qr코드 인식)
cap = cv2.VideoCapture(0)
i = 0
while (cap.isOpened()):
    ret, img = cap.read()

    if not ret:
        continue

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    decoded = pyzbar.decode(gray)

    for d in decoded:
        qrcode_data = d.data.decode("utf-8")

    if qrcode_data is not None:
        break

cap.release()
cv2.destroyAllWindows()

# <dummy> qrcode_data = "위도 : 129.060044\n경도 : 35.157895"
# 불러온 위도,경도를 endX, endY에 할당
endX = qrcode_data[21:]
endY = qrcode_data[5:15]

print("출발 :35.134199, 129.103163") # 부경대학교 대연캠퍼스 1호관
print("도착 :"+endX, endY)

# 파싱
r = requests.post("https://maps.googleapis.com/maps/api/directions/json?origin=35.134199,129.103163&destination="+
                  endX+","+endY+"&mode=transit&transit_mode=bus|subway&language=ko&key=AIzaSyDaVL22v6JiLtVtL52rnYi4hy7z3XKXJXE")
res = json.loads(r.text)

distance = res['routes'][0]['legs'][0]['distance']['text']
duration = res['routes'][0]['legs'][0]['duration']['text']

print("거리 : "+distance+", 소요 시간 : "+duration)

try:
    i = 0
    while res['routes'][0]['legs'][0]['steps'][i]['html_instructions'] is not None:
        description = res['routes'][0]['legs'][0]['steps'][i]['html_instructions']
        print(description)
        i += 1
except IndexError:
    pass

