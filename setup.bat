@echo off
REM --------- Tạo thư mục OCR nếu chưa có ---------
SET OCR_DIR=%~dp0ocr2

IF NOT EXIST "%OCR_DIR%" (
    mkdir "%OCR_DIR%"
    echo Thu muc "%OCR_DIR%" da duoc tao.
) ELSE (
    echo Thu muc "%OCR_DIR%" da ton tai.
)

REM --------- Di chuyen vao thu muc OCR ---------
cd /d "%OCR_DIR%"

REM --------- Clone Git repo ---------
SET REPO_URL=https://github.com/trungdinh22/License-Plate-Recognition.git

REM Kiem tra thu muc repo da ton tai chua
FOR /D %%D IN ("%OCR_DIR%\License-Plate-Recognition") DO (
    SET REPO_EXIST=1
)

REM --------- Kiểm tra repo đã tồn tại chưa ---------
IF EXIST "%REPO_DIR%" (
    echo Repo da ton tai, khong clone lai.
) ELSE (
    echo Dang clone repo tu %REPO_URL% ...
    git clone %REPO_URL%
)

REM --------- Di chuyen vao thu muc License-Plate-Recognition ---------
cd /d "%REPO_DIR%"
echo Da chuyen vao thu muc: %CD%

REM --------- Tạo virtual environment Python 3.10 trong .venv ---------
REM Kiểm tra Python 3.10
py -3.10 -m venv .venv

REM --------- Kích hoạt virtual environment ---------
call .venv\Scripts\activate.bat

echo Da tao va kich hoat .venv voi Python 3.10

REM --------- Ghi nội dung mới vào requirements.txt ---------
(
echo asttokens==3.0.1
echo certifi==2025.11.12
echo charset-normalizer==3.4.4
echo colorama==0.4.6
echo contourpy==1.3.2
echo cycler==0.12.1
echo decorator==5.2.1
echo exceptiongroup==1.3.0
echo executing==2.2.1
echo filelock==3.20.0
echo fonttools==4.60.1
echo fsspec==2025.10.0
echo idna==3.11
echo ipython==8.37.0
echo jedi==0.19.2
echo Jinja2==3.1.6
echo kiwisolver==1.4.9
echo MarkupSafe==3.0.3
echo matplotlib==3.10.7
echo matplotlib-inline==0.2.1
echo mpmath==1.3.0
echo networkx==3.4.2
echo numpy==1.25.2
echo opencv-python==4.12.0.88
echo packaging==25.0
echo pandas==2.3.3
echo parso==0.8.5
echo pillow==12.0.0
echo pip==25.3
echo prompt_toolkit==3.0.52
echo pure_eval==0.2.3
echo Pygments==2.19.2
echo pyparsing==3.2.5
echo python-dateutil==2.9.0.post0
echo pytz==2025.2
echo PyYAML==6.0.3
echo requests==2.32.5
echo scipy==1.15.3
echo seaborn==0.13.2
echo setuptools==57.4.0
echo six==1.17.0
echo stack-data==0.6.3
echo sympy==1.14.0
echo torch==1.13.1+cu117
echo torchaudio==0.13.1+cu117
echo torchvision==0.14.1+cu117
echo tqdm==4.67.1
echo traitlets==5.14.3
echo typing_extensions==4.15.0
echo tzdata==2025.2
echo urllib3==2.5.0
echo wcwidth==0.2.14
) > "%REQ_FILE%"

echo Da sua xong file requirements.txt

pause
