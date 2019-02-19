package com.chen.editor.widget.action;


import static com.chen.editor.widget.action.Action.ActionType.CONTRAST;

/**
 * Created by chenjc on 2018/10/18.
 */

public class ContrastAction extends Action {
    private float mDegree;

    public ContrastAction(float degree) {
        super(CONTRAST);
        mDegree = degree;
    }

    public float getDegree() {
        return mDegree;
    }

    public void setDegree(float degree) {
        mDegree = degree;
    }
}
