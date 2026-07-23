import * as Blockly from "blockly/core";
import {colors} from "./color";
import {toast} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import lightCarousal1 from "../assets/images/drag_drop_white.gif";
import darkCarousal1 from "../assets/images/drag_drop_dark.gif";
import lightCarousal2 from "../assets/images/scan_qr_code_light.gif";
import darkCarousal2 from "../assets/images/scan_qr_code_dark.gif";
import lightCarousal3 from "../assets/images/openBot_car_light.gif";
import darkCarousal3 from "../assets/images/openBot_car_dark.gif";

/**
 * Blockly control and loop blocks
 * @type {string[]}
 */
export const controlBlocksType = ["controls_if", "controls_ifelse", "logic_compare", "logic_operation", "logic_negate", "logic_boolean", "logic_null"];
export const loopBlocksType = ["controls_whileUntil", "controls_repeat", "controls_flow_statements"];

/**
 * function for dark theme in blockly workspace
 * @type {Theme}
 */
export const DarkTheme = Blockly.Theme.defineTheme('dark', {
    'base': Blockly.Themes.Classic,
    'blockStyles': {
        'logic_blocks': {
            'colourPrimary': "#4860b7",
            'colourSecondary': "#4860b7",
            'colourTertiary': "#2b3c7b"
        },
        'loop_blocks': {
            'colourPrimary': "#b75439",
            'colourSecondary': "#ff0000",
            'colourTertiary': "#8c4531"
        },
        "text_blocks": {
            'colourPrimary': "#c169c9",
            'colourSecondary': "#ff0000",
            'colourTertiary': "#a65aad"
        },
        'variable_blocks': {
            'colourPrimary': "#c169c9",
            'colourSecondary': "#ff0000",
            'colourTertiary': "#a65aad"
        },
        'math_blocks': {
            'colourPrimary': "#8b6aee",
            'colourSecondary': "#ff0000",
            'colourTertiary': "#5f46ab"
        },
    },
    'componentStyles': {
        'workspaceBackgroundColour': colors.blocklyBackground,
        'toolboxBackgroundColour': colors.blocklyBackground,
        'toolboxForegroundColour': colors.whiteFont,
        'flyoutBackgroundColour': '#686868',
    },
});

/**
 * function for light theme in blockly workspace
 * @type {Theme}
 */
export const LightTheme = Blockly.Theme.defineTheme('light', {
    'base': Blockly.Themes.Classic,
    'blockStyles': {
        'logic_blocks': {
            'colourPrimary': "#4860b7",
            'colourSecondary': "#4860b7",
            'colourTertiary': "#2b3c7b"
        },
        'loop_blocks': {
            'colourPrimary': "#b75439",
            'colourSecondary': "#ff0000",
            'colourTertiary': "#8c4531"
        },
        "text_blocks": {
            'colourPrimary': "#c169c9",
            'colourSecondary': "#ff0000",
            'colourTertiary': "#a65aad"
        },
        'variable_blocks': {
            'colourPrimary': "#c169c9",
            'colourSecondary': "#ff0000",
            'colourTertiary': "#a65aad"
        },
        'math_blocks': {
            'colourPrimary': "#8b6aee",
            'colourSecondary': "#ff0000",
            'colourTertiary': "#5f46ab"
        },
    },
    'componentStyles': {
        'workspaceBackgroundColour': colors.whiteBackground,
        'toolboxBackgroundColour': colors.whiteBackground,
        'toolboxForegroundColour': colors.blackFont,
        'flyoutBackgroundColour': '#686868',
    },
});

/**
 * help centre content
 * @type {{connectAndDriveFirstLine: string, dragAndDropSecondLine: string, saveAndDownloadSecondLine: string, dragAndDropFirstLine: string, connectAndDriveSecondLine: string, saveAndDownloadFirstLine: string}}
 */
export const HelpCenterText = {
    dragAndDropFirstLine: "Drag and drop the selected block of code into the input field.",
    dragAndDropSecondLine: "Repeat the steps of selecting and placing all required code blocks, and then connecting them together to create a sequence of actions.",

    saveAndDownloadFirstLine: "Upload the code on your google drive and generate the QR code.\n",
    saveAndDownloadSecondLine: "Use the OpenBot application to scan the QR code and successfully save the block code on your device.",

    connectAndDriveFirstLine: "Pair your smartphone with the OpenBot car and run the code that you have downloaded.",
    connectAndDriveSecondLine: "Carry out desired actions such as activating indicator lights, detecting objects, and more on your robot car.",
}

/**
 * dark and light theme constants
 * @type {{light: string, dark: string}}
 */
export const Themes = {
    "light": "light",
    "dark": "dark",
}

/**
 * constants for local storage items
 * @type {{configData: string, currentProject: string, accessToken: string, allProjects: string}}
 */
export const localStorageKeys = {
    allProjects: "projects",
    currentProject: "currentProject",
    accessToken: "accessToken",
    configData: "configData",
    theme: "theme"
}

/**
 * general constants
 * @type {{qr: string, tflite: string, endCode: string, js: string, py: string, ProfileSuccessMsg: string, FolderName: string, CookieMsg: string, baseUrl: string, InternetOffMsg: string, xml: string, json: string, LoadingTime: number}}
 */
