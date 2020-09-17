#include <Arduino.h>
#include <freenove-led.h>

#define PIN_DIRECTION_RIGHT 3
#define PIN_DIRECTION_LEFT 4
#define PIN_MOTOR_PWM_RIGHT 5
#define PIN_MOTOR_PWM_LEFT 6

#define I2C_ADDRESS 0x20
#define LEDS_COUNT 10

Freenove_WS2812B_Controller strip(I2C_ADDRESS, LEDS_COUNT, TYPE_GRB);

void setup()

{
  Serial.begin(9600);
  while (!strip.begin())
    ;

  pinMode(PIN_DIRECTION_LEFT, OUTPUT);
  pinMode(PIN_MOTOR_PWM_LEFT, OUTPUT);
  pinMode(PIN_DIRECTION_RIGHT, OUTPUT);
  pinMode(PIN_MOTOR_PWM_RIGHT, OUTPUT);

  strip.setAllLedsColor(255, 255, 255);
  strip.show();
}

void motor(int pwmspeed, int motor)
{
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

  int leftspeed = 0;
  int rightspeed = 0;

  if (base == 'L')
  {
    leftspeed = command.substring(2).toInt() * negative(command.charAt(1));
  }
  else if (base == 'R')
  {
    rightspeed = command.substring(2).toInt() * negative(command.charAt(1));
  }
  else if (base == 'A')
  {
    int action = String(command.charAt(1)).toInt();
    // Do actions
  }

  motor(leftspeed, 0);
  motor(rightspeed, 1);
}

void loop()
{
  String command = Serial.readStringUntil('\n');
  readCommand(command);
}