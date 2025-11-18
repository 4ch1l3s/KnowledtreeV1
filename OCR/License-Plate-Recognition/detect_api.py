from flask import Flask, request, jsonify
import os
import cv2
import numpy as np
import json
from datetime import datetime
import torch
import function.utils_rotate as utils_rotate
import function.helper as helper

app = Flask(__name__)

# File JSON lưu dữ liệu
SAVE_JSON = r"C:\Users\nguye\project\KnowledtreeV1\du_lieu_xe.json"

# Load YOLO models (chỉ load 1 lần khi server khởi động)
yolo_LP_detect = torch.hub.load('yolov5', 'custom', path='model/LP_detector.pt', source='local')
yolo_license_plate = torch.hub.load('yolov5', 'custom', path='model/LP_ocr.pt', source='local')
yolo_license_plate.conf = 0.60


@app.route("/detect", methods=["POST"])
def detect_plate():
    if "image" not in request.files:
        return jsonify({"error": "Thiếu file ảnh 'image'"}), 400

    file = request.files["image"]

    # Chuyển file upload sang OpenCV image
    img_bytes = np.frombuffer(file.read(), np.uint8)
    img = cv2.imdecode(img_bytes, cv2.IMREAD_COLOR)

    # YOLO detect biển số
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

    # Lấy biển số đầu tiên hoặc unknown
    bien_so = list(list_read)[0] if len(list_read) else "unknown"
    thoi_gian = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    # Lưu file ảnh upload (nếu muốn)
    save_folder = r"C:\Users\nguye\project\KnowledtreeV1\uploads"
    os.makedirs(save_folder, exist_ok=True)
    img_path = os.path.join(save_folder, file.filename)
    cv2.imwrite(img_path, img)

    # Lưu JSON record
    if os.path.exists(SAVE_JSON):
        try:
            with open(SAVE_JSON, "r", encoding="utf-8") as f:
                data_list = json.load(f)
        except:
            data_list = []
    else:
        data_list = []

    for item in data_list:
        if item["bien_so_xe"] == bien_so and item["trang_thai"] == "xe_vao":
            return jsonify({
                "bien_so": bien_so,
                "trang_thai": "xe_da_trong_bai"
            })

    data_new = {
        "bien_so_xe": bien_so,
        "thoi_gian_gui": thoi_gian,
        "link_image": img_path,
        "trang_thai": "xe_vao"
    }

    data_list.append(data_new)

    with open(SAVE_JSON, "w", encoding="utf-8") as f:
        json.dump(data_list, f, indent=4, ensure_ascii=False)

    return jsonify({
        "bien_so": bien_so,
        "thoi_gian_gui": thoi_gian,
        "link_image": img_path,
        "trang_thai": "xe_vao"
    })


@app.route("/xe-ra", methods=["POST"])
def xe_ra():
    if "image" not in request.files:
        return jsonify({"error": "Thiếu file ảnh 'image'"}), 400

    file = request.files["image"]

    # Convert file thành ảnh OpenCV
    img_bytes = np.frombuffer(file.read(), np.uint8)
    img = cv2.imdecode(img_bytes, cv2.IMREAD_COLOR)

    # YOLO detect biển số
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

    bien_so = list(list_read)[0] if len(list_read) else "unknown"
    thoi_gian_ra = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    # Không đọc được biển số
    if bien_so == "unknown":
        return jsonify({
            "bien_so": "unknown",
            "trang_thai": "khong_doc_duoc_bien_so"
        })

    # Load JSON
    if os.path.exists(SAVE_JSON):
        try:
            with open(SAVE_JSON, "r", encoding="utf-8") as f:
                data_list = json.load(f)
        except:
            data_list = []
    else:
        data_list = []

    # Kiểm tra xe có đang ở trong bãi không
    xe_trong_bai = None
    for item in data_list:
        if item["bien_so_xe"] == bien_so and item["trang_thai"] == "xe_vao":
            xe_trong_bai = item
            break

    # Nếu không tìm thấy xe vào trước đó
    if xe_trong_bai is None:
        return jsonify({
            "bien_so": bien_so,
            "trang_thai": "khong_tim_thay_xe_trong_bai"
        })

    # Cập nhật trạng thái xe ra
    xe_trong_bai["trang_thai"] = "xe_ra"
    xe_trong_bai["thoi_gian_ra"] = thoi_gian_ra

        
    # ======= TÍNH TIỀN GỬI XE =======
    fmt = "%Y-%m-%d %H:%M:%S"
    t_vao = datetime.strptime(xe_trong_bai["thoi_gian_gui"], fmt)
    t_ra = datetime.strptime(thoi_gian_ra, fmt)

    # Tổng phút gửi
    so_phut = (t_ra - t_vao).total_seconds() / 60

    # Tính giờ làm tròn lên
    so_gio = so_phut / 60
    so_gio_tinh_tien = int(so_gio) if so_gio.is_integer() else int(so_gio) + 1

    # Giá mỗi giờ
    GIA_MOI_GIO = 5000  # đổi tùy ý

    # Tính tổng tiền
    tien = so_gio_tinh_tien * GIA_MOI_GIO

    # Lưu thêm vào dữ liệu JSON
    xe_trong_bai["tong_tien"] = tien
    xe_trong_bai["so_gio_tinh_tien"] = so_gio_tinh_tien



    # Ghi lại JSON
    with open(SAVE_JSON, "w", encoding="utf-8") as f:
        json.dump(data_list, f, indent=4, ensure_ascii=False)

    return jsonify({
        "bien_so": bien_so,
        "thoi_gian_vao": xe_trong_bai["thoi_gian_gui"],
        "thoi_gian_ra": thoi_gian_ra,
        "so_gio_tinh_tien": so_gio_tinh_tien,
        "tong_tien": tien,
        "trang_thai": "xe_ra"
    })



if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)
