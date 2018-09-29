package com.cv4j.rxcache.extra.persistence.disk.converter;

import com.alibaba.fastjson.JSON;
import com.cv4j.rxcache.persistence.disk.converter.Converter;
import com.safframework.tony.common.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Created by tony on 2018/9/29.
 */
public class FastJSONConverter implements Converter {

    @Override
    public <T> T read(InputStream source, Type type) {
        String json = null;
        ByteArrayOutputStream outSteam = null;
        try {

            outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while( (len = source.read(buffer)) !=-1 ){
                if (len!=0) {
                    outSteam.write(buffer, 0, len);
                }
            }

            json = new String(outSteam.toByteArray(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            IOUtils.closeQuietly(outSteam);
        }

        return JSON.parseObject(json, type);
    }

    @Override
    public void writer(OutputStream sink, Object data) {

        String wrapperJSONSerialized = JSON.toJSONString(data);
        byte[] buffer = wrapperJSONSerialized.getBytes();

        try {
            sink.write(buffer, 0, buffer.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}