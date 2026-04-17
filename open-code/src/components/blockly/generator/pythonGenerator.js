import {pythonGenerator} from "blockly/python";

/**
 * code generation of blocks in python
 * @returns {string}
 */
pythonGenerator.forBlock['soundType'] = function (block) {
    let dropdown_type = block.getFieldValue('type');
    let code = '';
    code += "playSoundSpeed('" + dropdown_type + "')\n";
    return code;
};

pythonGenerator.forBlock['soundMode'] = function (block) {
    let dropdown_mode_type = block.getFieldValue('mode_type');
    let code = '';
    code += "playSoundMode('" + dropdown_mode_type + "')\n";
    return code;
};


pythonGenerator.forBlock['forward&BackwardAtSpeed'] = function (block) {
    let dropdown_direction_type = block.getFieldValue('direction_type');
    let number_specified_amount = block.getFieldValue('slider');
    let code = '';
    code += dropdown_direction_type + "(" + number_specified_amount + ")\n";
    return code;
};


pythonGenerator.forBlock['left&RightAtSpeed'] = function (block) {
    let dropdown_direction_type = block.getFieldValue('direction_type');
    let number_specified_amount = block.getFieldValue('slider');
    let code = '';
    code += dropdown_direction_type + "(" + number_specified_amount + ")\n";
    return code;
};

pythonGenerator.forBlock['moveLeft&Right'] = function (block) {
    let number_left_distance = block.getFieldValue('left_distance');
    let number_right_distance = block.getFieldValue('right_distance');
    let code = '';
    code += "moveOpenBot(" + number_left_distance + "," + number_right_distance + ")\n";
    return code;
};

pythonGenerator.forBlock['movementStop'] = function () {
    let code = '';
    code += "stopRobot()\n"
    return code;
};

pythonGenerator.forBlock['sonarReading'] = function () {
    let code = '';
    code += "sonarReading()\n";
    return code;
};

pythonGenerator.forBlock['speedReading'] = function () {
    let code = '';
    code += "speedReading()";
    return [code, pythonGenerator.ORDER_NONE];
};

pythonGenerator.forBlock['wheelOdometerSensors'] = function (block) {
    let dropdown_wheel_sensors = block.getFieldValue('wheel_sensors');
    let code = '';
    code += dropdown_wheel_sensors + "()";
    return [code, pythonGenerator.ORDER_NONE];
};


pythonGenerator.forBlock['voltageDividerReading'] = function () {
    let code = '';
    code += "voltageDividerReading()";
    return [code, pythonGenerator.ORDER_NONE];
};

pythonGenerator.forBlock['start'] = function (block) {
    const statements_start_blocks = pythonGenerator.statementToCode(block, 'start_blocks');
    let code = "";
    code += "def start():\n" + statements_start_blocks;
    return code;
};

pythonGenerator.forBlock['forever'] = function (block) {
    const statements_start_blocks = pythonGenerator.statementToCode(block, 'forever_loop_blocks');
    let code = "";
    code += "def forever():\n while(true):\n" + statements_start_blocks + "\n";
    return code;
};

pythonGenerator.forBlock['gyroscope_reading'] = function (block) {
    let code = '';
    let dropdown_type = block.getFieldValue('axis');

    function scaleType() {
        switch (dropdown_type) {
            case "x":
                return "X";
            case "y":
                return "Y";
            case "z":
                return "Z";
            default:
        }
    }

    code += "gyroscopeReading" + scaleType() + "()";
    return [code, pythonGenerator.ORDER_NONE];
};

pythonGenerator.forBlock['acceleration_reading'] = function (block) {
    let code = "";
    let dropdown_type = block.getFieldValue('axis');

    function scaleType() {
        switch (dropdown_type) {
            case "x":
                return "X";
            case "y":
                return "Y";
            case "z":
                return "Z";
            default:
        }
    }

    code += "accelerationReading" + scaleType() + "()";
    return [code, pythonGenerator.ORDER_NONE];
};

pythonGenerator.forBlock['magnetic_reading'] = function (block) {
    let code = "";
    let dropdown_type = block.getFieldValue('axis');

    function scaleType() {
        switch (dropdown_type) {
            case "x":
                return "X";
            case "y":
                return "Y";
            case "z":
                return "Z";
            default:
        }
    }

    code += "magneticReading" + scaleType() + "()";
    return [code, pythonGenerator.ORDER_NONE];
};

pythonGenerator.forBlock['speedControl'] = function (block) {
    let dropdown_type = block.getFieldValue('type');
    let code = '';
    code += "setSpeed(" + dropdown_type + ")\n";
    return code;
};


pythonGenerator.forBlock['controllerMode'] = function (block) {
    let dropdown_controller = block.getFieldValue('controller');
    let code = '';
    code += "switchController(" + dropdown_controller + ")\n";
    return code;
};


pythonGenerator.forBlock['driveModeControls'] = function (block) {
    let dropdown_driveModeControls = block.getFieldValue('controller');
    let code = '';
    code += "switchDriveMode(" + dropdown_driveModeControls + ")\n";
    return code;
};

