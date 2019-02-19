package com.chen.editor.widget.action;

/**
 * Created by chenjc on 2018/10/18.
 */

public class ReverseY extends Action {
    private int mReverseYTimes;

    public ReverseY(int reverseYTimes) {
        super(ActionType.REVERSE_Y);
        mReverseYTimes = reverseYTimes;
    }

    public int getReverseYTimes() {
        return mReverseYTimes;
    }

    public void setReverseYTimes(int reverseYTimes) {
        mReverseYTimes = reverseYTimes;
    }
}
