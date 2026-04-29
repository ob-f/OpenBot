import * as Blockly from "blockly/core";

/**
 * custom field for on/off toggle
 */
export class FieldToggle extends Blockly.Field {
    constructor(state, onText, offText, opt_validator) {
        super('', opt_validator);
        this.state_ = state;
        this.onText_ = onText || "ON";
        this.offText_ = offText || "OFF";
        this.SERIALIZABLE = true;
    }
    init() {
        super.init();
        Blockly.Tooltip.bindMouseEvents(this.fieldGroup_);
        this.setValue(this.state_);
        this.fieldGroup_.classList.add('blocklyFieldToggle');
    }

    showEditor_() {
        this.state_ = !this.state_;
        const text = this.state_ ? this.onText_ : this.offText_;
        super.setValue(text);
        this.updateDisplay_();
        super.showEditor_();
    }

    //set the text value to the selected state
    setValue(newValue) {
        const onText = this.onText_ || "ON";
        const offText = this.offText_ || "OFF";

        if (typeof newValue === "string") {
            const normalized = newValue.trim().toUpperCase();
            this.state_ = normalized === onText.toUpperCase();
        } else {
            this.state_ = !!newValue;
        }
        const text = this.state_ ? onText : offText;
        super.setValue(text);
        this.updateDisplay_();
    }

    //update the value of state after clicking on element
    updateDisplay_() {
        if (this.textElement_) {
            const onText = this.onText_ || "ON";
            const offText = this.offText_ || "OFF";
            this.textElement_.firstChild.nodeValue = this.state_ ? onText : offText;
            const rectElement = this.fieldGroup_?.querySelector('rect');
            if (rectElement) {
                rectElement.classList.toggle('field-toggle-on', this.state_);
                rectElement.classList.toggle('field-toggle-off', !this.state_);
            }
        }
    }

    static fromJson(options) {
        return new FieldToggle(options.state, options.onText, options.offText);
    }

}


//registering the custom field "field_toggle"
Blockly.fieldRegistry.register('field_toggle', FieldToggle);




