## OpenBot PlayGround:

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

### OpenBot Robot Info:

- OpenBot Robot configures the following components inside it.
    - Sensors: Voltage divider, Sonar, Bumpers, Speed.
    - Wheel Odometer: Front and Back wheels.
    - Led's: Indicators, Front, Back, status.
    - Motors: Forward and Backward.

### OpenBot PlayGround Categories:

- ### Control:

  OpenBot PlayGround includes customizable blocks that can be used to control the flow of program such as loops, events
  and conditionals. It provides a visual way for users to structure the logic of their programs.

    <img src="../../../../docs/images/playground_blockly_control.jpg" height="50%" width="50%"/>

  A brief overview of some control blocks:
    - Start: Initiates the execution of program and execute the block code single time.
    - Conditionals: ``If`` and ``If-else`` blocks are used to create conditional branches in your code.
        - Wait: Brings pauses or delays in the code execution.
        - Forever: Once the Forever block is embedded in place then it will create an infinite loop which indicates that
          loop will continue indefinitely with each iteration.

- ### Loops:

  Loops category provides various blocks that helps to control the flow of your program through repetition.

     <img src="../../../../docs/images/playground_blockly_loops.jpg" height="50%" width="50%"/>

  Some loop blocks examples are listed here below:

    - Repeat: The ``Repeat`` block enables you to define the number of iterations for a set of blocks to be executed.
    - While: The ``While`` block continues executing a set of blocks as long as a specified condition remains true.

- ### Operators:

  Operators allow you to perform several operations or calculations within your program. The blocks allow you to build
  complex expressions and conditions according to the requirement.

  <img src="../../../../docs/images/playground_operator_blocks.jpg" height="50%" width="50%"/>

  Here are some common types of operators that you might find in OpenBot PlayGround:

    - Arithmetic: Addition, subtraction, multiplication, division, and other arithmetic operations are available in this
      category.
    - Math Operators: Blocks like "Power," "Square Root," and "Random Fraction" are used to perform more advanced
      mathematical computations.


- ### Variables:

  Variables are used for data storage within your blocks and inside variables category blocks allow you to declare, set,
  change and manipulate variables.The concept of variables in OpenBot PlayGround help you to manage and manipulate data
  in your programs.

  <img src="../../../../docs/images/playground_variable_blocks.jpg" height="50%" width="50%"/>

  Have a look on some Variable block examples:

    - Set: Set Variable block is going to assign a value to a variable.
    - Change: It will help you to modify the value of an existing variable.

- ### Lights:

  Lights are another type of category that is provided by OpenBot PlayGround which helps to make the use of indicators
  and can set the values of brightness dynamically.

  <img src="../../../../docs/images/playground_light_blocks.jpg" height="50%" width="50%"/>

  Below are some examples:
    - Indicators: Block used to enable indicators by turning them ON/OFF.
    - Brightness: used to set the brightness of tail and head LED by taking dynamic values.

  NOTE: Keeping the brightness at zero will make the brightness mode OFF and if the brightness is at the highest point
  ie.100 will turn ON the brightness mode.

- ### Controller:

  Certainly! When selecting a mode within the controller block, it will be applied uniformly across all other fragments
  within the OpenBot robot app.

  <img src="../../../../docs/images/playground_controller_blocks.jpg" height="50%" width="50%"/>

  Below are the examples of Controller Block:

    - Switch Controller: It helps you to choose the Controller method by either Gamepad or Phone.
    - Drive Mode: It helps you to switch the Drive Mode by either Joystick or Game or dual.

   <p style="color:yellow ">TIP: If you are selecting Phone as a controller then drive mode automatically sets to dual in robot app irrespective of block chosen drive mode. </p>

- ### Sound:

  Sound Blocks can be utilized to play sound for drive modes and robot static speed.

  <img src="../../../../docs/images/playground_sound_blocks.jpg" height="50%" width="50%"/>

  Let's have some examples:

    - Speed: Helps you to play the sound as slow, medium and fast.
    - Mode: Helps you to play the sound as dual, joystick or game.

- ### Sensors:

  Sensors are the blocks which are going to return different readings for OpenBot condition and environment status .

  <img src="../../../../docs/images/playground_sensors_blocks.jpg" height="50%" width="50%"/>

  Overview:
    - Phone Sensors: Help to measure Gyroscope,Acceleration, and Magnetic readings at different axis(3-Dimensional).
    - Car Sensors: Help to provide the different readings like Sonar, Speed. Also, it will check if bumper gets collide
      with an obstacle.

- ### Movement:

  As the name suggests it is responsible for the movement of Robot at any speed and in any direction and the speed limit
  is 0-255.

  <img src="../../../../docs/images/playground_movement_blocks.jpg" height="50%" width="50%"/>

  Let's have some examples:

    - Set speed: Helps to set the speed as slow, medium and fast.
    - Move: Helps to make the movement in forward or backward and left or right direction at required speed.

  Key Points:
    - if the left speed value is set lower than the right, the robot will move counterclockwise, or vice versa.
    - If you equalize the left and right speeds, it will move straight.
    - Setting a positive value on the left and a negative value on the right will cause the robot to spin.


- ### Artificial Intelligence(AI):

  OpenBot Playground provides another important category named Artificial Intelligence which further configures many
  features such as Object Tracking, Autopilot, Point Goal Navigation.

  <img src="../../../../docs/images/playground_ai_blocks.jpg" height="50%" width="50%"/>

  Lets understand this concept by some examples of blocks:
    - ``Object Tracking``: Its primary function revolves around detecting objects. This AI fragment allows you to pick
      any
      object for tracking. Depending on your phone's performance, you have the flexibility to choose an object detector
      model. By default, this block comes equipped with the "MobileNetV1-300" model. Additionally, you have the option
      to manually add any model of your choice.
    - ``AutoPilot``: This snippet is also available through OpenBot Playground, utilizing data collection, wherein a
      pre-trained
      dataset (ML model CIL-Mobile-Cmd) is already integrated. Subsequently, the camera fragment is displayed on the
      screen,
      initiating the tracking of the captured path.
    - ``Point Goal Navigation``: The primary objective of this block is to reach a designated point through navigation.
      You can
      configure the forward and left values in 3-dimensional view using the navigation models within it. When the
      project is
      executed on a phone, the point navigation fragment will be displayed on the screen with an Augmented Reality (AR)
      view. Subsequently, the robot will initiate movement until it successfully reaches the goal.

   <p style="color: yellow"> TIP: If you've incorporated an external modal, ensure to enable AutoSync in the playground. This feature will assist you in display newly added model in block and verifying the availability and successful download of the modal in robot app.</p>

- ### Advanced Artificial Intelligence(AI):

  The OpenBotPlayground introduces several advancements, featuring an Advanced Artificial Intelligence (AI) that offers
  modular blocks for detection and autopilot functionality

  <img src="../../../../docs/images/playground_advance_ai_blocks.jpg" height="50%" width="50%"/>

  #### Multiple Detection Block:

    - This advanced module is designed for object tracking, accommodating various classes such as a person, car, book,
      traffic light, etc. The identification of the object is carried out by the integrated AI model. The functionality
      of this module is contingent upon the specified conditions.
    - The block is designed to enable multiple object detections, initializing the process for the specified class. Once
      the chosen class is detected, the robot will execute all tasks outlined in the subsequent 'do' statement. If the
      specified class is not detected within the defined number of continuous frames, the robot will proceed to execute
      the tasks specified in the subsequent ``do`` statement. The block can be use multiple times within the playground
      for different classes as well. 

