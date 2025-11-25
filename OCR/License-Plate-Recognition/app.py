from flask import Flask, request, jsonify
import os
import cv2
import numpy as np
from datetime import datetime
import torch
from flask_sqlalchemy import SQLAlchemy
import function.utils_rotate as utils_rotate
import function.helper as helper

app = Flask(__name__)

# --- Cấu hình SQLite ---
app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:///parking2.db"
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

db = SQLAlchemy(app)

# --- Models ---
class ParkingRecord(db.Model):
    __tablename__ = "parking_records"

    id = db.Column(db.Integer, primary_key=True)
    plate_number = db.Column(db.String(20), nullable=False)
    checkin_time = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    checkout_time = db.Column(db.DateTime, nullable=True)
    checkin_image_path = db.Column(db.String(255), nullable=True)
    checkout_image_path = db.Column(db.String(255), nullable=True)

    history = db.relationship("ParkingHistory", back_populates="record", cascade="all, delete-orphan")

    def to_dict(self):
        return {
            "id": self.id,
            "plate_number": self.plate_number,
            "checkin_time": self.checkin_time.isoformat(),
            "checkout_time": self.checkout_time.isoformat() if self.checkout_time else None,
            "checkin_image_path": self.checkin_image_path,
            "checkout_image_path": self.checkout_image_path,
            "history": [h.to_dict() for h in self.history]
        }

class ParkingHistory(db.Model):
    __tablename__ = "parking_history"

    id = db.Column(db.Integer, primary_key=True)
    record_id = db.Column(db.Integer, db.ForeignKey("parking_records.id"), nullable=False)
    action = db.Column(db.String(20), nullable=False)   # "checkin" hoặc "checkout"
    timestamp = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    record = db.relationship("ParkingRecord", back_populates="history")

    def to_dict(self):
        return {
            "id": self.id,
            "action": self.action,
            "timestamp": self.timestamp.isoformat()
        }

# --- Khởi tạo DB ---
with app.app_context():
    db.create_all()

# --- Folder lưu ảnh ---
UPLOAD_FOLDER = r"C:\Users\nguye\project\KnowledtreeV1\uploads"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# --- Load YOLO models ---
yolo_LP_detect = torch.hub.load('yolov5', 'custom', path='model/LP_detector.pt', source='local')
yolo_license_plate = torch.hub.load('yolov5', 'custom', path='model/LP_ocr.pt', source='local')
yolo_license_plate.conf = 0.60

# --- Giá mỗi giờ ---
GIA_MOI_GIO = 5000

# --- Helper nhận diện biển số ---
def recognize_plate(img):
    plates = yolo_LP_detect(img, size=640)
    list_plates = plates.pandas().xyxy[0].values.tolist()
    list_read = set()

    if len(list_plates) == 0:
        lp = helper.read_plate(yolo_license_plate, img)
        if lp != "unknown":
            list_read.add(lp)
    else:
        for p in list_plates:
            x, y = int(p[0]), int(p[1])
            w, h = int(p[2] - p[0]), int(p[3] - p[1])
            crop = img[y:y+h, x:x+w]

            flag = 0
            for cc in range(2):
                for ct in range(2):
                    lp = helper.read_plate(
                        yolo_license_plate,
                        utils_rotate.deskew(crop, cc, ct)
                    )
                    if lp != "unknown":
                        list_read.add(lp)
                        flag = 1
                        break
                if flag:
                    break
    return list(list_read)

