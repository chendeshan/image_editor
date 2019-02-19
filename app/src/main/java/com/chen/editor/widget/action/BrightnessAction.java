package com.chen.editor.widget.action;

/**
 * Created by chenjc on 2018/10/18.
 */

public class BrightnessAction extends Action {
    private int mDegree;

    public BrightnessAction(int degree) {
        super(ActionType.BRIGHTNESS);
        this.mDegree = degree;
    }

    public int getDegree() {
        return mDegree;
    }

    public void setDegree(int degree) {
        this.mDegree = degree;
    }
}
