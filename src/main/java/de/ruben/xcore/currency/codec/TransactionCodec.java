package de.ruben.xcore.currency.codec;

import com.mongodb.MongoClient;
import de.ruben.xcore.currency.account.type.Transaction;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class TransactionCodec implements Codec<Transaction> {


    @Override
    public Transaction decode(BsonReader bsonReader, DecoderContext decoderContext) {
        Document document =  MongoClient.getDefaultCodecRegistry().get(Document.class).decode(bsonReader, decoderContext);
        return new Transaction().fromDocument(document);
    }

    @Override
    public void encode(BsonWriter bsonWriter, Transaction transaction, EncoderContext encoderContext) {
        MongoClient.getDefaultCodecRegistry().get(Document.class).encode(bsonWriter, transaction.toDocument(), encoderContext);
    }

    @Override
    public Class<Transaction> getEncoderClass() {
        return Transaction.class;
    }
}