# --- API xe vào ---
@app.route("/xe-vao", methods=["POST"])
def xe_vao():
    if "image" not in request.files:
        return jsonify({"error": "Thiếu file ảnh 'image'"}), 400

    file = request.files["image"]
    filename = f"{datetime.utcnow().strftime('%Y%m%d%H%M%S')}_{file.filename}"
    img_bytes = np.frombuffer(file.read(), np.uint8)
    img = cv2.imdecode(img_bytes, cv2.IMREAD_COLOR)

    plates = recognize_plate(img)
    bien_so = plates[0] if plates else "unknown"

    # Lưu ảnh check-in
    img_path = os.path.join(UPLOAD_FOLDER, filename)
    cv2.imwrite(img_path, img)

    # Kiểm tra xe đã check-in chưa
    existing = ParkingRecord.query.filter(
        ParkingRecord.plate_number == bien_so,
        ParkingRecord.checkout_time.is_(None)
    ).first()

    if existing:
        return jsonify({
            "plate_number": bien_so,
            "status": "xe_da_trong_bai",
            "checkin_image_path": existing.checkin_image_path
        })

    # Thêm record mới với ảnh check-in
    record = ParkingRecord(plate_number=bien_so, checkin_image_path=img_path)
    db.session.add(record)
    db.session.commit()

    # Thêm lịch sử check-in
    history = ParkingHistory(record_id=record.id, action="checkin")
    db.session.add(history)
    db.session.commit()

    return jsonify(record.to_dict())

# --- API xe ra ---
@app.route("/xe-ra", methods=["POST"])
def xe_ra():
    if "image" not in request.files:
        return jsonify({"error": "Thiếu file ảnh 'image'"}), 400

    file = request.files["image"]
    filename = f"{datetime.utcnow().strftime('%Y%m%d%H%M%S')}_{file.filename}"
    img_bytes = np.frombuffer(file.read(), np.uint8)
    img = cv2.imdecode(img_bytes, cv2.IMREAD_COLOR)

    plates = recognize_plate(img)
    bien_so = plates[0] if plates else "unknown"

    if bien_so == "unknown":
        return jsonify({"plate_number": "unknown", "status": "khong_doc_duoc_bien_so"})

    # Tìm record chưa checkout
    record = ParkingRecord.query.filter(
        ParkingRecord.plate_number == bien_so,
        ParkingRecord.checkout_time.is_(None)
    ).first()

    if not record:
        return jsonify({"plate_number": bien_so, "status": "khong_tim_thay_xe_trong_bai"})

    # Lưu ảnh checkout
    img_path = os.path.join(UPLOAD_FOLDER, filename)
    cv2.imwrite(img_path, img)
    record.checkout_time = datetime.utcnow()
    record.checkout_image_path = img_path
    db.session.commit()

    # Thêm lịch sử checkout
    history = ParkingHistory(record_id=record.id, action="checkout")
    db.session.add(history)
    db.session.commit()

    # Tính tiền gửi xe
    delta = record.checkout_time - record.checkin_time
    so_gio = delta.total_seconds() / 3600
    so_gio_tinh_tien = int(so_gio) if so_gio.is_integer() else int(so_gio) + 1
    tong_tien = so_gio_tinh_tien * GIA_MOI_GIO

    return jsonify({
        "plate_number": bien_so,
        "checkin_time": record.checkin_time.isoformat(),
        "checkout_time": record.checkout_time.isoformat(),
        "checkin_image_path": record.checkin_image_path,
        "checkout_image_path": record.checkout_image_path,
        "so_gio_tinh_tien": so_gio_tinh_tien,
        "tong_tien": tong_tien,
        "status": "xe_ra"
    })

# --- API lịch sử ---
@app.route("/history", methods=["GET"])
def history():
    status = request.args.get("status")  # xe_vao, xe_ra hoặc None
    plate = request.args.get("plate")    # biển số

    query = ParkingRecord.query

    if status == "xe_vao":
        query = query.filter(ParkingRecord.checkout_time.is_(None))
    elif status == "xe_ra":
        query = query.filter(ParkingRecord.checkout_time.isnot(None))

    if plate:
        query = query.filter(ParkingRecord.plate_number == plate)

    records = query.order_by(ParkingRecord.checkin_time.desc()).all()
    result = []

    for r in records:
        r_dict = r.to_dict()
        # Tính tiền nếu đã checkout
        if r.checkout_time:
            delta = r.checkout_time - r.checkin_time
            so_gio = delta.total_seconds() / 3600
            so_gio_tinh_tien = int(so_gio) if so_gio.is_integer() else int(so_gio) + 1
            tong_tien = so_gio_tinh_tien * GIA_MOI_GIO
            r_dict["so_gio_tinh_tien"] = so_gio_tinh_tien
            r_dict["tong_tien"] = tong_tien
        result.append(r_dict)

    return jsonify(result)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)
