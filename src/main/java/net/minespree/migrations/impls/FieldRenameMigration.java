package net.minespree.migrations.impls;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import net.minespree.migrations.Migration;
import net.minespree.migrations.MigrationManager;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renames newer fields that could get a simpler/clearer name
 * @since 28/10/2017
 */
public class FieldRenameMigration extends Migration {
    private static final Map<String, Object> RENAME_FIELDS = new HashMap<>();

    static {
        RENAME_FIELDS.put("unlockedCosmeticsNew", "unlockedCosmetics");
        RENAME_FIELDS.put("achievementsNew", "achievements");
    }

    public FieldRenameMigration(MigrationManager manager) {
        super("FieldRename", manager);
    }

    @Override
    public boolean init() {
        MongoCollection<Document> collection = manager.getCollection("players");

        long total = collection.count();
        migrationCount(total);

        FindIterable<Document> all = collection.find();
        List<UpdateOneModel<Document>> updates = new ArrayList<>();

        Document renameDoc = new Document(RENAME_FIELDS);

        for (Document document : all) {
            updates.add(new UpdateOneModel<>(
                    Filters.eq("_id", document.get("_id")),
                    new Document("$rename", renameDoc),
                    OPTIONS
            ));
        }

        BulkWriteResult result = collection.bulkWrite(updates);

        return result.wasAcknowledged();
    }
}
