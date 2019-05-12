#include "Arduino.h"
#include "ArduinoJson.h"
#include "LiquidCrystal_PCF8574.h"

// Global variables and defines
// There are several different versions of the LCD I2C adapter, each might have a different address.
// Try the given addresses by Un/commenting the following rows until LCD works follow the serial monitor prints.
// To find your LCD address go to: http://playground.arduino.cc/Main/I2cScanner and run example.
//#define LCD_ADDRESS 0x3F
#define LCD_ADDRESS 0x27

// Define LCD characteristics
#define LCD_ROWS 4
#define LCD_COLUMNS 20
#define SCROLL_DELAY 150
#define BACKLIGHT 255

// object initialization
LiquidCrystal_PCF8574 lcd20x4(LCD_ADDRESS);

// data received
String data = "no data";
StaticJsonBuffer<500> jsonBuffer;

char* line0 = "     NOW PLAYING2   ";
char* line1 = "     NOW PLAYING    ";
char* line2 = "     NOW PLAYING    ";
char* line3 = "     NOW PLAYING    ";

// example from https://github.com/mathertel/LiquidCrystal_PCF8574/blob/master/examples/LiquidCrystal_PCF8574_Test/LiquidCrystal_PCF8574_Test.ino

void setup() {
  // put your setup code here, to run once:
  lcd20x4.begin(LCD_COLUMNS, LCD_ROWS); 
  lcd20x4.setBacklight(255);

  Serial.begin(9600);
}

void loop() {
    // put your main code here, to run repeatedly:


    if(Serial.available() > 0) {
      data = Serial.readString();
      jsonBuffer.clear();
      JsonObject& root = jsonBuffer.parseObject(data);
      

      if (!root.success()) {
        Serial.println("parseObject() failed");
      } else {

        line1 = root["Track"];
        line2 = root["Artist"];
        line3 = "";
        
      }
    } else {
      Serial.print("did not receive any data this time\n");
      
    }

    // realising there's a race condition issue here where there's a bit of a jsonBuffer backlog
    redraw();
    delay(1000);
}

void redraw() {
  
    lcd20x4.clear();
    lcd20x4.setCursor(0,0);                    // Set cursor at the begining of line 1
    lcd20x4.print(line0);
    lcd20x4.setCursor(0,1);                    // Set cursor at the begining of line 2
    lcd20x4.print(line1);                   // Print print String to LCD on first line
    lcd20x4.setCursor(0,2);                    // Set cursor at the begining of line 3
    lcd20x4.print(line2);                   // Print print String to LCD on second line
    lcd20x4.setCursor(0,3);                    // Set cursor at the begining of line 4
    lcd20x4.print(line3);                   // Print print String to LCD on second line
}
