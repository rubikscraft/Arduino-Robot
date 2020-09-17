#include <Arduino.h>
#include <freenove-led.h>

#define PIN_DIRECTION_RIGHT 3
#define PIN_DIRECTION_LEFT 4
#define PIN_MOTOR_PWM_RIGHT 5
#define PIN_MOTOR_PWM_LEFT 6

#define MIN_DISTANCE 15

#define I2C_ADDRESS 0x20
#define LEDS_COUNT 10

#define PIN_SONIC_TRIG 7
#define PIN_SONIC_ECHO 8
#define MAX_DISTANCE 100
#define SONIC_TIMEOUT (MAX_DISTANCE * 60)
#define SOUND_VELOCITY 340

Freenove_WS2812B_Controller strip(I2C_ADDRESS, LEDS_COUNT, TYPE_GRB);
float currentDistance = MAX_DISTANCE;

int currentLeftSpeed = 0;
int currentRightSpeed = 0;

void setup()
{
  Serial.begin(9600);
  while (!strip.begin())
    ;

  pinMode(PIN_DIRECTION_LEFT, OUTPUT);
  pinMode(PIN_MOTOR_PWM_LEFT, OUTPUT);
  pinMode(PIN_DIRECTION_RIGHT, OUTPUT);
  pinMode(PIN_MOTOR_PWM_RIGHT, OUTPUT);

  pinMode(PIN_SONIC_TRIG, OUTPUT);
  pinMode(PIN_SONIC_ECHO, INPUT);
}

float getSonar()
{
  unsigned long pingTime;
  float distance;

  digitalWrite(PIN_SONIC_TRIG, HIGH); // make trigPin output high level lasting for 10μs to triger HC_SR04,
  delayMicroseconds(10);
  digitalWrite(PIN_SONIC_TRIG, LOW);

  pingTime = pulseIn(PIN_SONIC_ECHO, HIGH, SONIC_TIMEOUT); // Wait HC-SR04 returning to the high level and measure out this waitting time
  if (pingTime != 0)
    distance = (float)pingTime * SOUND_VELOCITY / 2 / 10000; // calculate the distance according to the time
  else
    distance = MAX_DISTANCE;
  return distance; // return the distance value
}

void motor(int pwmspeed, int motor, bool block = false)
{
  if (pwmspeed > 0 && block)
  {
    pwmspeed = 0;
  }

  bool dir = pwmspeed < 0;
  int motorSpeed = abs(pwmspeed);

  if (motor == 0)
  {
    analogWrite(PIN_MOTOR_PWM_LEFT, motorSpeed);
    digitalWrite(PIN_DIRECTION_LEFT, dir);
  }
  else if (motor == 1)
  {
    analogWrite(PIN_MOTOR_PWM_RIGHT, motorSpeed);
    digitalWrite(PIN_DIRECTION_RIGHT, !dir);
  }
}

int negative(char in)
{
  return in == '-' ? -1 : 1;
}

void readCommand(String command)
{
  char base = command.charAt(0);

  if (base == 'L')
  {
    currentLeftSpeed = command.substring(2).toInt() * negative(command.charAt(1));
  }
  else if (base == 'R')
  {
    currentRightSpeed = command.substring(2).toInt() * negative(command.charAt(1));
  }
  else if (base == 'A')
  {
    int action = String(command.charAt(1)).toInt();
    switch (action)
    {
    default:
      strip.setAllLedsColor(255, 255, 255);
      strip.show();
      delay(100);
      break;
    }
    // Do actions
  }
}

void loop()
{
  if (Serial.available())
  {
    String command = Serial.readStringUntil('\n');

    readCommand(command);
  }

  currentDistance = getSonar();

  bool block = currentDistance < MIN_DISTANCE;

  motor(currentLeftSpeed, 0, block);
  motor(currentRightSpeed, 1, block);

  if (block)
    strip.setAllLedsColor(10, 0, 0);
  else
    strip.setAllLedsColor(0, 10, 0);

  strip.show();
}