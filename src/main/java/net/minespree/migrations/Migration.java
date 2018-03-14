package net.minespree.migrations;

import com.mongodb.client.model.UpdateOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @since 28/10/2017
 */
@Getter
@AllArgsConstructor
public abstract class Migration {
    public static final UpdateOptions OPTIONS = new UpdateOptions().upsert(false);

    protected String name;
    protected MigrationManager manager;

    public abstract boolean init();

    protected void migrationCount(long count) {
        log("Migrating " + count + " documents");
    }

    protected void log(String message, Object... args) {
        getLogger().log(Level.INFO, "[" + name + "] " + message, args);
    }

    protected Logger getLogger() {
        return manager.getLogger();
    }

}
