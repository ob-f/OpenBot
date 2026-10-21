import pygame


def test_joystick_connection():
    pygame.init()

    j = pygame.joystick.Joystick(0)
    j.init()

    for _ in range(100):
        events = pygame.event.get()


if __name__ == "__main__":
    pygame.init()

    j = pygame.joystick.Joystick(0)
    j.init()

    try:
        while True:
            events = pygame.event.get()
            for event in events:
                if event.type == pygame.JOYBUTTONDOWN:
                    print("Button Pressed")
                    if j.get_button(0):
                        # A Button
                        print("A Button")
                    elif j.get_button(1):
                        # B Button
                        print("B Button")
                    elif j.get_button(2):
                        # X Button
                        print("X Button")
                    elif j.get_button(3):
                        # Y Button
                        print("Y Button")
                    elif j.get_button(4):
                        # Left Bumper
                        print("Left Bumper")
                    elif j.get_button(5):
                        # Right Bumper
                        print("Right Bumper")
                    elif j.get_button(6):
                        # Back Button
                        print("Back Button")
                    elif j.get_button(7):
                        # Start bbutton
                        print("Start Button")
                    elif j.get_button(8):
                        # L Stick in
                        print("L Stick in")
                    elif j.get_button(9):
                        # R Stick in
                        print("R Stick in")
                    elif j.get_button(10):
                        # Guide Button
                        print("Guide Button")
                        j.rumble(0, 0.7, 500)

                elif event.type == pygame.JOYBUTTONUP:
                    print("Button Released")
                else:
                    # print(f"Axis {0} value: {j.get_axis(0):>6.3f}")
                    # print(f"Axis {1} value: {j.get_axis(1):>6.3f}")
                    print(f"Axis {2} value: {j.get_axis(2):>6.3f}")
                    # print(f"Axis {3} value: {j.get_axis(3):>6.3f}")
                    # print(f"Axis {4} value: {j.get_axis(4):>6.3f}")
                    # print(j.get_hat(0))
    except KeyboardInterrupt:
        print("EXITING NOW")
        j.quit()
