@echo off
echo ===================================
echo   Compiling TalentTracker Project...
echo ===================================

SET "JAVAFX_PATH=C:\Program Files\javafx-sdk-21.0.7\lib"
SET "SRC_FILES=com\talenttracker\MainApp.java com\talenttracker\controller\AddProjectController.java com\talenttracker\controller\HeaderController.java"

javac --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -d out %SRC_FILES%

if %errorlevel% neq 0 (
    echo.
    echo !!!!!!!!!!!!!!!!!!
    echo COMPILATION FAILED!
    echo !!!!!!!!!!!!!!!!!!
    pause
    exit /b %errorlevel%
)

echo.
echo Compilation Successful!
echo.
echo ===================================
echo   Copying resources...
echo ===================================

copy com\talenttracker\view\AddProject.fxml out\com\talenttracker\view\AddProject.fxml
copy com\talenttracker\view\HeaderView.fxml out\com\talenttracker\view\HeaderView.fxml
copy com\talenttracker\view\style.css out\com\talenttracker\view\style.css

echo.
echo ===================================
echo   Launching Application...
echo ===================================

java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp out com.talenttracker.MainApp

echo.
echo ===================================
echo   Application Closed.
echo ===================================
pause 