pythonGenerator.forBlock['brightness'] = function (block) {
    let value = block.getFieldValue('slider');
    let code = "";
    code += "ledBrightness(" + value + ")\n";
    return code
};

pythonGenerator.forBlock['wait'] = function (block) {
    let value = block.getFieldValue('time');
    let code = "";
    code += "pause(" + value + ")\n";
    return code
};

pythonGenerator.forBlock['sonarReading'] = function () {
    let code = "";
    code += "sonarReading() ";
    return [code, pythonGenerator.ORDER_NONE];
};

pythonGenerator.forBlock['indicators'] = function (block) {
    const dropdown_side = block.getFieldValue('side');
    const toggleState = block.getFieldValue('TOGGLE_STATE');

    function indicatorStatus() {
        if (toggleState === "ON") {
            return "On";
        } else if (toggleState === "OFF") {
            return "Off";
        }
    }

    let code = "";
    code += dropdown_side + "Indicator" + indicatorStatus() + "()\n";
    return code;
};

pythonGenerator.forBlock['brightnessHighOrLow'] = function (block) {
    const toggleState = block.getFieldValue('TOGGLE_STATE');

    function indicatorStatus() {
        if (toggleState === "ON") {
            return "'ON'";
        } else if (toggleState === "OFF") {
            return "'OFF'";
        }
    }

    let code = "";
    code += "toggleLed(" + indicatorStatus() + ")\n";
    return code;
};

pythonGenerator.forBlock['autopilot'] = function (block) {
    let dropdown_autopilot_models = block.getFieldValue('autopilot models');
    let code = '';
    code += "enableAutopilot('" + dropdown_autopilot_models + "')\n"
    return code;
};

pythonGenerator.forBlock['navigateForwardAndLeft'] = function (block) {
    let forward_position = block.getFieldValue('forward');
    let left_position = block.getFieldValue('left');
    let dropdown_navigation_models = block.getFieldValue('navigation_models');
    let code = '';
    code += "reachGoal(" + forward_position + "," + left_position + ",'" + dropdown_navigation_models + "')\n";
    return code;
};

pythonGenerator.forBlock['objectTracking'] = function (block) {
    const dropdown_class = block.getFieldValue('class');
    const dropdown_models = block.getFieldValue('models');
    let code = "";
    code += "follow('" + dropdown_class + "','" + dropdown_models + "')\n";
    return code;
};

pythonGenerator.forBlock['disableAI'] = function () {
    let code = '';
    code += "disableAI();\n";
    return code;
};

pythonGenerator.forBlock['multipleAIDetection'] = function (block) {
    let autopilot_models = block.getFieldValue('autopilot_models');
    let labels = block.getFieldValue('labels');
    let objectTracking_models = block.getFieldValue('objectTracking_models');
    let tasks = pythonGenerator.statementToCode(block, 'tasks');
    let code = "";
    code += "enableMultipleAI('" + autopilot_models + "','" + tasks + "','" + labels + "','" + objectTracking_models + "')\n"
    return code;
};

pythonGenerator.forBlock['multipleObjectTracking'] = function (block) {
    let labels1 = block.getFieldValue('labels1');
    let models = block.getFieldValue('models');
    let labels2 = block.getFieldValue('labels2');
    let tasks = pythonGenerator.statementToCode(block, 'tasks');
    let code = "";
    code += "enableMultipleDetection('" + labels1 + "','" + models + "','" + labels2 + "','" + tasks + "');\n";
    return code;
};

pythonGenerator.forBlock['variableDetection'] = function (block, generator) {
    let labels = block.getFieldValue('labels');
    let models = block.getFieldValue('models');
    let detect_tasks = pythonGenerator.statementToCode(block, 'detect_tasks');
    let frames = block.getFieldValue("frames");
    let framesLost_tasks = pythonGenerator.statementToCode(block, 'framesLost_tasks');
    let code = "";
    code += "onDetect" + '("' + labels + '","' + models + '","' + detect_tasks + '")\n' + 'onLostFrames("' + labels + '",' + frames + ',"' + framesLost_tasks + '")\n';
    return code;
};

pythonGenerator.forBlock['inputSound'] = function (block, generator) {
    let text = block.getFieldValue('text');
    let code = "";
    code += "playSound('" + text + "')\n";
    return code;
};

pythonGenerator.forBlock['display_sensors'] = function (block, generator) {
    let text = pythonGenerator.blockToCode(block.getInputTargetBlock('value'), generator);
    let code = "";
    if (text[0] === undefined) {
        code += "displaySensorData('')\n";
    } else {
        code += "displaySensorData('" + text[0] + "')\n";
    }
    return code;
};

pythonGenerator.forBlock['display_string'] = function (block, generator) {
    let text = block.getFieldValue('text');
    let code = "";
    code += "displayString('" + text + "')\n";
    return code;
};