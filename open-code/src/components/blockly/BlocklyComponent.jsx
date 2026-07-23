import './BlocklyComponent.css';
import React, {useCallback, useContext, useEffect, useRef} from 'react';
import * as Blockly from 'blockly/core';
import * as locale from 'blockly/msg/en';
import 'blockly/blocks';
import {ThemeContext} from "../../App";
import {aiBlocks, Constants, DarkTheme, errorToast, LightTheme, PathName} from "../../utils/constants";
import {Modal} from "@blockly/plugin-modal";
import {StoreContext} from "../../context/context";
import {getCurrentProject, handleChildBlockInWorkspace, updateCurrentProject} from "../../services/workspace";
import {useTheme} from "@mui/material";
import useMediaQuery from "@mui/material/useMediaQuery";
import {checkFileExistsInFolder, getFolderId, getShareableLink} from "../../services/googleDrive";
import {RightDrawer} from "../drawer/drawer";
import {useLocation} from "react-router-dom";

Blockly.setLocale(locale);

/**
 * Blockly workspace component
 *
 * @param {string} props.initialXml - initial XML code to load into the workspace
 * @param {Function} props.onWorkspaceChange - function to call when workspace changes
 * @param {Object} props.rest - additional props to pass to Blockly inject method
 * @returns {JSX.Element}
 */
