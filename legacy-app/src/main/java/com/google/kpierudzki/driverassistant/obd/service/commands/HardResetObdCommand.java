package com.google.kpierudzki.driverassistant.obd.service.commands;

import com.github.pires.obd.commands.protocol.ObdProtocolCommand;

/**
 * Created by Kamil on 15.09.2017.
 */
public class HardResetObdCommand extends ObdProtocolCommand {

    public HardResetObdCommand() {
        super("ATD");
    }

    @Override
    public String getFormattedResult() {
        return getResult();
    }

    @Override
    public String getName() {
        return "Hard reset";
    }
}
