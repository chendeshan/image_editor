package com.chen.editor.widget.action;

/**
 * Created by chenjc on 2018/10/18.
 */

public class RotateAction extends Action {
    private int mRotateTimes;

    public RotateAction(int rotateTimes) {
        super(ActionType.ROTATE);
        mRotateTimes = rotateTimes;
    }

    public int getRotateTimes() {
        return mRotateTimes;
    }

    public void setRotateTimes(int rotateTimes) {
        mRotateTimes = rotateTimes;
    }
}
