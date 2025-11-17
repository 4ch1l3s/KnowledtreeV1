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

pause
