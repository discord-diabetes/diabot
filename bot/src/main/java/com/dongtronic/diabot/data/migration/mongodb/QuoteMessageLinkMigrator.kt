package com.dongtronic.diabot.data.migration.mongodb

import com.dongtronic.diabot.util.DiabotCollection
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.Document

@ChangeLog(order = "009")
class QuoteMessageLinkMigrator {
    @ChangeSet(order = "001", id = "removeQuoteMessageLink", author = "Garlic")
    fun migrate(db: MongoDatabase) {
        db.getCollection(DiabotCollection.QUOTES.getEnv())
                .updateMany(Document(), Updates.unset("messageLink"))
    }
}