from flask import Flask
app = Flask(__name__)

@app.get("/")
def home():
    return "Flask đã chạy OK!"

app.run(port=8000)
