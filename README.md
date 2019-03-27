# Chambre-arduin-b4a
Mini projet d'une chambre commandée à dsitance sous Arduino et b4a

## Composants
| Arduino | SeeedStudio | B4A | Android |
| :-----: | :------: | :-----: | :---------: |
| ![](/icone/Arduino.png) | ![](/icone/Seeed_Studio.png) | ![](/icone/B4A.png) | ![](/icone/Android.png) |

### Arduino
| Arduino Mega  | Arduino Uno|
| :-------------: | :-------------: |
| ![](/composants/Arduino%20Mega.jpg) | ![](/composants/Arduino%20Uno.jpg) |


### Shield
| Shield Ethernet | Shield Grove base | 
| :-------------: | :-------------: |
| ![](/composants/Shield_Arduino_Ethernet.jpg) | ![](/composants/SeeedStudio/Shield_Grove_Base.png)  |
 
### SeeedStudio
| DHT11 | LDR | PIR Motion (option) |
| :-------------: | :-------------: | :-------------: |
| ![](/composants/SeeedStudio/Grove_DHT11.jpg) | ![](/composants/SeeedStudio/Grove_light.jpg) | ![](/composants/SeeedStudio/Grove_PIR_Motion_Sensor.jpg) |

| LCD RGB | BME280 |Cable x5| 
| :-------------: | :-------------: | :-------------: |
| ![](/composants/SeeedStudio/Grove_LCD_RGB_Backlight.jpg) | ![](/composants/SeeedStudio/Grove_BME280.jpg) | ![](/composants/SeeedStudio/Grove_Cable.jpg) |

### Divers
| Module 8 Relais |
| :-------------: |
| ![](/composants/Divers/8_Relais.jpg) |

## Branchement
| Fritzing | Circuits.io |
| :-------------: | :-------------: |
| ![](/icone/Fritzing.png) | ![](/icone/Circuits.io.png) |

![](/fritzing/chambre.png)

### Shield
* Shield Grove
* Shield Ethernet sans POE

### Analogique
* A0: Grove Capteur Temperature & Humidité
* A1: Grove Luminosite LDR
* A2: NC
* A3: NC
* A4: NC
* A5: NC

### Digital
* D0 : RX rs232 non utilise
* D1 : TX rs232 non utilise
* D2 : NC
* D3 : NC
* D4 : Digital Output VR monter
* D5 : Digital Output VR descendre
* D6 : Digital Output Prise
* D7 : Digital Output Plafonnier
* D8 : Digital Output Radiateur
* D9 : NC
* D10 : SS Shield-Ethernet-Arduino
* D11 : MOSI Shield-Ethernet-Arduino
* D12 : MISO Shield-Ethernet-Arduino
* D13 : SCK Shield-Ethernet-Arduino

### I2C
* Grove Afficheur LCD RGB
* Grove Barometre

### Alimentation
* Vin : NC
* GND : NC
* 5V : NC
* 3.3V : NC
* Vref : NC

### Divers 
* Ioref : NC
* Reset : NC
