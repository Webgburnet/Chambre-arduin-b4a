/* 
* PROJET DOMOTIQUE CAR TECH INNO
* 
* Arduino Uno
* 
* A0: Grove Capteur Temperature & Humidité
* A1: Luminosite LDR
* A2: NC
* A3: NC
* A4: NC
* A5: NC
* 
* pin0 : RX rs232 non utilise
* pin1 : TX rs232 non utilise
* pin2 : NC
* pin3 : NC
* 
* pin4 : Digital Output VR monter
* pin5 : Digital Output VR descendre
* pin6 : Digital Output Prise
* pin7 : Digital Output Plafonnier
* pin8 : Digital Output Radiateur
* 
* pin9 : NC
* pin10 : NC
* pin11 : NC
* pin12 : NC
* pin13 : NC 
* 
* I2C : Afficheur LCD RGB
* 
* Shield Ethernet sans POE
* Shield Grove
* 
*/

#include <Ethernet.h>
#include <EthernetUdp.h>
#include <Wire.h> 
#include <rgb_lcd.h>
#include <SPI.h>
#include <DHT.h>

#define DHTPIN A0
#define DHTTYPE DHT11   // DHT 11
#define PIR_MOTION_SENSOR 2

#define RELAIS_PRISE 6
#define RELAIS_LUMIERE 7
#define RELAIS_CHAUFFAGE 8

DHT dht(DHTPIN, DHTTYPE);

byte mac[]={0x90,0xA2,0xDA,0x0F,0x2C,0x28};
IPAddress ip_shield(192,168,1,205);

EthernetUDP UDP;
IPAddress remote = UDP.remoteIP();
rgb_lcd lcd;
const int colorR = 255;
const int colorG = 0;
const int colorB = 0;

byte etat_lumiere=0;

void setup() {
  // put your setup code here, to run once:
  Ethernet.begin(mac,ip_shield);
  UDP.begin(5500);
  Serial.begin(9600);
  dht.begin();
  
  pinMode( RELAIS_PRISE , OUTPUT); //Prise
  pinMode( RELAIS_LUMIERE , OUTPUT); //Lumiere
  pinMode( RELAIS_CHAUFFAGE , OUTPUT); //Chauffage
  pinMode(PIR_MOTION_SENSOR, INPUT);
  
  digitalWrite(RELAIS_PRISE , HIGH);
  digitalWrite(RELAIS_LUMIERE , HIGH);
  digitalWrite(RELAIS_CHAUFFAGE , HIGH);
  
  lcd.begin(16, 2);
  lcd.setRGB(colorR, colorG, colorB);                      
  lcd.print("Maquette chambre");
  lcd.setCursor(0,1);
  lcd.print("IP:");
  lcd.setCursor(3,1);
  lcd.print(ip_shield);
  delay(3000);
  lcd.clear();
}