function BlocklyComponent(props) {
    const {initialXml, children, onWorkspaceChange, ...rest} = props;

    // Refs for the workspace, toolbox, and blockly div
    const blocklyDiv = useRef();
    const toolbox = useRef();
    const primaryWorkspace = useRef();

    // Get context values from the store
    const {theme} = useContext(ThemeContext);
    const {
        projectName,
        currentProjectXml,
        setDrawer,
        setWorkspace,
        setIsError,
        setCurrentProjectXml,
        isOnline,
        setCode,
        setCategory,
        category,
        isAutoSyncEnabled,
        setIsAutoSyncEnabled,
    } = useContext(StoreContext);
    const themes = useTheme();
    const isMobile = useMediaQuery(themes.breakpoints.down('sm'));
    const location = useLocation();

    // Save workspace code to local storage when workspace changes
    const handleWorkspaceChange = useCallback(() => {
        if (category === Constants.xml) {
            setDrawer(false);
        }
        setIsError(false);
        if (projectName !== undefined) {
            setCurrentProjectXml(Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(Blockly.getMainWorkspace())));
            updateCurrentProject(projectName, Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(Blockly.getMainWorkspace())));
        }

    }, []);

    /**
     * handling duplicate for start and forever
     * @type {(function(*): void)|*}
     */
    const handleDuplicateBlocks = useCallback((event) => {
        const blockTypes = ["start", "forever", "onBumper"];
        blockTypes.forEach((blockType) => {
            handlingBlocks(event, blockType);
        });
    }, []);

   
    const DISABLED_REASON_ID = "only-one-active-block";

    const setBlockEnabled = (block, enabled) => {
        if (!block) return;
        if (typeof block.setDisabledReason !== "function") return;
        try {
            block.setDisabledReason(!enabled, DISABLED_REASON_ID);
        } catch (e) {
            // Fail softly: keep workspace usable even if an unexpected block
            // object is encountered.
            console.warn("Failed to toggle Blockly block enabled state", e);
        }
    };

    const handlingBlocks = (event, blockType) => {
        let existingBlocks = primaryWorkspace.current.getBlocksByType(blockType);
        if (event.type === Blockly.Events.CREATE && event.blockId) {
            let block = primaryWorkspace.current.getBlockById(event.blockId);
            if (block.type === blockType) {
                disableChildBlocks(existingBlocks);
            }
        } else if (event.type === Blockly.Events.DELETE && event.blockId) {
            if (existingBlocks.length > 0) {
                setBlockEnabled(existingBlocks[0], true);
                enableAllChildBlocks(existingBlocks[0]);
            }
        }
    };

    //function to disable all  child blocks
    const disableChildBlocks = (blocks) => {
        if (blocks.length > 1) {
            for (let i = 1; i < blocks.length; i++) {
                setBlockEnabled(blocks[i], false);
            }
        }
    };

    //function to enable all child blocks
    const enableAllChildBlocks = (block) => {
        if (!block) return;
        setBlockEnabled(block, true);
        const children = typeof block.getChildren === "function" ? block.getChildren(false) : [];
            for (let i = 0; i < children.length; i++) {
                enableAllChildBlocks(children[i]);
            }
    };

    /**
     * function to disable forever block if start block contains AI block
     * @type {(function(): void)|*}
     */
    const handleForeverBlocks = useCallback(() => {
        let childArray = []
        const start = primaryWorkspace.current.getBlocksByType("start")
        const forever = primaryWorkspace.current.getBlocksByType("forever")
        let startBlock = start[0]?.childBlocks_
        let filteredAIBlocks = []
        if (start?.length > 0 && forever.length > 0) {
            let handleEvents = handleChildBlockInWorkspace(startBlock, childArray)
            handleEvents?.forEach((item) => {
                if (aiBlocks.includes(item)) {
                    filteredAIBlocks.push(item)
                }
            })
            if (filteredAIBlocks?.length > 0) {
                for (let i = 0; i < forever.length; i++) {
                    setBlockEnabled(forever[i], false);
                }
            } else {
                setBlockEnabled(forever[0], true);
                enableAllChildBlocks(forever[0]);
            }
        }
    }, [])

    //function to check qr code availability and display drive link
    const checkQRCode = async () => {
        if (localStorage.getItem("isSigIn") === "true") {
            if (isOnline) {
                let folderId = await getFolderId();
                if (folderId) {
                    let fileExistWithFileID = await checkFileExistsInFolder(folderId, getCurrentProject().projectName, 'js')
                    if (fileExistWithFileID.exists) {
                        let QrLink = await getShareableLink(fileExistWithFileID.fileId, folderId)
                        let linkCode = {
                            driveLink: QrLink,
                            projectName: getCurrentProject().projectName
                        }
                        category === "" && setCategory(Constants.qr);
                        setCode(linkCode);
                    }
                }
            } else {
                errorToast("Please Check your internet connection.")
            }
        } else {
            // setCategory(category === Constants.py ? Constants.py : Constants.js);
        }
    }

    useEffect(() => {
        setCategory(Constants.js);
        checkQRCode().catch(err => {
            console.log(err);
        });
    }, [])

    //updating auto sync state to render blockly component
    useEffect(() => {
        setIsAutoSyncEnabled(false);
    }, [isAutoSyncEnabled])

    useEffect(() => {
        // Create the primary workspace instance
        primaryWorkspace.current = Blockly.inject(blocklyDiv.current, {
            theme: theme === "dark" ? DarkTheme : LightTheme,
            renderer: "zelos",
            toolbox: toolbox.current,
            zoom:
                {
                    controls: false,
                    wheel: true,
                    startScale: 1.0,
                    maxScale: 3,
                    minScale: 0.2,
                    scaleSpeed: 1.5,
                    pinch: true
                },
            ...rest,
        });

        // Set the zoom level for mobile devices
        //decreased size of blocks in mobile view
        const zoomLevel = 0.7;
        if (isMobile) {
            primaryWorkspace.current.setScale(zoomLevel);
        }

        //storing current workspace behaviour and features
        setWorkspace(primaryWorkspace.current)

        // Create and render a new modal instance
        const model = new Modal(primaryWorkspace.current);
        model.init();
        model.render({
            shouldCloseOnOverlayClick: true,
            shouldCloseOnEsc: true,
        });

        // Add change listener to the workspace
        primaryWorkspace.current.addChangeListener(handleWorkspaceChange);

        //blocks who are not attach to start or forever gets disabled
        primaryWorkspace.current.addChangeListener(Blockly.Events.disableOrphans);

        //handle start and forever duplicate blocks occurrence
        primaryWorkspace.current.addChangeListener(handleDuplicateBlocks);

        //handle forever block disability if start block contains AI block
        primaryWorkspace.current.addChangeListener(handleForeverBlocks);

        // Load XML code into the workspace if it exists, otherwise load initial XML code
        if (currentProjectXml) {
            Blockly.Xml.domToWorkspace(Blockly.utils.xml.textToDom(currentProjectXml), primaryWorkspace.current);
            Blockly.getMainWorkspace().clearUndo() //to clear undoStack on loading blocks
        } else {
            Blockly.Xml.domToWorkspace(Blockly.utils.xml.textToDom(initialXml), primaryWorkspace.current);
            Blockly.getMainWorkspace().clearUndo() //to clear undoStack on loading blocks
        }

        // Clean up the workspace when the component unmounts
        return () => {
            primaryWorkspace.current.dispose();
        }
    }, [theme, toolbox, blocklyDiv, props, isAutoSyncEnabled]);

    // Return the blockly div and hidden toolbox
    return (
        <React.Fragment>
            <div ref={blocklyDiv}
                 id="blocklyDiv"
                 style={{
                     width: "100%",
                     backgroundColor: theme === "dark" ? "#202020" : "#FFFFFF",
                 }}>
                {location.pathname === PathName.playGround ? <RightDrawer/> : ""}
            </div>
            <div style={{display: "none"}} ref={toolbox}>
                {children}
            </div>
        </React.Fragment>
    );
}

export default BlocklyComponent;
