# 📘 SETUP MODEL AI
---

## 🚀 Bắt đầu

### **Bước 1 — Mở Terminal và chạy file `setup.bat`**
Mở CMD hoặc PowerShell tại thư mục dự án và chạy:
```bash
setup.bat
```
File này sẽ tự động cài đặt tất cả các thư viện cần thiết.

---

## 🎥 Chạy chương trình Webcam
Sau khi setup xong, chạy lệnh:
```bash
python webcam.py
```
Webcam sẽ được kích hoạt và chương trình bắt đầu chạy.

---

## 🔧 Cấu hình Camera
Nếu webcam không mở được:
- Mở file **webcam.py**
- Kiểm tra dòng:
```python
vid = cv2.VideoCapture(0)
```
Nếu bạn dùng webcam ngoài, có thể đổi `0` thành `1` hoặc `2`.

---

## 📂 Cấu trúc thư mục
```
📦 Dự Án
 ┣ 📜 webcam.py
 ┣ 📜 setup.bat
 ┣ 📜 requirements.txt (tuỳ chọn)
 ┗ 📜 README.md
```

---

## 💡 Mẹo nhỏ
- Luôn chạy terminal tại đúng thư mục chứa dự án.
- Nếu lỗi thư viện, chạy lại:
```bash
pip install -r requirements.txt
```

---

## ❤️ Cảm ơn bạn đã sử dụng dự án

