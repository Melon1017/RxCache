import com.safframework.rxcache.RxCache;
import com.safframework.rxcache.converter.KryoConverter;
import com.safframework.rxcache.converter.MoshiConverter;
import com.safframework.rxcache.domain.Record;
import com.safframework.rxcache.persistence.converter.GsonConverter;
import com.safframework.rxcache.persistence.disk.impl.DiskImpl;
import com.safframework.rxcache.persistence.encrypt.AES128Encryptor;
import domain.User;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;

import java.io.File;

/**
 * Created by tony on 2018/10/2.
 */
public class TestDiskImplWithEncrypt {

    public static void main(String[] args) {

        File cacheDirectory = new File("aaa");

        if (!cacheDirectory.exists()) {

            cacheDirectory.mkdir();
        }

        AES128Encryptor encryptor = new AES128Encryptor("abcdefghijklmnop");
        DiskImpl diskImpl = new DiskImpl(cacheDirectory,new GsonConverter(encryptor));

        RxCache.config(new RxCache.Builder().persistence(diskImpl));

        RxCache rxCache = RxCache.getRxCache();

        User u = new User();
        u.name = "tony";
        u.password = "123456";
        rxCache.save("test",u);

        Observable<Record<User>> observable = rxCache.load2Observable("test", User.class);

        observable.subscribe(new Consumer<Record<User>>() {

            @Override
            public void accept(Record<User> record) throws Exception {

                User user = record.getData();
                System.out.println(user.name);
                System.out.println(user.password);
            }
        });
    }
}
