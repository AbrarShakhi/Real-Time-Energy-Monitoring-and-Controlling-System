package com.abrarshakhi.rtemcs.model;

import java.util.List;

public class TuyaCommand {
    private List<Command> commands;

    public TuyaCommand(List<Command> commands) {
        this.commands = commands;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    public static class Command {
        private String code;
        private Object value;

        public Command(String code, Object value) {
            this.code = code;
            this.value = value;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