void loop() {
  // Protocole UDP
  int Size=UDP.parsePacket();
  char message[6];
  String message2;
  

    
  lcd.setCursor(0,0);
  lcd.print("IP:");
  lcd.setCursor(3,0);
  lcd.print(ip_shield);

  //Variable Capteur temperature et humidité
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  if (isnan(t) || isnan(h)) 
    {
        lcd.setCursor(0,1);
        lcd.print("Pas de capteur DHT");
        
        Serial.println("Pas de capteur DHT"); 
    } 
    else 
    {
        lcd.setCursor(0,1);
        lcd.print("T=");
        lcd.setCursor(2,1);
        lcd.print(t);
        lcd.setCursor(6,1);
        lcd.print((char)223);
        lcd.setCursor(7,1);
        lcd.print("C H=");
        lcd.setCursor(11,1);
        lcd.print(h);
        lcd.setCursor(15,1);
        lcd.print("%");

        Serial.print("Humidity: "); 
        Serial.print(h);
        Serial.print(" %\t");
        Serial.print("Temperature: "); 
        Serial.print(t);
        Serial.println(" *C");
    }

    int sensorValue = digitalRead(PIR_MOTION_SENSOR);
    if(sensorValue == HIGH && etat_lumiere==0)
    {
      digitalWrite(RELAIS_LUMIERE , LOW);
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("Le message est : ");
      lcd.setCursor(0,1);
      lcd.print("Mouvement detecté");
      delay(2000);
      digitalWrite(RELAIS_LUMIERE , HIGH);
      lcd.clear();
     }

  if(Size>0)
  {
    lcd.clear();
    UDP.read(message,6);
    message2=message;
    lcd.setCursor(0,0);
    lcd.print("Le message est : ");
    lcd.setCursor(0,1);
    lcd.print(message);
    delay(10);
    lcd.clear();
    Serial.println(Size);
    Serial.println(message);
    if(message2=="Acquer")
    {
      lcd.setCursor(0,0);
      lcd.print("Le message est : ");
      lcd.setCursor(0,1);
      lcd.print("Capteur actualisé");
      delay(2000);
      lcd.clear();
      //char  ReplyBuffer[] = "Capteur DHT11 : ";
      UDP.beginPacket(UDP.remoteIP(), UDP.remotePort());
      //UDP.write(ReplyBuffer);
      UDP.write("Temperature = ");
      UDP.print(t);
      UDP.write(" C");
      UDP.write("\n");
      UDP.write("Humidite = ");
      UDP.print(h);
      UDP.write(" %");
      UDP.endPacket();
    }
    else
    {
      char  ReplyBuffer[] = "acknowledged";
      UDP.beginPacket(UDP.remoteIP(), UDP.remotePort());
      UDP.write(ReplyBuffer);
      UDP.endPacket();
    }

  }

  /*Liste instruction pour B4A :
   * ELumiere : Eteindre la lumiere
   * ALumiere : Allumer la lumiere
   * AChauffage : Allumer le chauffage
   * EChauffage : Eteindre le chauffage
   * APrise : Allumer les prises
   * EPrise : Eteindre les prises
   */
   
  if(message2=="APrise")
  {
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Allumer Prise : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    delay(2000);
    lcd.clear();
    digitalWrite(RELAIS_PRISE , LOW);
    
    Serial.println("Allumer Prise:good");
  }
  
  if(message2=="EPrise")
  {
    digitalWrite(6 , HIGH);
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Eteindre Prise :");
    lcd.setCursor(0,1);
    lcd.print("good");
    delay(2000);
    lcd.clear();
    digitalWrite(RELAIS_PRISE , HIGH);
    
    Serial.println("Eteindre Prise:good");
  }

  if(message2=="ALumie")
  {
    etat_lumiere=1;
    digitalWrite(RELAIS_LUMIERE , LOW);
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Allumer Eclairage :");
    lcd.setCursor(0,1);
    lcd.print("Good");
    delay(2000);
    lcd.clear();
    
    Serial.println("Allumer Eclairages:good");
  }
  
  if(message2=="ELumie")
  {
    etat_lumiere=0;
    digitalWrite(RELAIS_LUMIERE , HIGH);
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Eteindre Eclairage : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    delay(2000);
    lcd.clear();
    
    Serial.println("Eteindre Eclairages:good");
  }

  if(message2=="AChauf")
  {
    digitalWrite(RELAIS_CHAUFFAGE , LOW);
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Allumer Chauffage : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    delay(2000);
    lcd.clear();
    
    Serial.println("Allumer Chauffage:good");
  }
  
  if(message2=="EChauf")
  {
    digitalWrite(RELAIS_CHAUFFAGE , HIGH);
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Eteindre Chauffage:");
    lcd.setCursor(0,1);
    lcd.print("good");
    delay(2000);
    lcd.clear();
    
    Serial.println("Eteindre Chauffage:good");
  }
  
}
