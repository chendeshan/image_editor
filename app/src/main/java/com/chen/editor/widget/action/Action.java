package com.chen.editor.widget.action;

/**
 * Created by chenjc on 2018/10/17.
 */

public abstract class Action implements Cloneable {
    public enum ActionType {
        DEFAULT,
        ROTATE,
        DRAW_LINE,
        REVERSE_X,
        REVERSE_Y,
        CONTRAST,
        BRIGHTNESS,
    }

    private ActionType mCurrentActionType;

    public Action(ActionType actionType) {
        mCurrentActionType = actionType;
    }


    public ActionType getActionType() {
        return mCurrentActionType;
    }
}
