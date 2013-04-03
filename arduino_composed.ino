#include <SoftwareSerial.h>
#include <TinyGPS.h>

////////////////////////////////////////////////////////////
//fsr
const int fsrAnalogPin1= 3; // FSR is connected to analog 0
const int fsrAnalogPin2= 4; // FSR is connected to analog 0
int fsrReading1;      // the analog reading from the FSR resistor1 divider
int fsrReading2;      // the analog reading from the FSR resistor1 divider
int stepCounter = 0;
int isStep = 0;       //flag when no step is 0, step detected is 1

///////////////////////////////////////////////////////////
//accelerometer
const int accAnalogPinX = 2;
const int accAnalogPinY = 1;
const int accAnalogPinZ = 0;
int xRead;
int yRead;
int zRead;

//The minimum and maximum values that came from
//the accelerometer while standing still
int minVal = 275;
int maxVal = 413;

///////////////////////////////////////////////////////////
//GPS
const int RXPIN = 2;
const int TXPIN = 3;

static int count = 0;
static int timer = 0;

float latitude, longitude, GPSspeed;

#define TERMBAUD 115200
#define GPSBAUD 4800

TinyGPS gps;
SoftwareSerial ss(RXPIN, TXPIN);

void getGPS(TinyGPS& gps);


void setup()
{
  Serial.begin(TERMBAUD);
  ///////////////////////////////////////////////////////////
  //GPS
  ss.begin(GPSBAUD);
}

void loop(void) {
  ////////////////////////////////////////////////////////////
  //fsr

  
  fsrReading1 = analogRead(fsrAnalogPin1);
  fsrReading2 = analogRead(fsrAnalogPin2);

  if((fsrReading1 > 50) ||(fsrReading2 > 50)  ) {
   
    if(isStep == 0){
      
      ++stepCounter;
      /*
      Serial.print("step = ");
      Serial.println(stepCounter); 
      
        //accelerometer
  Serial.println("acc:");
  Serial.print("x: ");  Serial.println(xRead);
  Serial.print("y: ");  Serial.println(yRead);
  Serial.print("z: ");  Serial.println(zRead);
  */

    }
     isStep = 1;
  }
  
  if((fsrReading1 || fsrReading2) == 0) 
    isStep = 0;
    
  ////////////////////////////////////////////////////////////

  
  xRead = analogRead(accAnalogPinX);
  yRead = analogRead(accAnalogPinY);
  zRead = analogRead(accAnalogPinZ);
  
  ////////////////////////////////////////////////////////////
  //GPS
  while(ss.available())     // While there is data on the RX pin...
  {
    int c = ss.read();    // load the data into a variable...
    if(gps.encode(c))      // if there is a new valid sentence...
    {
      getgps(gps);         // then grab the data.
      //delay(1000);
    }
  }
  
  timer++;
  //print out data every second
  if(timer%10 == 0){
     Serial.print("step ");
     Serial.println(stepCounter); 
      
    //accelerometer
    Serial.print("x ");  Serial.println(xRead);
    Serial.print("y ");  Serial.println(yRead);
    Serial.print("z ");  Serial.println(zRead);
    
    //GPS
    // Serial.println("GPSc:");
     Serial.print("Lat "); 
     Serial.println(latitude,5); 
     Serial.print("Lon "); 
     Serial.println(longitude,5);
     Serial.print("Speed "); 
     Serial.println(GPSspeed);
  }
  
  delay(100);
}

void getgps(TinyGPS &gps)
{
  count++;
  Serial.println(count);


  gps.f_get_position(&latitude, &longitude);
/*
  Serial.print("Lat/Long: "); 
  Serial.print(latitude,5); 
  Serial.print(", "); 
  Serial.println(longitude,5);
  */
  
  // Same goes for date and time
  int year;
  byte month, day, hour, minute, second, hundredths;
  gps.crack_datetime(&year,&month,&day,&hour,&minute,&second,&hundredths);
  /*
  // Print data and time
  Serial.print("Date: "); 
  Serial.print(month, DEC); Serial.print("/"); 
  Serial.print(day, DEC); Serial.print("/"); 
  Serial.println(year);

  Serial.print("Time: "); 
  Serial.print(hour, DEC); Serial.print(":"); 
  Serial.print(minute, DEC); Serial.print(":"); 
  Serial.print(second, DEC); Serial.print("."); 
  Serial.println(hundredths, DEC);
  //Since month, day, hour, minute, second, and hundr
  
  // Here you can print the altitude and course values directly since 
  // there is only one value for the function
  Serial.print("Altitude (meters): "); Serial.println(gps.f_altitude());  
  // Same goes for course
  Serial.print("Course (degrees): "); Serial.println(gps.f_course()); 
  // And same goes for speed
  */
  GPSspeed = gps.f_speed_kmph();
 // Serial.print("Speed(kmph): "); Serial.println(GPSspeed);
  //Serial.println();
  
  // Here you can print statistics on the sentences.
  unsigned long chars;
  unsigned short sentences, failed_checksum;
  gps.stats(&chars, &sentences, &failed_checksum);
  /*
  Serial.print("Failed Checksums: ");Serial.print(failed_checksum);
  Serial.println(); Serial.println();
  
  // Here you can print the number of satellites in view
  Serial.print("Satellites: ");
  Serial.println(gps.satellites());
  */
}