export const Constants = {
    LoadingTime: 800,
    ProfileSuccessMsg: "Profile updated successfully!",
    CookieMsg: "Our website use cookies. By continuing navigating, we assume your permission to deploy cookies as detailed in our Privacy Policy.",
    FolderName: "openBot-Playground",
    baseUrl: "https://www.googleapis.com/drive/v3",
    endCode: "\nstart();\nforever();\n",
    js: "js",
    py: "py",
    qr: "qr",
    xml: "xml",
    json: "json",
    tflite: "tflite",
    InternetOffMsg: "Please check your internet connection.",
    subscription: "Please subscribe to upload projects",
    projects: "projects",
    models: "models",
    theme: "theme",
    subscriptionEnded:"Your trial has just ended!",
    subscriptionContinueService:"To continue using OpenBot Playground, you",
    subscriptionContinueInfo:"will need to upgrade your plan.",
    subscribeButton:"Subscribe Now"
}

export const Month = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];


/**
 * errors for openBot playground
 * @type {{error1: string, error3: string, error2: string}}
 */
export const Errors = {
    error1: "No start or forever block present in the playground.",
    error2: "Detected adjacent AI blocks. Please review the Start block configuration. When using adjacent AI blocks,insert a 'disable AI' block between them.",
    error3: "Identical objects for multiple detection AI block.",
    error4: "AI block present within the forever block. Please review the block code.",
    error5: "Similar classes present in the Advanvced AI blocks"
}

export const PlaygroundConstants = {
    start: "start",
    forever: "forever",
    detectionOrUndetection: "detectionOrUndetection",
    multipleObjectTracking: "multipleObjectTracking",
    variableDetection: "variableDetection",
    labels: "labels",
    disableAI: "disableAI",
    object_1: "object_1",
    object_2: "object_2",
    labels1: "labels1",
    labels2: "labels2"
}
/**
 * AI models
 * @type {{type: string[]}}
 */
export const Models = {
    type: [
        'AUTOPILOT',
        'DETECTOR',
        'NAVIGATION',
        'CMDNAV',
    ],
}

/**
 * Application paths
 * @type {{playGround: string, home: string}}
 */
export const PathName = {
    "home": "/",
    "playGround": "/playground",
}

/**
 * Static labels for AI blocks
 * @type {string[]}
 */
export const Labels = [
    "person",
    "bicycle",
    "car",
    "motorcycle",
    "airplane",
    "bus",
    "train",
    "truck",
    "truck",
    "boat",
    "traffic light",
    "fire hydrant",
    "stop sign",
    "parking meter",
    "bench",
    "bird",
    "cat",
    "dog",
    "horse",
    "sheep",
    "cow",
    "elephant",
    "bear",
    "zebra",
    "giraffe",
    "backpack",
    "umbrella",
    "handbag",
    "tie",
    "suitcase",
    "frisbee",
    "skis",
    "snowboard",
    "sports ball",
    "kite",
    "baseball bat",
    "baseball glove",
    "skateboard",
    "surfboard",
    "tennis racket",
    "bottle",
    "wine glass",
    "cup",
    "fork",
    "knife",
    "spoon",
    "bowl",
    "banana",
    "apple",
    "sandwich",
    "orange",
    "broccoli",
    "carrot",
    "hot dog",
    "pizza",
    "donut",
    "cake",
    "chair",
    "couch",
    "potted plant",
    "bed",
    "dining table",
    "toilet",
    "tv",
    "laptop",
    "mouse",
    "remote",
    "keyboard",
    "cell phone",
    "microwave",
    "oven",
    "toaster",
    "sink",
    "refrigerator",
    "book",
    "clock",
    "vase",
    "scissors",
    "teddy bear",
    "hair drier",
    "toothbrush",
]

/**
 * carousal content and text
 * @type {[{carousalLine1: string, carousalLine2: string, videoDark: *, carousalHeader: string, videoLight: *},{carousalLine1: string, carousalLine2: string, videoDark: *, carousalHeader: string, videoLight: *},{carousalLine1: string, carousalLine2: string, videoDark: *, carousalHeader: string, videoLight: *}]}
 */
export const Carousal = [
    {
        carousalHeader: "Drag and Drop",
        carousalLine1: "Drag and drop the selected block of code into the input field.",
        carousalLine2: "Repeat the steps of selecting and placing all required code blocks, and then connecting them together to create a sequence of actions.",
        videoLight: lightCarousal1,
        videoDark: darkCarousal1,
    },
    {
        carousalHeader: "Save and Download",
        carousalLine1: "Check for errors by compiling the code and, upon successful compilation, generate the QR code.",
        carousalLine2: "Use the OpenBot android application to scan the QR code and successfully save the block code on your device.",
        videoLight: lightCarousal2,
        videoDark: darkCarousal2,
    },
    {
        carousalHeader: "Connect and Drive",
        carousalLine1: "Pair your smartphone with the OpenBot car and run the code that you have downloaded.",
        carousalLine2: "Carry out desired actions such as activating indicator lights, detecting objects, and more on your robot car.",
        videoLight: lightCarousal3,
        videoDark: darkCarousal3,
    }
]

/**
 * error Toast
 * @param message
 */
export const errorToast = (message) => {
    toast.error(message, {
        position: 'top-center',
        autoClose: 5000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: false,
        draggable: true,
        progress: undefined,
        theme: 'colored',
        pauseOnFocusLoss: false,
    })
}

/**
 * AI block types
 * @type {string[]}
 */
export const aiBlocks = ["objectTracking", "autopilot", "multipleObjectTracking", "navigateForwardAndLeft", "multipleAIDetection"];

/**
user usage tables
 */
export const tables = {
    users : "users",
    projects: "projects",
    models:"models",
    userUsage:"userUsage"
}