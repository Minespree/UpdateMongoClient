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
import java.util.List;

/**
 * Migrates the old kits structure to a new schema
 * @since 28/10/2017
 */
public class KitMigration extends Migration {
    private static final Document UNSET_DOC = new Document("unlockedKits", "");

    public KitMigration(MigrationManager manager) {
        super("KitArrayToObj", manager);
    }

    @Override
    public boolean init() {
        MongoCollection<Document> collection = manager.getCollection("players");

        long total = collection.count();
        migrationCount(total);

        FindIterable<Document> all = collection.find();
        List<UpdateOneModel<Document>> updates = new ArrayList<>();

        for (Document document : all) {
            Object unlockedKitsObj = document.get("unlockedKits");

            if (unlockedKitsObj == null) continue;

            Document unlockedKits = (Document) unlockedKitsObj;
            Document setDoc = new Document();

            for (String gameId : unlockedKits.keySet()) {
                Document gameDoc = new Document();
                List<Document> kits = (List<Document>) unlockedKits.get(gameId);

                Document selectedKit = kits.stream().filter(kitDoc -> kitDoc.getBoolean("default")).findAny().orElse(null);

                if (selectedKit != null) {
                    gameDoc.put("equipped", selectedKit.getString("id"));
                }

                Document tiersDoc = new Document();

                kits.forEach(kitDoc -> {
                    tiersDoc.put(kitDoc.getString("id"), kitDoc.getInteger("tier"));
                });

                gameDoc.put("tiers", tiersDoc);

                setDoc.put(gameId, gameDoc);
            }

            Document update = new Document("$set", new Document("kits", setDoc));
            update.put("$unset", UNSET_DOC);

            updates.add(new UpdateOneModel<>(
                    Filters.eq("_id", document.get("_id")),
                    update,
                    OPTIONS
            ));
        }

        BulkWriteResult result = collection.bulkWrite(updates);

        return result.wasAcknowledged();
    }
}
