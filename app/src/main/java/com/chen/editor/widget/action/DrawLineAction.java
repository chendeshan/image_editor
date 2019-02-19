package com.chen.editor.widget.action;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjc on 2018/10/18.
 */

public class DrawLineAction extends Action {
    private List<Line> mLines = new ArrayList<>();

    public DrawLineAction() {
        super(ActionType.DRAW_LINE);
    }

    public List<Line> getLines() {
        return mLines;
    }

    public void setLines(List<Line> lines) {
        mLines = lines;
    }

    public void addLine(Line line) {
        mLines.add(line);
    }

    public static class Line {
        private float _startX;
        private float _startY;
        private float _endX;
        private float _endY;

        public Line(float startX, float startY, float endX, float endY) {
            _startX = startX;
            _startY = startY;
            _endX = endX;
            _endY = endY;
        }


        public float getStartX() {
            return _startX;
        }

        public void setStartX(float startX) {
            _startX = startX;
        }

        public float getStartY() {
            return _startY;
        }

        public void setStartY(float startY) {
            _startY = startY;
        }

        public float getEndX() {
            return _endX;
        }

        public void setEndX(float endX) {
            _endX = endX;
        }

        public float getEndY() {
            return _endY;
        }

        public void setEndY(float endY) {
            _endY = endY;
        }
    }
}
