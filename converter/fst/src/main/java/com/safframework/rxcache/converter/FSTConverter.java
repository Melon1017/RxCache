package com.safframework.rxcache.converter;

import com.safframework.rxcache.persistence.converter.AbstractConverter;
import com.safframework.rxcache.persistence.encrypt.Encryptor;
import org.nustaq.serialization.FSTConfiguration;

import java.lang.reflect.Type;

/**
 * Created by tony on 2019-04-16.
 */
public class FSTConverter extends AbstractConverter {

    private static FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();

    public FSTConverter() {
    }

    public FSTConverter(Encryptor encryptor) {
        super(encryptor);
    }


    @Override
    public <T> T fromJson(String json, Type type) {
        return (T)conf.asObject(json.getBytes());
    }

    @Override
    public String toJson(Object data) {

        byte barray[] = conf.asByteArray(data);
        return new String(barray);
    }
}
