#include <SoftwareSerial.h>
#include <Wire.h>
#include "MPU9250.h"
#include <TimerOne.h>

SoftwareSerial BT(10, 11); //RX, TX

char inbyte;


//objekt MPU9250 na I2C z addresem 0x68
MPU9250 IMU(Wire,0x68);
int status;
int controlNr = 0;

//flaga do odczytu danych przy przerwaniach
bool flag = false;

// Initial time
long int ti;
long int T;
volatile bool intFlag=false;


void setup() 
{
  Serial.begin(115200);
  while(!Serial) {}

  //dioda sygnalizujaca ze juz wszystko zrobione
  pinMode(6, OUTPUT); 
  digitalWrite(6, LOW);

  //do bluetooth
  BT.begin(115200); 

  //rozpoczxecie komunikacji z MPU 
  status = IMU.begin();
  if (status < 0) {
    Serial.println("IMU initialization unsuccessful");
    Serial.println("Check IMU wiring or try cycling power");
    Serial.print("Status: ");
    Serial.println(status);
    while(1) {}
  }
  
  // ustawienie skali akcelerometru do +/-2G 
  IMU.setAccelRange(MPU9250::ACCEL_RANGE_2G);
  // ustawienie skali żyroskopu do +/-250 deg/s
  IMU.setGyroRange(MPU9250::GYRO_RANGE_250DPS);
  // ustaiwienie DLPF bandwidth to 20 Hz
  IMU.setDlpfBandwidth(MPU9250::DLPF_BANDWIDTH_5HZ);
  // ustaiwienie SRD do 19 - daje to 50 Hz update rate
  // 0 - 1kHZ
    if (IMU.setSrd(0) < 0) {
    Serial.println("IMU initialization unsuccessful - SRD");
    while(1) {}
  }

  //bias i scale rate
  IMU.setAccelCalZ(-3.65905+0.05,0.991236);
  IMU.setAccelCalX(0.477-0.455,1.007);
  IMU.setAccelCalY(0.1147+0.032,1.01);
  IMU.setMagCalZ(-20,0.98);
  IMU.setMagCalX(31.9,0.93);
  IMU.setMagCalY(-15.2,0.98);

  Serial.println("IMU initialization done");

  //dioda
  digitalWrite(6, HIGH);
}

void loop() 
{    
  // odczyt danych z sensora
  IMU.readSensor();
  T = millis()-ti;
  // przeslanie danych
  sendAndroidValues();
  displayDataFromMPU();
  
  controlNr++;

  if(controlNr%250 == 125)
  {
    //dioda
  digitalWrite(6, LOW);
  }
  else if(controlNr%250 == 0)
  {
    digitalWrite(6, HIGH);
  }
}

void getIMU()
{ 
  flag = true;
}


//wyslanie danych przez BT
void sendAndroidValues()
 {
  //# przed wartosciami aby latwo je oddzielic w aplikacji
  //Serial.print('#');
  BT.print('#');
  BT.print(T,DEC);
  BT.print("+");
  BT.print(IMU.getAccelX_mss(),3);
  BT.print("+");
  BT.print(IMU.getAccelY_mss(),3);
  BT.print("+");
  BT.print(-IMU.getAccelZ_mss(),3);
  BT.print("+");
  BT.print(IMU.getGyroX_rads(),3);
  BT.print("+");
  BT.print(IMU.getGyroY_rads(),3);
  BT.print("+");
  BT.print(IMU.getGyroZ_rads(),3);
  BT.print("+");
  BT.print(IMU.getMagX_uT(),2);
  BT.print("+");
  BT.print(IMU.getMagY_uT(),2);
  BT.print("+");
  BT.print(IMU.getMagZ_uT(),2);
  BT.print("+");
  BT.print(T,DEC);
  BT.print("+");
 BT.print('~'); //uzywany do odczytu konca transmisji w aplikacji
 BT.println(controlNr); 
}


void displayDataFromMPU()
{
  Serial.print(T,DEC);
  Serial.print("\t");
  Serial.print(IMU.getAccelX_mss(),3);
  Serial.print("\t");
  Serial.print(IMU.getAccelY_mss(),3);
  Serial.print("\t");
  Serial.print(-IMU.getAccelZ_mss(),3);
  Serial.print("\t");
  Serial.print(IMU.getGyroX_rads(),3);
  Serial.print("\t");
  Serial.print(IMU.getGyroY_rads(),3);
  Serial.print("\t");
  Serial.print(IMU.getGyroZ_rads(),3);
  Serial.print("\t");
  Serial.print(IMU.getMagX_uT(),3);
  Serial.print("\t");
  Serial.print(IMU.getMagY_uT(),3);
  Serial.print("\t");
  Serial.print(IMU.getMagZ_uT(),3);
  Serial.print("\t");
  Serial.println(controlNr);
}



