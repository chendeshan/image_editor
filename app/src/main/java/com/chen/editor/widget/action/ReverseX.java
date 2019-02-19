package com.chen.editor.widget.action;

/**
 * Created by chenjc on 2018/10/18.
 */

public class ReverseX extends Action {
    private int mReverseXTimes;

    public ReverseX(int reverseXTimes) {
        super(ActionType.REVERSE_X);
        mReverseXTimes = reverseXTimes;
    }

    public int getReverseXTimes() {
        return mReverseXTimes;
    }

    public void setReverseXTimes(int reverseXTimes) {
        mReverseXTimes = reverseXTimes;
    }
}
