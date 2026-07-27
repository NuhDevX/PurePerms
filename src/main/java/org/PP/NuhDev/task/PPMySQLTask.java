package org.PP.NuhDev.task;

import org.PP.NuhDev.PurePerms;
import org.powernukkitx.scheduler.Task;

import java.sql.Connection;
import java.sql.SQLException;

public class PPMySQLTask extends Task {

    private final Connection db;
    private final PurePerms plugin;

    public PPMySQLTask(PurePerms plugin, Connection db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            if (this.db != null && this.db.isValid(3)) {
                this.plugin.getLogger().debug("Connected to MySQL Server");
            } else {
                this.plugin.getLogger().debug("[MySQL] Warning: Connection is invalid or lost.");
            }
        } catch (SQLException e) {
            this.plugin.getLogger().debug("[MySQL] Warning: " + e.getMessage());
        }
    }
}
